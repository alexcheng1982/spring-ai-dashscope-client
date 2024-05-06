# Spring AI 阿里云模型服务灵积（Dashscope）客户端

[English](./README.md) | [中文](./README_zh_CN.md)

阿里云模型服务灵积（Dashscope）Spring AI 集成客户端

[![build](https://github.com/JavaAIDev/spring-ai-dashscope-client/actions/workflows/build.yaml/badge.svg)](https://github.com/JavaAIDev/spring-ai-dashscope-client/actions/workflows/build.yaml)

![Maven Central Version](https://img.shields.io/maven-central/v/io.github.alexcheng1982/spring-ai-dashscope-client)


> 需要阿里云模型服务灵积的 API Key，设置为环境变量 `DASHSCOPE_API_KEY`。

## 快速上手

添加 Maven 依赖

```xml

<dependency>
  <groupId>io.github.alexcheng1982</groupId>
  <artifactId>spring-ai-dashscope-client</artifactId>
  <version>VERSION</version>
</dependency>
```

使用：

```java
var client = DashscopeChatClient.createDefault();
var response = client.call("hello");
```

## 功能

* `ChatClient`
* `StreamingChatClient`
* `EmbeddingClient`
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

会自动创建一个 `ChatClient` 类型的 Bean。默认的 `ChatOptions`
可以通过配置项 `spring.ai.dashscope.chat.options` 来配置。

```yaml
spring:
  ai:
    dashscope:
      chat:
        options:
          model: qwen-plus
          temperature: 0.2
```

可以参考[代码示例](./example)。