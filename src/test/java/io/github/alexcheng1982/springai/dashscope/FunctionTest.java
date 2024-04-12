package io.github.alexcheng1982.springai.dashscope;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("native-image")
@Manual
public class FunctionTest {

  @Test
  void function() {
    var response = new FunctionExampleMain().runFunction();
    assertNotNull(response);
  }
}
