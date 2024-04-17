package io.github.alexcheng1982.springai.dashscope;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alexcheng1982.springai.dashscope.api.DashscopeApi;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeModelName;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.Prompt;

class DashscopeChatOptionsTest {

  @Test
  void testChatOptions() {
    var options = DashscopeChatOptions.builder()
        .withModel(DashscopeModelName.QWEN_MAX)
        .withTemperature(0.2f)
        .build();
    assertEquals(DashscopeModelName.QWEN_MAX, options.getModel());
    assertEquals(0.2f, options.getTemperature());
    options = DashscopeChatOptions.builder(options)
        .withTemperature(0.7f)
        .build();
    assertEquals(0.7f, options.getTemperature());
  }

  @Test
  void testDefaultChatOptions() {
    var chatClient = new DashscopeChatClient(new DashscopeApi(),
        DashscopeChatOptions.builder()
            .withModel(DashscopeModelName.QWEN_MAX)
            .withTemperature(0.1f)
            .build());
    var request = chatClient.createRequest(
        new Prompt("hello", DashscopeChatOptions.builder()
            .withStops(List.of("stop"))
            .build()));
    assertEquals(DashscopeModelName.QWEN_MAX, request.options().getModel());
    assertEquals(0.1f, request.options().getTemperature());
    assertEquals(List.of("stop"), request.options().getStops());
  }
}