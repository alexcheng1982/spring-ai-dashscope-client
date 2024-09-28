package io.github.alexcheng1982.springai.dashscope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.alexcheng1982.springai.dashscope.api.DashscopeModelName;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

class DashscopeChatOptionsTest {

  @Test
  void testChatOptions() {
    var options = DashscopeChatOptions.builder()
        .withModel(DashscopeModelName.QWEN_TURBO)
        .withTemperature(0.2)
        .withFunctions(Set.of("add"))
        .build();
    assertEquals(DashscopeModelName.QWEN_TURBO, options.getModel());
    assertEquals(0.2, options.getTemperature());
    var anotherOptions = DashscopeChatOptions.builder().withTemperature(0.7)
        .build();
    var updated = options.updateFromChatOptions(anotherOptions);
    assertEquals(DashscopeModelName.QWEN_TURBO, updated.getModel());
    assertEquals(0.7, updated.getTemperature());
    assertFalse(CollectionUtils.isEmpty(updated.getFunctions()));
  }
}