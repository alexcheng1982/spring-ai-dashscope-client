package io.github.alexcheng1982.springai.dashscope.autoconfigure;

import io.github.alexcheng1982.springai.dashscope.DashscopeChatModel;
import io.github.alexcheng1982.springai.dashscope.DashscopeChatOptions;
import io.github.alexcheng1982.springai.dashscope.DashscopeEmbeddingModel;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeApi;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass(DashscopeApi.class)
@EnableConfigurationProperties({DashscopeChatProperties.class,
    DashscopeEmbeddingProperties.class})
public class DashscopeAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public DashscopeApi dashscopeApi() {
    return new DashscopeApi();
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = DashscopeChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
      matchIfMissing = true)
  public DashscopeChatModel dashscopeChatModel(DashscopeApi dashscopeApi,
      DashscopeChatOptions options,
      FunctionCallbackContext functionCallbackContext) {
    return new DashscopeChatModel(dashscopeApi, options,
        functionCallbackContext);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = DashscopeChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
      matchIfMissing = true)
  public DashscopeChatOptions defaultDashscopeChatOptions(
      DashscopeChatProperties properties) {
    return properties.getOptions();
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = DashscopeEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
      matchIfMissing = true)
  public DashscopeEmbeddingModel dashscopeEmbeddingClient(
      DashscopeEmbeddingProperties properties) {
    return new DashscopeEmbeddingModel(properties.getOptions());
  }

  @Bean
  @ConditionalOnMissingBean
  public FunctionCallbackContext springAiFunctionManager(
      ApplicationContext context) {
    FunctionCallbackContext manager = new FunctionCallbackContext();
    manager.setApplicationContext(context);
    return manager;
  }
}
