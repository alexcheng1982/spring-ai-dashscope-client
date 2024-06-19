# Spring AI 阿里云模型服务灵积（Dashscope）模型

[English](./README.md) | [中文](./README_zh_CN.md)

阿里云模型服务灵积（Dashscope）Spring AI 集成模型

[![build](https://github.com/JavaAIDev/spring-ai-dashscope-client/actions/workflows/build.yaml/badge.svg)](https://github.com/JavaAIDev/spring-ai-dashscope-client/actions/workflows/build.yaml)

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.alexcheng1982/spring-ai-dashscope-client)](https://central.sonatype.com/artifact/io.github.alexcheng1982/spring-ai-dashscope-client)

## 版本

| 客户端/模型版本 | Spring AI 版本 |
|----------|--------------|
| `1.1.x`  | `0.8.1`      |
| `1.2.x`  | `1.0.0`      |

> 需要阿里云模型服务灵积的 API Key，设置为环境变量 `DASHSCOPE_API_KEY`。

## 快速上手

添加 [Maven 依赖](https://central.sonatype.com/artifact/io.github.alexcheng1982/spring-ai-dashscope-client)

```xml

<dependency>
  <groupId>io.github.alexcheng1982</groupId>
  <artifactId>spring-ai-dashscope-client</artifactId>
  <version>VERSION</version>
</dependency>
```

使用：

```java
var model = DashscopeChatModel.createDefault();
var response = model.call("hello");
```

## 功能

* `ChatModel`
* `StreamingChatModel`
* `EmbeddingModel`
* 方法调用
* 多模态输入，图片和音频

## Spring Boot 集成

添加 Spring Boot Starter 的依赖：

```xml

<dependency>
  <groupId>io.github.alexcheng1982</groupId>
  <artifactId>spring-ai-dashscope-spring-boot-starter</artifactId>
  <version>VERSION</version>
</dependency>
```

会自动创建一个 `ChatModel` 类型的 Bean 和一个 `EmbeddingModel` 类型的
Bean。默认的 `ChatOptions`
可以通过配置项 `spring.ai.dashscope.chat.options`
来配置。默认的 `EmbeddingOptions`
可以通过配置项 `spring.ai.dashscope.embedding.options` 来配置。

```yaml
spring:
  ai:
    dashscope:
      chat:
        options:
          model: qwen-plus
          temperature: 0.2
      embedding:
        options:
          model: text-embedding-v2
```

可以参考[代码示例](./example)。