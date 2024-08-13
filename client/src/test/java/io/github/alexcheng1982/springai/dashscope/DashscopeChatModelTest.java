package io.github.alexcheng1982.springai.dashscope;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.util.MimeTypeUtils.IMAGE_JPEG;

import io.github.alexcheng1982.springai.dashscope.api.DashscopeModelName;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.util.MimeType;

/**
 * This test requires a Dashscope API key
 */
@Manual
class DashscopeChatModelTest {

  @Test
  void smokeTest() {
    var model = DashscopeChatModel.createDefault();
    var response = model.call("hello");
    assertNotNull(response);
    System.out.println(response);
  }

  @Test
  void streamSmokeTest() {
    var model = DashscopeChatModel.createDefault();
    var response = model.stream("如何做西红柿炖牛腩？");
    response.toIterable().forEach(System.out::println);
  }

  @Test
  void multiModalImageSmokeTest() throws MalformedURLException {
    var model = DashscopeChatModel.createDefault();
    var prompt = new Prompt(new UserMessage("这是什么?",
        List.of(
            new Media(IMAGE_JPEG,
                URI.create(
                        "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg")
                    .toURL()))),
        DashscopeChatOptions.builder()
            .withModel(DashscopeModelName.QWEN_VL_PLUS)
            .build());
    var response = model.call(prompt);
    System.out.println(response.getResult().getOutput().getContent());
  }

  @Test
  void multiModalImageSmokeTest2() throws MalformedURLException {
    var model = DashscopeChatModel.createDefault();
    var prompt = new Prompt(new UserMessage("用图片中的这些食材，做一道菜",
        List.of(
            new Media(IMAGE_JPEG,
                URI.create(
                        "https://img0.baidu.com/it/u=772327866,3555189679&fm=253&fmt=auto&app=138&f=JPEG")
                    .toURL()))),
        DashscopeChatOptions.builder()
            .withModel(DashscopeModelName.QWEN_VL_PLUS)
            .build());
    var response = model.call(prompt);
    System.out.println(response.getResult().getOutput().getContent());
  }

  @Test
  void multiModalAudioSmokeTest() throws MalformedURLException {
    var model = DashscopeChatModel.createDefault();
    var prompt = new Prompt(new UserMessage("这段音频在说什么?",
        List.of(
            new Media(new MimeType("audio", "wav"),
                URI.create(
                        "https://dashscope.oss-cn-beijing.aliyuncs.com/audios/2channel_16K.wav")
                    .toURL()))),
        DashscopeChatOptions.builder()
            .withModel(DashscopeModelName.QWEN_AUDIO_TURBO)
            .withMaxTokens(100)
            .build());
    var response = model.call(prompt);
    System.out.println(response.getResult().getOutput().getContent());
  }
}