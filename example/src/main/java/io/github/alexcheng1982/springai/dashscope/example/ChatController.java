package io.github.alexcheng1982.springai.dashscope.example;

import org.springframework.ai.chat.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {
  
  public record Request(String input) {

  }

  public record Response(String output) {

  }

  private final ChatClient chatClient;

  public ChatController(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  @PostMapping("/chat")
  public Response chat(@RequestBody Request request) {
    return new Response(chatClient.call(request.input()));
  }
}
