# Aliyun Dashscope Spring AI Client

[English](./README.md) | [中文](./README_zh_CN.md)

Aliyun Dashscope Client for Spring AI

[![build](https://github.com/JavaAIDev/spring-ai-dashscope-client/actions/workflows/build.yaml/badge.svg)](https://github.com/JavaAIDev/spring-ai-dashscope-client/actions/workflows/build.yaml)

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.alexcheng1982/spring-ai-dashscope-client)](https://central.sonatype.com/artifact/io.github.alexcheng1982/spring-ai-dashscope-client)


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
var client = DashscopeChatClient.createDefault();
var response = client.call("hello");
```

## Features

* `ChatClient`
* `StreamingChatClient`
* `EmbeddingClient`
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

This will create a `ChatClient` bean. Default `ChatOptions` can be configured
with the configuration key `spring.ai.dashscope.chat.options`.

```yaml
spring:
  ai:
    dashscope:
      chat:
        options:
          model: qwen-plus
          temperature: 0.2
```

See [example](./example) for reference.