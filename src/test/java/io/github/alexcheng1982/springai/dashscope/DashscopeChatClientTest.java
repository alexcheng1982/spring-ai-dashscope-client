package io.github.alexcheng1982.springai.dashscope;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.util.MimeTypeUtils.IMAGE_JPEG;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation.Models;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

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
  void multiModalSmokeTest() {
    var client = DashscopeChatClient.createDefault();
    var prompt = new Prompt(new UserMessage("这是什么?", List.of(
        new Media(IMAGE_JPEG,
            "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg"))),
        DashscopeChatOptions.builder()
            .withModel(Models.QWEN_VL_PLUS)
            .build());
    var response = client.call(prompt);
    System.out.println(response.getResult().getOutput().getContent());
  }
}