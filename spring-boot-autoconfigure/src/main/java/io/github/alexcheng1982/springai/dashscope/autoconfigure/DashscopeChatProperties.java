package io.github.alexcheng1982.springai.dashscope.autoconfigure;

import io.github.alexcheng1982.springai.dashscope.DashscopeChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(DashscopeChatProperties.CONFIG_PREFIX)
public class DashscopeChatProperties {

  public static final String CONFIG_PREFIX = "spring.ai.dashscope.chat";

  private boolean enabled = true;

  @NestedConfigurationProperty
  private DashscopeChatOptions options = DashscopeChatOptions.builder()
      .withModel(DashscopeChatOptions.DEFAULT_MODEL).build();

  public DashscopeChatOptions getOptions() {
    return this.options;
  }

  public void setOptions(
      DashscopeChatOptions options) {
    this.options = options;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabled() {
    return this.enabled;
  }
}
