package io.github.alexcheng1982.springai.dashscope;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam.TextType;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeModelName;
import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

public class DashscopeEmbeddingClient implements EmbeddingClient {

  @Override
  public EmbeddingResponse call(EmbeddingRequest request) {
    var builder = TextEmbeddingParam.builder()
        .texts(request.getInstructions());
    if (request.getOptions() instanceof DashscopeEmbeddingOptions options) {
      if (options.getModel() != null) {
        builder.model(options.getModel());
      }
      if (options.getTextType() != null) {
        builder.textType(options.getTextType());
      }
    } else {
      builder.model(DashscopeModelName.TEXT_EMBEDDING_V1)
          .textType(TextType.DOCUMENT);
    }
    TextEmbedding embedding = new TextEmbedding();
    try {
      var result = embedding.call(builder.build());
      return new EmbeddingResponse(result.getOutput().getEmbeddings().stream().map(item ->
          new Embedding(item.getEmbedding(), item.getTextIndex())
      ).toList());
    } catch (NoApiKeyException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Double> embed(Document document) {
    return embed(document.getContent());
  }
}
