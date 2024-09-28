package io.github.alexcheng1982.springai.dashscope;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam.TextType;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeApiException;
import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

/**
 * Spring AI {@linkplain EmbeddingModel} for Aliyun Dashscope
 */
public class DashscopeEmbeddingModel implements EmbeddingModel {

  private DashscopeEmbeddingOptions defaultOptions;

  public DashscopeEmbeddingModel() {
  }

  public DashscopeEmbeddingModel(DashscopeEmbeddingOptions defaultOptions) {
    this.defaultOptions = defaultOptions;
  }

  @Override
  public EmbeddingResponse call(EmbeddingRequest request) {
    var builder = TextEmbeddingParam.builder()
        .texts(request.getInstructions());
    var options = request.getOptions() instanceof DashscopeEmbeddingOptions
        ? (DashscopeEmbeddingOptions) request.getOptions() : defaultOptions;
    if (options != null) {
      if (options.getModel() != null) {
        builder.model(options.getModel());
      }
      if (options.getTextType() != null) {
        builder.textType(options.getTextType());
      }
    } else {
      builder.model(DashscopeEmbeddingOptions.DEFAULT_MODEL)
          .textType(TextType.DOCUMENT);
    }
    TextEmbedding embedding = new TextEmbedding();
    try {
      var result = embedding.call(builder.build());
      return new EmbeddingResponse(
          result.getOutput().getEmbeddings().stream().map(item ->
              new Embedding(convertEmbeddings(item.getEmbedding()),
                  item.getTextIndex())
          ).toList());
    } catch (ApiException | NoApiKeyException e) {
      throw new DashscopeApiException(e);
    }
  }

  private float[] convertEmbeddings(List<Double> values) {
    int length = values.size();
    float[] result = new float[length];
    int index = 0;
    for (Double value : values) {
      result[index++] = value.floatValue();
    }
    return result;
  }


  @Override
  public float[] embed(Document document) {
    return embed(document.getContent());
  }
}
