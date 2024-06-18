package io.github.alexcheng1982.springai.dashscope;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

@Manual
public class DashscopeEmbeddingModelTest {

  @Test
  void smokeTest() {
    var client = new DashscopeEmbeddingModel();
    var result = client.embed("hello");
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }
}
