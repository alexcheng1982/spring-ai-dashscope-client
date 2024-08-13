package io.github.alexcheng1982.springai.dashscope;

import com.alibaba.dashscope.aigc.generation.GenerationOutput.Choice;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Message.MessageBuilder;
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
import io.github.alexcheng1982.springai.dashscope.metadata.DashscopeUsage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.AbstractToolCallSupport;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.model.ModelOptionsUtils;
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
public class DashscopeChatModel extends AbstractToolCallSupport implements
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
    Assert.notNull(dashscopeApi, "dashscopeApi must not be null");
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
    var request = createRequest(prompt);
    var result = doChatCompletion(request);
    var response = chatCompletionResultToChatResponse(result);
    if (request.isMultiModalRequest() || !isToolCall(response)) {
      return response;
    }
    var toolCallConversation = handleToolCalls(prompt, response);
    return this.call(new Prompt(toolCallConversation, prompt.getOptions()));
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
              .map(this::multiModalConversationResultToChatResponse));
    } else {
      return RxJava2Adapter.flowableToFlux(
          dashscopeApi.chatCompletionStream(request.getMessages(),
                  request.options())
              .flatMap(result -> {
                var response = generationResultToChatResponse(
                    result);
                if (!isToolCall(response, Set.of("tool"))) {
                  return Flux.just(response);
                } else {
                  var toolCallConversation = handleToolCalls(prompt, response);
                  return this.stream(
                      new Prompt(toolCallConversation, prompt.getOptions()));
                }
              }));
    }
  }

  private boolean isToolCall(ChatResponse response) {
    return isToolCall(response, Set.of("tool_calls"));
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
            new AssistantMessage(
                (String) choice.getMessage().getContent().get(0).get("text")),
            ChatGenerationMetadata.from(choice.getFinishReason(),
                null))).toList();
    return new ChatResponse(generations, buildChatResponseMetadata(result));
  }

  private ChatResponse generationResultToChatResponse(
      GenerationResult generationResult) {
    List<org.springframework.ai.chat.model.Generation> generations = generationResult.getOutput()
        .getChoices().stream()
        .map(choice -> buildGeneration(choice, new HashMap<>())).toList();
    return new ChatResponse(generations,
        buildChatResponseMetadata(generationResult));
  }

  private Generation buildGeneration(Choice choice,
      Map<String, Object> metadata) {
    List<AssistantMessage.ToolCall> toolCalls =
        choice.getMessage().getToolCalls() == null ? List.of()
            : choice.getMessage()
                .getToolCalls()
                .stream()
                .filter(toolCall -> toolCall.getType().equals("function"))
                .map(toolCall -> (ToolCallFunction) toolCall)
                .map(toolCall -> new AssistantMessage.ToolCall(toolCall.getId(),
                    toolCall.getType(),
                    toolCall.getFunction().getName(),
                    toolCall.getFunction().getArguments()))
                .toList();

    var assistantMessage = new AssistantMessage(
        choice.getMessage().getContent(),
        metadata, toolCalls);
    String finishReason = (choice.getFinishReason() != null
        ? choice.getFinishReason() : "");
    var generationMetadata = ChatGenerationMetadata.from(finishReason, null);
    return new Generation(assistantMessage, generationMetadata);
  }

  private ChatResponseMetadata buildChatResponseMetadata(
      GenerationResult result) {
    return ChatResponseMetadata.builder()
        .withUsage(new DashscopeUsage(result.getUsage()))
        .build();
  }

  private ChatResponseMetadata buildChatResponseMetadata(
      MultiModalConversationResult result) {
    return ChatResponseMetadata.builder()
        .withUsage(new DashscopeUsage(result.getUsage()))
        .build();
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
      var promptOptions = ModelOptionsUtils.copyToTarget(
          prompt.getOptions(),
          ChatOptions.class, DashscopeChatOptions.class);
      options = options.copyFrom(promptOptions);

      Set<String> promptEnabledFunctions = this.runtimeFunctionCallbackConfigurations(
          options);
      functionsForThisRequest.addAll(promptEnabledFunctions);
    }

    if (this.defaultOptions != null && !CollectionUtils.isEmpty(
        this.defaultOptions.getFunctions())) {
      functionsForThisRequest.addAll(this.defaultOptions.getFunctions());
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

  private ChatCompletionResult doChatCompletion(
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

  private List<ChatCompletionMessage> toDashscopeMessages(
      List<org.springframework.ai.chat.messages.Message> messages) {
    if (messages.stream()
        .anyMatch(message -> message instanceof UserMessage userMessage
            && !CollectionUtils.isEmpty(userMessage.getMedia()))) {
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
    MessageBuilder<?, ?> builder = Message.builder()
        .role(roleFrom(message.getMessageType()))
        .content(message.getContent());
    if (message instanceof ToolResponseMessage toolResponseMessage
        && !CollectionUtils.isEmpty(toolResponseMessage.getResponses())) {
      var toolResponse = toolResponseMessage.getResponses().get(0);
      builder.toolCallId(toolResponse.id())
          .name(toolResponse.name())
          .content(toolResponse.responseData());
    } else if (message instanceof AssistantMessage assistantMessage) {
      builder.toolCalls(assistantMessage.getToolCalls().stream()
          .map(toolCall -> {
            var toolCallFunction = new ToolCallFunction();
            toolCallFunction.setId(toolCall.id());
            var callFunction = toolCallFunction.new CallFunction();
            callFunction.setName(toolCall.name());
            callFunction.setArguments(toolCall.arguments());
            toolCallFunction.setFunction(callFunction);
            return (ToolCallBase) toolCallFunction;
          })
          .toList());
    }
    return builder.build();
  }

  private MultiModalMessage toDashscopeMultiModalMessage(
      org.springframework.ai.chat.messages.Message message) {
    List<Map<String, Object>> content = new ArrayList<>();
    if (message instanceof UserMessage userMessage) {
      for (Media media : userMessage.getMedia()) {
        content.add(new HashMap<>() {{
          put(media.getMimeType().getType(), media.getData());
        }});
      }
    }
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
      case TOOL -> Role.TOOL.getValue();
      default -> Role.USER.getValue();
    };
  }
}
