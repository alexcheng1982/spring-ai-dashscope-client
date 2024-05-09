package io.github.alexcheng1982.springai.dashscope;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alexcheng1982.springai.dashscope.api.DashscopeModelName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.model.ModelOptionsUtils;

class DashscopeChatOptionsTest {

  @Test
  void testChatOptions() {
    var options = DashscopeChatOptions.builder()
        .withModel(DashscopeModelName.QWEN_TURBO)
        .withTemperature(0.2f)
        .build();
    assertEquals(DashscopeModelName.QWEN_TURBO, options.getModel());
    assertEquals(0.2f, options.getTemperature());
    var updated = new DashscopeChatOptions();
    updated = ModelOptionsUtils.merge(options, updated,
        DashscopeChatOptions.class);
    updated.setTemperature(0.7f);
    assertEquals(DashscopeModelName.QWEN_TURBO, updated.getModel());
    assertEquals(0.7f, updated.getTemperature());
  }
}