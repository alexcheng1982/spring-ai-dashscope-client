package io.github.alexcheng1982.springai.dashscope.metadata;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;

public class DashscopeChatResponseMetadata implements ChatResponseMetadata {

  private final DashscopeUsage usage;

  public DashscopeChatResponseMetadata(GenerationResult generationResult) {
    this.usage = new DashscopeUsage(generationResult.getUsage());
  }

  @Override
  public Usage getUsage() {
    return this.usage;
  }
}
