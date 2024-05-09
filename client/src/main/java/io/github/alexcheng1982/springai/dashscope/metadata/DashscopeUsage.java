package io.github.alexcheng1982.springai.dashscope.metadata;

import com.alibaba.dashscope.aigc.generation.GenerationUsage;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationUsage;
import java.util.Optional;
import org.springframework.ai.chat.metadata.Usage;

/**
 * {@linkplain Usage} implementation of Aliyun Dashscope
 */
public class DashscopeUsage implements Usage {

  private Long promptTokens;
  private Long generationTokens;
  private Long totalTokens;

  public DashscopeUsage(GenerationUsage usage) {
    if (usage != null) {
      this.promptTokens = integerToLong(usage.getInputTokens());
      this.generationTokens = integerToLong(usage.getOutputTokens());
      this.totalTokens = integerToLong(usage.getTotalTokens());
    }
  }

  public DashscopeUsage(MultiModalConversationUsage usage) {
    if (usage != null) {
      this.promptTokens = integerToLong(usage.getInputTokens());
      this.generationTokens = integerToLong(usage.getOutputTokens());
      this.totalTokens = this.promptTokens + this.generationTokens;
    }
  }

  private Long integerToLong(Integer value) {
    return Optional.ofNullable(value)
        .map(Integer::longValue)
        .orElse(null);
  }

  @Override
  public Long getPromptTokens() {
    return this.promptTokens;
  }

  @Override
  public Long getGenerationTokens() {
    return this.generationTokens;
  }

  @Override
  public Long getTotalTokens() {
    return this.totalTokens;
  }

  @Override
  public String toString() {
    return "DashscopeUsage{" +
        "promptTokens=" + promptTokens +
        ", generationTokens=" + generationTokens +
        ", totalTokens=" + totalTokens +
        '}';
  }
}
