package io.github.alexcheng1982.springai.dashscope;

import com.alibaba.dashscope.aigc.generation.GenerationOutput.Choice;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.tools.FunctionDefinition;
import com.alibaba.dashscope.tools.ToolBase;
import com.alibaba.dashscope.tools.ToolCallBase;
import com.alibaba.dashscope.tools.ToolCallFunction;
import com.alibaba.dashscope.tools.ToolFunction;
import com.alibaba.dashscope.utils.JsonUtils;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeApi;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeApi.ChatCompletionRequest;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeModelName;
import io.github.alexcheng1982.springai.dashscope.metadata.DashscopeChatResponseMetadata;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Spring AI {@linkplain ChatClient} for Aliyun Dashscope
 */
public class DashscopeChatClient extends
    AbstractFunctionCallSupport<Message, ChatCompletionRequest, GenerationResult> implements
    ChatClient {

  private static final DashscopeChatOptions DEFAULT_OPTIONS = DashscopeChatOptions.builder()
      .withModel(DashscopeModelName.QWEN_MAX)
      .withTemperature(0.7f).build();
  private final DashscopeChatOptions defaultOptions;
  private final DashscopeApi dashscopeApi;

  public DashscopeChatClient(DashscopeApi dashscopeApi) {
    this(dashscopeApi, DEFAULT_OPTIONS);
  }

  public DashscopeChatClient(DashscopeApi dashscopeApi,
      DashscopeChatOptions options) {
    this(dashscopeApi, options, null);
  }

  public DashscopeChatClient(DashscopeApi dashscopeApi,
      DashscopeChatOptions options,
      FunctionCallbackContext functionCallbackContext) {
    super(functionCallbackContext);
    Assert.notNull(dashscopeApi, "DashscopeApi must not be null");
    Assert.notNull(options, "Options must not be null");
    this.dashscopeApi = dashscopeApi;
    this.defaultOptions = options;
  }

  /**
   * Create a {@linkplain DashscopeChatClient} with default options
   *
   * @return A {@linkplain DashscopeChatClient}
   */
  public static DashscopeChatClient createDefault() {
    return new DashscopeChatClient(new DashscopeApi());
  }

  @Override
  public ChatResponse call(Prompt prompt) {
    var generationResult = callWithFunctionSupport(createRequest(prompt));
    List<org.springframework.ai.chat.Generation> generations = generationResult.getOutput()
        .getChoices().stream()
        .map(choice -> new org.springframework.ai.chat.Generation(
            choice.getMessage().getContent())
            .withGenerationMetadata(
                ChatGenerationMetadata.from(choice.getFinishReason(),
                    null))).toList();
    return new ChatResponse(generations,
        new DashscopeChatResponseMetadata(generationResult));
  }

  private ChatCompletionRequest createRequest(Prompt prompt) {
    Set<String> functionsForThisRequest = new HashSet<>();

    List<Message> chatCompletionMessages = toDashscopeMessages(
        prompt.getInstructions());

    DashscopeChatOptions options = defaultOptions;

    if (prompt.getOptions() != null) {
      if (prompt.getOptions() instanceof ChatOptions runtimeOptions) {
        options = ModelOptionsUtils.copyToTarget(
            runtimeOptions,
            ChatOptions.class, DashscopeChatOptions.class);

        Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(
            options,
            IS_RUNTIME_CALL);
        functionsForThisRequest.addAll(promptEnabledFunctions);
      } else {
        throw new IllegalArgumentException(
            "Prompt options are not of type ChatOptions: "
                + prompt.getOptions().getClass().getSimpleName());
      }
    }

    if (this.defaultOptions != null) {

      Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(
          this.defaultOptions,
          !IS_RUNTIME_CALL);

      functionsForThisRequest.addAll(defaultEnabledFunctions);
    }

    if (!CollectionUtils.isEmpty(functionsForThisRequest)) {
      options = DashscopeChatOptions.builder(options)
          .withTools(this.getToolFunctions(functionsForThisRequest))
          .build();
    }

    return new ChatCompletionRequest(chatCompletionMessages, options);
  }

  private List<ToolBase> getToolFunctions(
      Set<String> functionNames) {
    return this.resolveFunctionCallbacks(functionNames)
        .stream()
        .map(this::toToolFunction)
        .toList();
  }

  private ToolBase toToolFunction(FunctionCallback functionCallback) {
    var fd = FunctionDefinition.builder()
        .name(functionCallback.getName())
        .description(functionCallback.getDescription())
        .parameters(
            JsonUtils.parseString(functionCallback.getInputTypeSchema())
                .getAsJsonObject())
        .build();
    return ToolFunction.builder().function(fd).build();
  }

  @Override
  protected ChatCompletionRequest doCreateToolResponseRequest(
      ChatCompletionRequest previousRequest, Message responseMessage,
      List<Message> conversationHistory) {
    for (ToolCallBase toolCall : responseMessage.getToolCalls()) {
      if (toolCall.getType().equals("function")) {
        var functionName = ((ToolCallFunction) toolCall).getFunction()
            .getName();
        String functionArguments = ((ToolCallFunction) toolCall).getFunction()
            .getArguments();

        if (!this.functionCallbackRegister.containsKey(functionName)) {
          throw new IllegalStateException(
              "No function callback found for function name: " + functionName);
        }

        String functionResponse = this.functionCallbackRegister.get(
                functionName)
            .call(functionArguments);

        conversationHistory
            .add(Message.builder().role("tool")
                .content(functionResponse)
                .toolCallId(toolCall.getId())
                .build());
      }
    }

    return new ChatCompletionRequest(
        conversationHistory, previousRequest.options());
  }

  @Override
  protected List<Message> doGetUserMessages(ChatCompletionRequest request) {
    return request.messages();
  }

  @Override
  protected Message doGetToolResponseMessage(GenerationResult response) {
    return response.getOutput().getChoices().get(0).getMessage();
  }

  @Override
  protected GenerationResult doChatCompletion(ChatCompletionRequest request) {
    return this.dashscopeApi.chatCompletion(request.messages(),
        request.options());
  }

  @Override
  protected boolean isToolFunctionCall(GenerationResult response) {
    List<Choice> choices = response.getOutput().getChoices();
    if (CollectionUtils.isEmpty(choices)) {
      return false;
    }
    var choice = choices.get(0);
    return !CollectionUtils.isEmpty(choice.getMessage().getToolCalls());
  }

  private List<Message> toDashscopeMessages(
      List<org.springframework.ai.chat.messages.Message> messages) {
    return messages.stream().map(this::toDashscopeMessage).toList();
  }

  private Message toDashscopeMessage(
      org.springframework.ai.chat.messages.Message message) {
    return Message.builder()
        .role(roleFrom(message.getMessageType()))
        .content(message.getContent())
        .build();
  }

  private String roleFrom(MessageType messageType) {
    return switch (messageType) {
      case SYSTEM -> Role.SYSTEM.getValue();
      case ASSISTANT -> Role.ASSISTANT.getValue();
      case FUNCTION -> "tool";
      default -> Role.USER.getValue();
    };
  }
}
