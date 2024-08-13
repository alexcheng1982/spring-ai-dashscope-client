package io.github.alexcheng1982.springai.dashscope;

import com.alibaba.dashscope.embeddings.TextEmbeddingParam.TextType;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeModelName;
import org.springframework.ai.embedding.EmbeddingOptions;

public class DashscopeEmbeddingOptions implements EmbeddingOptions {

  public static final String DEFAULT_MODEL = DashscopeModelName.TEXT_EMBEDDING_V1;

  private String model;

  private TextType textType;

  public String getModel() {
    return model;
  }

  @Override
  public Integer getDimensions() {
    return null;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public TextType getTextType() {
    return textType;
  }

  public void setTextType(TextType textType) {
    this.textType = textType;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(DashscopeEmbeddingOptions options) {
    return new Builder(options);
  }

  public static class Builder {

    private final DashscopeEmbeddingOptions options;

    public Builder() {
      this.options = new DashscopeEmbeddingOptions();
    }

    public Builder(DashscopeEmbeddingOptions options) {
      this.options = options;
    }

    public Builder withModel(String model) {
      this.options.model = model;
      return this;
    }

    public Builder withTextType(TextType textType) {
      this.options.textType = textType;
      return this;
    }

    public DashscopeEmbeddingOptions build() {
      return this.options;
    }
  }
}
