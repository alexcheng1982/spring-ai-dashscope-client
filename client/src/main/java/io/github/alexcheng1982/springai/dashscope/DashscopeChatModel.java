package io.github.alexcheng1982.springai.dashscope;

import com.alibaba.dashscope.aigc.generation.GenerationOutput.Choice;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.tools.FunctionDefinition;
import com.alibaba.dashscope.tools.ToolBase;
import com.alibaba.dashscope.tools.ToolCallBase;
import com.alibaba.dashscope.tools.ToolCallFunction;
import com.alibaba.dashscope.tools.ToolFunction;
import com.alibaba.dashscope.utils.JsonUtils;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeApi;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeApi.ChatCompletionMessage;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeApi.ChatCompletionRequest;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeApi.ChatCompletionResult;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeModelName;
import io.github.alexcheng1982.springai.dashscope.metadata.DashscopeChatResponseMetadata;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;

/**
 * Spring AI {@linkplain ChatModel} and {@linkplain StreamingChatModel} for
 * Aliyun Dashscope
 */
public class DashscopeChatModel extends
    AbstractFunctionCallSupport<ChatCompletionMessage, ChatCompletionRequest, ChatCompletionResult> implements
    ChatModel, StreamingChatModel {

  private static final DashscopeChatOptions DEFAULT_OPTIONS = DashscopeChatOptions.builder()
      .withModel(DashscopeChatOptions.DEFAULT_MODEL)
      .build();
  private final DashscopeChatOptions defaultOptions;
  private final DashscopeApi dashscopeApi;

  public DashscopeChatModel(DashscopeApi dashscopeApi) {
    this(dashscopeApi, DEFAULT_OPTIONS);
  }

  public DashscopeChatModel(DashscopeApi dashscopeApi,
      DashscopeChatOptions options) {
    this(dashscopeApi, options, null);
  }

  public DashscopeChatModel(DashscopeApi dashscopeApi,
      FunctionCallbackContext functionCallbackContext) {
    this(dashscopeApi, DEFAULT_OPTIONS, functionCallbackContext);
  }

  public DashscopeChatModel(DashscopeApi dashscopeApi,
      DashscopeChatOptions options,
      FunctionCallbackContext functionCallbackContext) {
    super(functionCallbackContext);
    Assert.notNull(dashscopeApi, "DashscopeApi must not be null");
    Assert.notNull(options, "Options must not be null");
    this.dashscopeApi = dashscopeApi;
    this.defaultOptions = options;
  }

  /**
   * Create a {@linkplain DashscopeChatModel} with default options
   *
   * @return A {@linkplain DashscopeChatModel}
   */
  public static DashscopeChatModel createDefault() {
    return new DashscopeChatModel(new DashscopeApi());
  }

  @Override
  public ChatResponse call(Prompt prompt) {
    var generationResult = callWithFunctionSupport(createRequest(prompt));
    return chatCompletionResultToChatResponse(generationResult);
  }

  @Override
  public ChatOptions getDefaultOptions() {
    return DashscopeChatOptions.builder()
        .withModel(DashscopeModelName.QWEN_TURBO).build();
  }

  @Override
  public Flux<ChatResponse> stream(Prompt prompt) {
    var request = createRequest(prompt);
    if (request.isMultiModalRequest()) {
      return RxJava2Adapter.flowableToFlux(
          dashscopeApi.multiModalStream(request.getMultiModalMessages(),
                  request.options())
              .map(result -> {
                var response = handleFunctionCallOrReturn(request,
                    new ChatCompletionResult(result));
                return chatCompletionResultToChatResponse(response);
              }));
    } else {
      return RxJava2Adapter.flowableToFlux(
          dashscopeApi.chatCompletionStream(request.getMessages(),
                  request.options())
              .map(result -> {
                var response = handleFunctionCallOrReturn(request,
                    new ChatCompletionResult(result));
                return chatCompletionResultToChatResponse(response);
              }));
    }
  }

  private ChatResponse chatCompletionResultToChatResponse(
      ChatCompletionResult result) {
    if (result.multiModalConversationResult() != null) {
      return multiModalConversationResultToChatResponse(
          result.multiModalConversationResult());
    } else {
      return generationResultToChatResponse(result.generationResult());
    }
  }

  private ChatResponse multiModalConversationResultToChatResponse(
      MultiModalConversationResult result) {
    List<org.springframework.ai.chat.model.Generation> generations = result.getOutput()
        .getChoices()
        .stream()
        .map(choice -> new org.springframework.ai.chat.model.Generation(
            (String) choice.getMessage().getContent().get(0).get("text"))
            .withGenerationMetadata(
                ChatGenerationMetadata.from(choice.getFinishReason(),
                    null))).toList();
    return new ChatResponse(generations,
        new DashscopeChatResponseMetadata(result));
  }

  private ChatResponse generationResultToChatResponse(
      GenerationResult generationResult) {
    List<org.springframework.ai.chat.model.Generation> generations = generationResult.getOutput()
        .getChoices().stream()
        .map(choice -> new org.springframework.ai.chat.model.Generation(
            choice.getMessage().getContent())
            .withGenerationMetadata(
                ChatGenerationMetadata.from(choice.getFinishReason(),
                    null))).toList();
    return new ChatResponse(generations,
        new DashscopeChatResponseMetadata(generationResult));
  }

  private ChatCompletionRequest createRequest(Prompt prompt) {
    Set<String> functionsForThisRequest = new HashSet<>();

    List<ChatCompletionMessage> chatCompletionMessages = toDashscopeMessages(
        prompt.getInstructions());

    DashscopeChatOptions options = new DashscopeChatOptions();
    if (defaultOptions != null) {
      options = defaultOptions.createCopy();
    }

    if (prompt.getOptions() != null) {
      if (prompt.getOptions() instanceof ChatOptions runtimeOptions) {
        var promptOptions = ModelOptionsUtils.copyToTarget(
            runtimeOptions,
            ChatOptions.class, DashscopeChatOptions.class);
        options = options.copyFrom(promptOptions);

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
      options.setTools(this.getToolFunctions(functionsForThisRequest));
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
      ChatCompletionRequest previousRequest,
      ChatCompletionMessage responseMessage,
      List<ChatCompletionMessage> conversationHistory) {
    if (responseMessage.message() != null) {
      for (ToolCallBase toolCall : responseMessage.message().getToolCalls()) {
        if (toolCall.getType().equals("function")) {
          var functionName = ((ToolCallFunction) toolCall).getFunction()
              .getName();
          String functionArguments = ((ToolCallFunction) toolCall).getFunction()
              .getArguments();

          if (!this.functionCallbackRegister.containsKey(functionName)) {
            throw new IllegalStateException(
                "No function callback found for function name: "
                    + functionName);
          }

          String functionResponse = this.functionCallbackRegister.get(
                  functionName)
              .call(functionArguments);

          conversationHistory
              .add(new ChatCompletionMessage(Message.builder().role("tool")
                  .content(functionResponse)
                  .toolCallId(toolCall.getId())
                  .build()));
        }
      }
    }

    return new ChatCompletionRequest(
        conversationHistory, previousRequest.options());
  }

  @Override
  protected List<ChatCompletionMessage> doGetUserMessages(
      ChatCompletionRequest request) {
    return request.messages();
  }

  @Override
  protected ChatCompletionMessage doGetToolResponseMessage(
      ChatCompletionResult response) {
    if (response.generationResult() != null) {
      return new ChatCompletionMessage(
          response.generationResult().getOutput().getChoices().get(0)
              .getMessage());
    } else {
      return new ChatCompletionMessage(
          response.multiModalConversationResult().getOutput().getChoices()
              .get(0).getMessage());
    }
  }

  @Override
  protected ChatCompletionResult doChatCompletion(
      ChatCompletionRequest request) {
    if (request.isMultiModalRequest()) {
      return new ChatCompletionResult(
          this.dashscopeApi.multiModal(request.getMultiModalMessages(),
              request.options()));
    } else {
      return new ChatCompletionResult(
          this.dashscopeApi.chatCompletion(request.getMessages(),
              request.options()));
    }
  }

  @Override
  protected Flux<ChatCompletionResult> doChatCompletionStream(
      ChatCompletionRequest request) {
    if (request.isMultiModalRequest()) {
      return RxJava2Adapter.flowableToFlux(
          this.dashscopeApi.multiModalStream(request.getMultiModalMessages(),
                  request.options())
              .map(result -> handleFunctionCallOrReturn(request,
                  new ChatCompletionResult(result))));
    } else {
      return RxJava2Adapter.flowableToFlux(
          this.dashscopeApi.chatCompletionStream(request.getMessages(),
                  request.options())
              .map(result -> handleFunctionCallOrReturn(request,
                  new ChatCompletionResult(result))));
    }
  }

  @Override
  protected boolean isToolFunctionCall(ChatCompletionResult response) {
    if (response.multiModalConversationResult() != null) {
      return false;
    }
    List<Choice> choices = response.generationResult().getOutput().getChoices();
    if (CollectionUtils.isEmpty(choices)) {
      return false;
    }
    var choice = choices.get(0);
    return !CollectionUtils.isEmpty(choice.getMessage().getToolCalls());
  }

  private List<ChatCompletionMessage> toDashscopeMessages(
      List<org.springframework.ai.chat.messages.Message> messages) {
    if (messages.stream()
        .anyMatch(message -> !CollectionUtils.isEmpty(message.getMedia()))) {
      return messages.stream().map(this::toDashscopeMultiModalMessage)
          .map(ChatCompletionMessage::new).toList();
    } else {
      return messages.stream().map(this::toDashscopeMessage)
          .map(ChatCompletionMessage::new)
          .toList();
    }
  }

  private Message toDashscopeMessage(
      org.springframework.ai.chat.messages.Message message) {
    return Message.builder()
        .role(roleFrom(message.getMessageType()))
        .content(message.getContent())
        .build();
  }

  private MultiModalMessage toDashscopeMultiModalMessage(
      org.springframework.ai.chat.messages.Message message) {
    var images = message.getMedia().stream()
        .map(media -> new HashMap<String, Object>() {{
          put(media.getMimeType().getType(), media.getData());
        }}).toList();
    var content = new ArrayList<Map<String, Object>>(images);
    content.add(new HashMap<>() {{
      put("text", message.getContent());
    }});
    return MultiModalMessage.builder()
        .role(roleFrom(message.getMessageType()))
        .content(content)
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
