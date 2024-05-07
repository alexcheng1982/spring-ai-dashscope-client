package io.github.alexcheng1982.springai.dashscope.autoconfigure;

import io.github.alexcheng1982.springai.dashscope.DashscopeEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(DashscopeEmbeddingProperties.CONFIG_PREFIX)
public class DashscopeEmbeddingProperties {

  public static final String CONFIG_PREFIX = "spring.ai.dashscope.embedding";

  private boolean enabled = true;

  @NestedConfigurationProperty
  private DashscopeEmbeddingOptions options = DashscopeEmbeddingOptions.builder()
      .withModel(DashscopeEmbeddingOptions.DEFAULT_MODEL).build();

  public DashscopeEmbeddingOptions getOptions() {
    return this.options;
  }

  public void setOptions(
      DashscopeEmbeddingOptions options) {
    this.options = options;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabled() {
    return this.enabled;
  }
}
