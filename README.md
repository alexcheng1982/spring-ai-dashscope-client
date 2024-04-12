# Aliyun Dashscope Spring AI Client

Aliyun Dashscope Client for Spring AI

[![build](https://github.com/JavaAIDev/spring-ai-dashscope-client/actions/workflows/build.yaml/badge.svg)](https://github.com/JavaAIDev/spring-ai-dashscope-client/actions/workflows/build.yaml)

![Maven Central Version](https://img.shields.io/maven-central/v/io.github.alexcheng1982/spring-ai-dashscope-client)


> A Aliyun Dashscope API key is required. The key is set as environment
> variable `DASHSCOPE_API_KEY`.

Usage:

```java
var client = DashscopeChatClient.createDefault();
var response = client.call("hello");
```