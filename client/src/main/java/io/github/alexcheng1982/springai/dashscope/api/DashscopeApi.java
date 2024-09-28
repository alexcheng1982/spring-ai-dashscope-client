package io.github.alexcheng1982.springai.dashscope.api;

import static com.alibaba.dashscope.aigc.generation.GenerationParam.ResultFormat.MESSAGE;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import io.github.alexcheng1982.springai.dashscope.DashscopeChatOptions;
import io.reactivex.Flowable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Dashscope API
 */
public class DashscopeApi {

  private final Generation generation;
  private final MultiModalConversation multiModalConversation;

  public DashscopeApi() {
    this.generation = new Generation();
    this.multiModalConversation = new MultiModalConversation();
  }

  public GenerationResult chatCompletion(
      List<Message> messages,
      DashscopeChatOptions options) {
    try {
      return generation.call(buildGenerationParam(messages, options));
    } catch (ApiException | NoApiKeyException | InputRequiredException e) {
      throw new DashscopeApiException(e);
    }
  }

  public Flowable<GenerationResult> chatCompletionStream(List<Message> messages,
      DashscopeChatOptions options) {
    try {
      return generation.streamCall(
          buildGenerationParam(messages, options));
    } catch (ApiException | NoApiKeyException | InputRequiredException e) {
      throw new DashscopeApiException(e);
    }
  }

  private GenerationParam buildGenerationParam(List<Message> messages,
      DashscopeChatOptions options) {
    var builder = GenerationParam.builder()
        .model(options.getModel())
        .topP(Optional.ofNullable(options.getTopP()).map(Double::valueOf)
            .orElse(null))
        .topK(options.getTopK())
        .enableSearch(Objects.equals(options.getEnableSearch(), Boolean.TRUE))
        .seed(options.getSeed())
        .repetitionPenalty(doubleToFloat(options.getRepetitionPenalty()))
        .temperature(doubleToFloat(options.getTemperature()))
        .maxTokens(options.getMaxTokens())
        .messages(messages)
        .tools(options.getTools())
        .resultFormat(MESSAGE)
        .incrementalOutput(
            Objects.equals(options.getIncrementalOutput(), Boolean.TRUE));

    if (options.getStops() != null) {
      builder.stopStrings(options.getStops());
    }
    return builder.build();
  }

  private Float doubleToFloat(Double value) {
    return Optional.ofNullable(value).map(Double::floatValue).orElse(null);
  }

  public MultiModalConversationResult multiModal(
      List<MultiModalMessage> messages,
      DashscopeChatOptions options) {
    try {
      return multiModalConversation.call(
          buildMultiModalConversationParam(messages, options, false));
    } catch (ApiException | NoApiKeyException | UploadFileException e) {
      throw new DashscopeApiException(e);
    }
  }

  public Flowable<MultiModalConversationResult> multiModalStream(
      List<MultiModalMessage> messages,
      DashscopeChatOptions options) {
    try {
      return multiModalConversation.streamCall(
          buildMultiModalConversationParam(messages, options, true));
    } catch (ApiException | NoApiKeyException | UploadFileException e) {
      throw new DashscopeApiException(e);
    }
  }

  private MultiModalConversationParam buildMultiModalConversationParam(
      List<MultiModalMessage> messages, DashscopeChatOptions options,
      boolean streaming) {
    return MultiModalConversationParam.builder()
        .model(options.getModel())
        .messages(messages)
        .temperature(doubleToFloat(options.getTemperature()))
        .topP(options.getTopP())
        .topK(options.getTopK())
        .enableSearch(options.getEnableSearch())
        .maxLength(options.getMaxTokens())
        .incrementalOutput(streaming)
        .build();
  }

  /**
   * Union type of {@linkplain Message} and {@linkplain MultiModalMessage}
   *
   * @param message           Message
   * @param multiModalMessage MultiModalMessage
   */
  public record ChatCompletionMessage(
      Message message,
      MultiModalMessage multiModalMessage) {

    public ChatCompletionMessage(Message message) {
      this(message, null);
    }

    public ChatCompletionMessage(MultiModalMessage multiModalMessage) {
      this(null, multiModalMessage);
    }
  }

  public record ChatCompletionRequest(
      List<ChatCompletionMessage> messages,
      DashscopeChatOptions options) {

    public boolean isMultiModalRequest() {
      return messages.stream()
          .anyMatch(message -> message.multiModalMessage() != null);
    }

    public List<Message> getMessages() {
      return messages.stream().map(ChatCompletionMessage::message)
          .filter(Objects::nonNull).toList();
    }

    public List<MultiModalMessage> getMultiModalMessages() {
      return messages.stream().map(ChatCompletionMessage::multiModalMessage)
          .filter(Objects::nonNull).toList();
    }
  }

  /**
   * Union type of {@linkplain GenerationResult} and
   * {@linkplain MultiModalConversationResult}
   *
   * @param generationResult             GenerationResult
   * @param multiModalConversationResult MultiModalConversationResult
   */
  public record ChatCompletionResult(
      GenerationResult generationResult,
      MultiModalConversationResult multiModalConversationResult) {

    public ChatCompletionResult(GenerationResult generationResult) {
      this(generationResult, null);
    }

    public ChatCompletionResult(
        MultiModalConversationResult multiModalConversationResult) {
      this(null, multiModalConversationResult);
    }
  }
}
