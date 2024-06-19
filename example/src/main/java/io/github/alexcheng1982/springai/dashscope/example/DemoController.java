package io.github.alexcheng1982.springai.dashscope.example;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
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
  private final EmbeddingModel embeddingModel;

  public DemoController(ChatModel chatModel,
      EmbeddingModel embeddingModel) {
    this.chatClient = ChatClient.create(chatModel);
    this.embeddingModel = embeddingModel;
  }

  @PostMapping("/chat")
  public ChatResponse chat(@RequestBody ChatRequest request) {
    return new ChatResponse(
        chatClient.prompt().user(request.input()).call().content());
  }

  @PostMapping("/embed")
  public EmbeddingResponse embed(@RequestBody EmbeddingRequest request) {
    return new EmbeddingResponse(embeddingModel.embed(request.input()));
  }
}
