package io.github.alexcheng1982.springai.dashscope;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.util.MimeTypeUtils.IMAGE_JPEG;

import io.github.alexcheng1982.springai.dashscope.api.DashscopeModelName;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.MimeType;

/**
 * This test requires a Dashscope API key
 */
@Manual
class DashscopeChatClientTest {

  @Test
  void smokeTest() {
    var client = DashscopeChatClient.createDefault();
    var response = client.call("hello");
    assertNotNull(response);
  }

  @Test
  void streamSmokeTest() {
    var client = DashscopeChatClient.createDefault();
    var response = client.stream("如何做西红柿炖牛腩？");
    response.toIterable().forEach(System.out::println);
  }

  @Test
  void multiModalImageSmokeTest() {
    var client = DashscopeChatClient.createDefault();
    var prompt = new Prompt(new UserMessage("这是什么?",
        List.of(
            new Media(IMAGE_JPEG,
                "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg"))),
        DashscopeChatOptions.builder()
            .withModel(DashscopeModelName.QWEN_VL_PLUS)
            .build());
    var response = client.call(prompt);
    System.out.println(response.getResult().getOutput().getContent());
  }

  @Test
  void multiModalAudioSmokeTest() {
    var client = DashscopeChatClient.createDefault();
    var prompt = new Prompt(new UserMessage("这段音频在说什么?",
        List.of(
            new Media(new MimeType("audio", "wav"),
                "https://dashscope.oss-cn-beijing.aliyuncs.com/audios/2channel_16K.wav"))),
        DashscopeChatOptions.builder()
            .withModel(DashscopeModelName.QWEN_AUDIO_TURBO)
            .withMaxTokens(100)
            .build());
    var response = client.call(prompt);
    System.out.println(response.getResult().getOutput().getContent());
  }
}