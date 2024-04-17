package io.github.alexcheng1982.springai.dashscope;

import com.alibaba.dashscope.tools.FunctionDefinition;
import com.alibaba.dashscope.tools.ToolFunction;
import com.alibaba.dashscope.utils.JsonUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeApi;
import io.github.alexcheng1982.springai.dashscope.api.DashscopeModelName;
import java.util.List;
import java.util.function.Function;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.model.function.FunctionCallbackWrapper;

public class FunctionExampleMain {

  record AddRequest(int left, int right) {

  }

  record AddResponse(int result) {

  }

  static class AddFunctionTool implements Function<AddRequest, AddResponse> {

    @Override
    public AddResponse apply(AddRequest addRequest) {
      return new AddResponse(addRequest.left + addRequest.right);
    }
  }

  static class SimpleFunctionCallbackContext extends FunctionCallbackContext {

    private final FunctionCallback functionCallback;

    SimpleFunctionCallbackContext(FunctionCallback functionCallback) {
      this.functionCallback = functionCallback;
    }

    @Override
    public FunctionCallback getFunctionCallback(String beanName,
        String defaultDescription) {
      return functionCallback;
    }
  }

  String runFunction() {
    SchemaGeneratorConfigBuilder configBuilder =
        new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12,
            OptionPreset.PLAIN_JSON);
    SchemaGeneratorConfig config = configBuilder.with(
            Option.EXTRA_OPEN_API_FORMAT_VALUES)
        .without(Option.FLATTENED_ENUMS_FROM_TOSTRING).build();
    SchemaGenerator generator = new SchemaGenerator(config);
    ObjectNode jsonSchema = generator.generateSchema(AddFunctionTool.class);

    FunctionDefinition fd = FunctionDefinition.builder().name("add")
        .description("add two number")
        .parameters(
            JsonUtils.parseString(jsonSchema.toString()).getAsJsonObject())
        .build();
    var options = DashscopeChatOptions.builder()
        .withModel(DashscopeModelName.QWEN_PLUS)
        .withTemperature(0.2f)
        .withTools(List.of(
            ToolFunction.builder()
                .type("function")
                .function(fd)
                .build()
        ))
        .withFunction("add")
        .build();
    var tool = new AddFunctionTool();
    var context = new SimpleFunctionCallbackContext(
        FunctionCallbackWrapper.builder(tool)
            .withName("add")
            .withDescription("add two numbers")
            .build());
    var client = new DashscopeChatClient(new DashscopeApi(), context);
    var response = client.call(new Prompt("add 100 to 200", options));
    return response.getResult().getOutput().getContent();
  }

  public static void main(String[] args) {
    System.out.println(new FunctionExampleMain().runFunction());
  }
}
