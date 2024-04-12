package io.github.alexcheng1982.springai.dashscope.api;

import static com.alibaba.dashscope.aigc.generation.GenerationParam.ResultFormat.MESSAGE;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.github.alexcheng1982.springai.dashscope.DashscopeChatOptions;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Dashscope API
 */
public class DashscopeApi {

  private final Generation generation;

  public DashscopeApi() {
    this.generation = new Generation();
  }

  public GenerationResult chatCompletion(
      List<Message> messages,
      DashscopeChatOptions options) {
    var builder = GenerationParam.builder()
        .model(options.getModel())
        .topP(Optional.ofNullable(options.getTopP()).map(Double::valueOf)
            .orElse(null))
        .topK(options.getTopK())
        .enableSearch(Objects.equals(options.getEnableSearch(), Boolean.TRUE))
        .seed(options.getSeed())
        .repetitionPenalty(options.getRepetitionPenalty())
        .temperature(options.getTemperature())
        .maxTokens(options.getMaxTokens())
        .messages(messages)
        .tools(options.getTools())
        .resultFormat(MESSAGE);

    if (options.getStops() != null) {
      builder.stopStrings(options.getStops());
    }

    try {
      return generation.call(builder.build());
    } catch (NoApiKeyException | InputRequiredException e) {
      throw new RuntimeException(e);
    }
  }


  public record ChatCompletionRequest(
      List<Message> messages,
      DashscopeChatOptions options) {

  }
}
