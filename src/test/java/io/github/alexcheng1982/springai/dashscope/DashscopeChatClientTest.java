package io.github.alexcheng1982.springai.dashscope;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

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
}