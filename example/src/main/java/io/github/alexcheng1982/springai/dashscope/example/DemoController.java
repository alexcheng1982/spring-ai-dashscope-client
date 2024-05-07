package io.github.alexcheng1982.springai.dashscope.example;

import java.util.List;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {

  public record ChatRequest(String input) {

  }

  public record ChatResponse(String output) {

  }

  public record EmbeddingRequest(String input) {

  }

  public record EmbeddingResponse(List<Double> result) {

  }

  private final ChatClient chatClient;
  private final EmbeddingClient embeddingClient;

  public DemoController(ChatClient chatClient,
      EmbeddingClient embeddingClient) {
    this.chatClient = chatClient;
    this.embeddingClient = embeddingClient;
  }

  @PostMapping("/chat")
  public ChatResponse chat(@RequestBody ChatRequest request) {
    return new ChatResponse(chatClient.call(request.input()));
  }

  @PostMapping("/embed")
  public EmbeddingResponse embed(@RequestBody EmbeddingRequest request) {
    return new EmbeddingResponse(embeddingClient.embed(request.input()));
  }
}
