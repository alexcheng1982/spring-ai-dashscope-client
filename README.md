# Aliyun Dashscope Spring AI Model

[English](./README.md) | [中文](./README_zh_CN.md)

Aliyun Dashscope Model for Spring AI

[![build](https://github.com/JavaAIDev/spring-ai-dashscope-client/actions/workflows/build.yaml/badge.svg)](https://github.com/JavaAIDev/spring-ai-dashscope-client/actions/workflows/build.yaml)

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.alexcheng1982/spring-ai-dashscope-client)](https://central.sonatype.com/artifact/io.github.alexcheng1982/spring-ai-dashscope-client)

## Versions

| Client/Model version | Spring AI version |
|----------------------|-------------------|
| `1.1.x`              | `0.8.1`           |
| `>= 1.3.x`           | `1.0.0`           |

> An Aliyun Dashscope API key is required. The key is set as environment
> variable `DASHSCOPE_API_KEY`.

## Quick start

Add [Maven dependency](https://central.sonatype.com/artifact/io.github.alexcheng1982/spring-ai-dashscope-client):

```xml

<dependency>
  <groupId>io.github.alexcheng1982</groupId>
  <artifactId>spring-ai-dashscope-client</artifactId>
  <version>VERSION</version>
</dependency>
```

Usage:

```java
var model = DashscopeChatModel.createDefault();
var response = model.call("hello");
```

## Features

* `ChatModel`
* `StreamingChatModel`
* `EmbeddingModel`
* Function calling
* Multimodal input with images and audios

## Spring Boot Starter

Add Spring Boot starter:

```xml

<dependency>
  <groupId>io.github.alexcheng1982</groupId>
  <artifactId>spring-ai-dashscope-spring-boot-starter</artifactId>
  <version>VERSION</version>
</dependency>
```

This will create a `ChatModel` bean and an `EmbeddingModel` bean.
Default `ChatOptions` can be configured
with the configuration key `spring.ai.dashscope.chat.options`.
Default `EmbeddingOptions` can be configured with the configuration
key `spring.ai.dashscope.embedding.options`.

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

See [example](./example) for reference.