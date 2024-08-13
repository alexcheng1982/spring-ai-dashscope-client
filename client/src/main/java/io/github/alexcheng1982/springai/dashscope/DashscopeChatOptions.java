package io.github.alexcheng1982.springai.dashscope;

import com.alibaba.dashscope.tools.ToolBase;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeModelName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallingOptions;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * {@linkplain ChatOptions} of Aliyun Dashscope
 *
 * @see DashscopeChatModel
 */
@JsonInclude(Include.NON_NULL)
public class DashscopeChatOptions implements FunctionCallingOptions,
    ChatOptions {

  public static final String DEFAULT_MODEL = DashscopeModelName.QWEN_TURBO;

  private String model;
  private Float topP;
  private Integer topK;
  private Boolean enableSearch;
  private Integer seed;
  private Float repetitionPenalty;
  private Float temperature;
  private List<String> stops;
  private Integer maxTokens;
  private Boolean incrementalOutput;
  private List<ToolBase> tools;

  private List<FunctionCallback> functionCallbacks = new ArrayList<>();

  private Set<String> functions = new HashSet<>();

  public String getModel() {
    return model;
  }

  @Override
  public Float getFrequencyPenalty() {
    return repetitionPenalty;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public void setTopP(Float topP) {
    this.topP = topP;
  }

  public void setTopK(Integer topK) {
    this.topK = topK;
  }

  public Boolean getEnableSearch() {
    return enableSearch;
  }

  public void setEnableSearch(Boolean enableSearch) {
    this.enableSearch = enableSearch;
  }

  public Integer getSeed() {
    return seed;
  }

  public void setSeed(Integer seed) {
    this.seed = seed;
  }

  public Float getRepetitionPenalty() {
    return repetitionPenalty;
  }

  public void setRepetitionPenalty(Float repetitionPenalty) {
    this.repetitionPenalty = repetitionPenalty;
  }

  public void setTemperature(Float temperature) {
    this.temperature = temperature;
  }

  public List<String> getStops() {
    return stops;
  }

  public void setStops(List<String> stops) {
    this.stops = stops;
  }

  public Integer getMaxTokens() {
    return maxTokens;
  }

  @Override
  public Float getPresencePenalty() {
    return null;
  }

  @Override
  public List<String> getStopSequences() {
    return stops;
  }

  public void setMaxTokens(Integer maxTokens) {
    this.maxTokens = maxTokens;
  }

  public Boolean getIncrementalOutput() {
    return incrementalOutput;
  }

  public void setIncrementalOutput(Boolean incrementalOutput) {
    this.incrementalOutput = incrementalOutput;
  }

  @Override
  public Float getTemperature() {
    return this.temperature;
  }

  @Override
  public Float getTopP() {
    return this.topP;
  }

  @Override
  public ChatOptions copy() {
    return createCopy();
  }

  @Override
  public Integer getTopK() {
    return this.topK;
  }

  public List<ToolBase> getTools() {
    return tools;
  }

  public void setTools(List<ToolBase> tools) {
    this.tools = tools;
  }

  @Override
  public List<FunctionCallback> getFunctionCallbacks() {
    return this.functionCallbacks;
  }

  @Override
  public void setFunctionCallbacks(List<FunctionCallback> functionCallbacks) {
    this.functionCallbacks = functionCallbacks;
  }

  @Override
  public Set<String> getFunctions() {
    return this.functions;
  }

  @Override
  public void setFunctions(Set<String> functions) {
    this.functions = functions;
  }

  public DashscopeChatOptions createCopy() {
    DashscopeChatOptions copy = new DashscopeChatOptions();
    copy.setModel(this.getModel());
    copy.setTopP(this.getTopP());
    copy.setTopK(this.getTopK());
    copy.setEnableSearch(this.getEnableSearch());
    copy.setMaxTokens(this.getMaxTokens());
    copy.setIncrementalOutput(this.getIncrementalOutput());
    copy.setTemperature(this.getTemperature());
    copy.setSeed(this.getSeed());
    copy.setRepetitionPenalty(this.getRepetitionPenalty());
    copy.setFunctions(this.getFunctions());
    copy.setFunctionCallbacks(this.getFunctionCallbacks());
    copy.setTools(this.getTools());
    return copy;
  }

  public DashscopeChatOptions copyFrom(DashscopeChatOptions options) {
    var copy = this.createCopy();
    Optional.ofNullable(options.getModel()).ifPresent(copy::setModel);
    Optional.ofNullable(options.getTopP()).ifPresent(copy::setTopP);
    Optional.ofNullable(options.getTopK()).ifPresent(copy::setTopK);
    Optional.ofNullable(options.getEnableSearch())
        .ifPresent(copy::setEnableSearch);
    Optional.ofNullable(options.getMaxTokens()).ifPresent(copy::setMaxTokens);
    Optional.ofNullable(options.getIncrementalOutput())
        .ifPresent(copy::setIncrementalOutput);
    Optional.ofNullable(options.getTemperature())
        .ifPresent(copy::setTemperature);
    Optional.ofNullable(options.getRepetitionPenalty())
        .ifPresent(copy::setRepetitionPenalty);
    Optional.ofNullable(options.getSeed())
        .ifPresent(copy::setSeed);
    if (!CollectionUtils.isEmpty(options.getFunctions())) {
      copy.setFunctions(options.getFunctions());
    }
    return copy;
  }

  public DashscopeChatOptions updateFromChatOptions(ChatOptions chatOptions) {
    if (chatOptions instanceof DashscopeChatOptions dashscopeChatOptions) {
      return this.copyFrom(dashscopeChatOptions);
    } else {
      DashscopeChatOptions updatedOptions = this.createCopy();
      updatedOptions.setTemperature(chatOptions.getTemperature());
      updatedOptions.setTopP(chatOptions.getTopP());
      updatedOptions.setTopK(chatOptions.getTopK());
      return updatedOptions;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    protected DashscopeChatOptions options;

    public Builder() {
      this.options = new DashscopeChatOptions();
    }

    public Builder(DashscopeChatOptions options) {
      this.options = options;
    }

    public Builder withModel(String model) {
      this.options.model = model;
      return this;
    }

    public Builder withRepetitionPenalty(Float repetitionPenalty) {
      this.options.repetitionPenalty = repetitionPenalty;
      return this;
    }

    public Builder withEnableSearch(
        Boolean enableSearch) {
      this.options.enableSearch = enableSearch;
      return this;
    }

    public Builder withMaxTokens(Integer maxTokens) {
      this.options.maxTokens = maxTokens;
      return this;
    }

    public Builder withIncrementalOutput(Boolean incrementalOutput) {
      this.options.incrementalOutput = incrementalOutput;
      return this;
    }

    public Builder withTopK(Integer topK) {
      this.options.topK = topK;
      return this;
    }


    public Builder withSeed(Integer seed) {
      this.options.seed = seed;
      return this;
    }

    public Builder withStops(List<String> stops) {
      this.options.stops = stops;
      return this;
    }

    public Builder withTemperature(Float temperature) {
      this.options.temperature = temperature;
      return this;
    }

    public Builder withTopP(Float topP) {
      this.options.topP = topP;
      return this;
    }

    public Builder withTools(List<ToolBase> tools) {
      this.options.tools = tools;
      return this;
    }

    public Builder withFunctionCallbacks(
        List<FunctionCallback> functionCallbacks) {
      this.options.functionCallbacks = functionCallbacks;
      return this;
    }

    public Builder withFunctions(Set<String> functionNames) {
      Assert.notNull(functionNames, "Function names must not be null");
      this.options.functions = functionNames;
      return this;
    }

    public Builder withFunction(String functionName) {
      Assert.hasText(functionName, "Function name must not be empty");
      this.options.functions.add(functionName);
      return this;
    }

    public DashscopeChatOptions build() {
      return this.options;
    }

  }
}
