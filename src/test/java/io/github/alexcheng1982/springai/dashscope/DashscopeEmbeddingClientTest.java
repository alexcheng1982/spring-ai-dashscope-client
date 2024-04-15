package io.github.alexcheng1982.springai.dashscope;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

@Manual
public class DashscopeEmbeddingClientTest {

  @Test
  void smokeTest() {
    var client = new DashscopeEmbeddingClient();
    var result = client.embed("hello");
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }
}
