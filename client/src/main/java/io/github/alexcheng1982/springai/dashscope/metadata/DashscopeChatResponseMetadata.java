package io.github.alexcheng1982.springai.dashscope.metadata;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;

/**
 * {@linkplain ChatResponseMetadata} implementation of Aliyun Dashscope
 */
public class DashscopeChatResponseMetadata implements ChatResponseMetadata {

  private final DashscopeUsage usage;

  public DashscopeChatResponseMetadata(GenerationResult generationResult) {
    this.usage = new DashscopeUsage(generationResult.getUsage());
  }

  public DashscopeChatResponseMetadata(MultiModalConversationResult result) {
    this.usage = new DashscopeUsage(result.getUsage());
  }

  @Override
  public Usage getUsage() {
    return this.usage;
  }

  @Override
  public String toString() {
    return "DashscopeChatResponseMetadata{" +
        "usage=" + usage +
        '}';
  }
}
