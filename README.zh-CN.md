# canal-client-plus-sdk

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![Java](https://img.shields.io/badge/Java-17-orange)] [![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0.txt)

> 基于阿里巴巴 Canal 官方客户端的增强 SDK（与 Spring Boot 解耦）：Simple / Cluster /
> Kafka / Pulsar / RocketMQ / RabbitMQ 类型化客户端、消息与行数据处理器、
> DML 事件注解、以及线程本地上下文模型。

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 功能与状态](#2-功能与状态)
- [3. 环境要求与兼容性](#3-环境要求与兼容性)
- [4. 架构与模块](#4-架构与模块)
- [5. 安装](#5-安装)
- [6. 快速开始](#6-快速开始)
- [7. 配置](#7-配置)
- [8. 核心用法 / API](#8-核心用法--api)
- [9. 测试与构建](#9-测试与构建)
- [10. 版本与分支](#10-版本与分支)
- [11. 贡献与许可](#11-贡献与许可)

## 1. 项目概述

`canal-client-plus-sdk` 在阿里巴巴 [Canal](https://github.com/alibaba/canal) 官方
客户端（`canal.client` 1.1.7）之上，提供更上层、约定友好的 binlog 变更事件消费模型：

- **类型化客户端** — `SimpleCanalClient`、`ClusterCanalClient`，以及基于 MQ 的
  `KafkaCanalClient`、`PulsarMQCanalClient`、`RocketMQCanalClient`、
  `RabbitMQCanalClient`，全部共享同一套流式 `Builder` API。
- **处理器** — `MessageHandler`（消息级）、`EntryHandler`（行级：`insert` /
  `update` / `delete`）、`RowDataHandler`，并提供同步与异步（`Async*`）实现及
  flat 消息变体。
- **注解** — `@OnCanalEvent` / `@CanalTable` / `@CanalEventHandler` 及 DML 快捷注解
  `@OnInsertEvent`、`@OnUpdateEvent`、`@OnDeleteEvent`、`@OnCreateTableEvent`、
  `@OnAlertTableEvent`、`@OnDropTableEvent`、`@OnCreateIndexEvent`、
  `@OnDropIndexEvent`、`@OnRenameTableEvent`。
- **上下文与模型** — `CanalContext`（基于 `TransmittableThreadLocal` 的持有器）与
  描述库 / 表 / 事件类型 / 执行时间的 `CanalModel`。

SDK 核心无 Spring 依赖（仅复用官方客户端用到的 Spring *util* 类），是一个可直接
接入自有应用的纯库。

它不是：

- Canal server / deployer——数据源仍然需要运行中的 Canal server（或 MQ 集群）。
- Spring Boot starter——不提供自动装配。

典型场景：

| 场景 | 使用内容 |
| :--- | :--- |
| 直接从 Canal server 消费 binlog 变更 | `SimpleCanalClient` / `ClusterCanalClient` |
| 消费转发到 Kafka / Pulsar / RocketMQ / RabbitMQ 的变更 | `KafkaCanalClient` / `PulsarMQCanalClient` / `RocketMQCanalClient` / `RabbitMQCanalClient` |
| 行级 insert / update / delete 分发 | `EntryHandler` + `SyncMessageHandlerImpl` / `AsyncMessageHandlerImpl` |
| 按表为 POJO 事件监听器加注解 | `@OnCanalEvent` 系列 + `CanalEventHolder` |
| 跨线程传递当前变更上下文 | `CanalContext` + `CanalModel` |

## 2. 功能与状态

| 能力 | 状态 | 说明 |
| :--- | :--- | :--- |
| Simple / Cluster canal 客户端 | 稳定 | `SimpleCanalClient.Builder`、`ClusterCanalClient.Builder` |
| Kafka / Pulsar / RocketMQ / RabbitMQ 客户端 | 稳定 | `KafkaCanalClient.Builder`、`PulsarMQCanalClient.Builder`、`RocketMQCanalClient.Builder`、`RabbitMQCanalClient.Builder` |
| 流式 Builder 选项 | 稳定 | `filter`、`batchSize`、`timeout`、`unit`、`messageHandler`、`setSubscribeTypes` |
| 消息 / 行处理器 | 稳定 | `MessageHandler`、`EntryHandler`、`RowDataHandler` + 同步 / 异步、flat / 行数据实现 |
| DML 事件注解 | 稳定 | `@OnCanalEvent`、`@OnInsertEvent`、`@OnUpdateEvent`、`@OnDeleteEvent`、DDL 注解 |
| 线程本地上下文 | 稳定 | `CanalContext`（TransmittableThreadLocal）+ `CanalModel` |
| 行模型工厂 | 稳定 | `EntryColumnModelFactory`（CanalEntry → POJO）、`MapColumnModelFactory`（→ Map） |

## 3. 环境要求与兼容性

| 要求 | 版本 |
| :--- | :--- |
| JDK | 17+ |
| Maven | 3.x（项目内置 Maven Wrapper `./mvnw`） |
| Canal server | 兼容 1.1.7（MQ 客户端则需要对应的 MQ 集群） |

版本线：

| 分支 | JDK | 版本 |
| :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

## 4. 架构与模块

```text
+------------------+   +------------------------------------------+
| MySQL binlog     |   | canal-client-plus-sdk                    |
|      |           |   |  client: Simple/Cluster/Kafka/           |
|      v           |   |          Pulsar/RocketMQ/RabbitMQ        |
| Canal server /   |-->|  handler: MessageHandler, EntryHandler,  |
| MQ cluster       |   |          RowDataHandler (sync/async)     |
+------------------+   |  annotation: @OnCanalEvent family        |
                       |  context: CanalContext + CanalModel      |
                       +-------------------+----------------------+
                                           |
                                           v
                     +-------------------------------------------+
                     | Application callbacks (message / row /    |
                     | annotated event listeners)                |
                     +-------------------------------------------+
```

单模块 Maven 工程（`packaging: jar`），无子模块。

| 构件 | 职责 |
| :--- | :--- |
| `io.github.easy4j:canal-client-plus-sdk` | Canal 客户端、处理器、注解、上下文与模型 |

关键包（均在 `com.alibaba.otter.canal` 下）：

| 包 | 内容 |
| :--- | :--- |
| `.client` | `CanalClient`、`AbstractCanalClient`、`AbstractClientBuilder`、6 个客户端实现 |
| `.handler` | `MessageHandler`、`EntryHandler`、`RowDataHandler` + 同步 / 异步 / flat 实现 |
| `.annotation` | `@OnCanalEvent`、`@CanalTable`、`@CanalEventHandler`、DML / DDL 事件注解、`CanalEventHolder` |
| `.context` / `.model` | `CanalContext`、`CanalModel` |
| `.factory` | `EntryColumnModelFactory`、`MapColumnModelFactory`、`IModelFactory` |
| `.util` | `CanalUtils`、`RowDataUtil`、`HandlerUtil`、`ThreadUtils`、`GenericUtil` |

## 5. 安装

项目**尚未发布到 Maven Central**。快照 / 发布版本通过阿里云 Maven 仓库与 GitHub
Releases 分发。

Maven：

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>canal-client-plus-sdk</artifactId>
    <version>2.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle：

```groovy
implementation 'io.github.easy4j:canal-client-plus-sdk:2.0.x.x.20260630-SNAPSHOT'
```

官方 `canal.client` / `canal.protocol`（1.1.7）构件会作为常规依赖被解析。

## 6. 快速开始

使用 simple 客户端直接从 Canal server 消费变更：

```java
import com.alibaba.otter.canal.client.SimpleCanalClient;
import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import com.alibaba.otter.canal.protocol.Message;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

SocketAddress address = new InetSocketAddress("127.0.0.1", 11111);
SimpleCanalConnector connector = new SimpleCanalConnector(address, "example", "", "");

SimpleCanalClient client = new SimpleCanalClient.Builder()
        .batchSize(100)                                  // 每批 100 条
        .timeout(1L)
        .unit(TimeUnit.SECONDS)
        .messageHandler((destination, message) -> {
            // message 为 com.alibaba.otter.canal.protocol.Message
            System.out.println("[" + destination + "] got " + message.getEntries().size() + " entries");
        })
        .build(Collections.singletonList(connector));

client.start();  // 阻塞消费循环；调用 client.stop() 终止
```

预期结果：客户端连接 Canal server，订阅 destination `example` 的 binlog 变更，
每收到一条 `Message` 即回调 `MessageHandler`。

## 7. 配置

本库没有配置文件或属性前缀。消费行为通过 Builder 按客户端配置
（或在 `AbstractCanalClient` 上使用对应 setter）：

| Builder 方法 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `filter(String)` | 空 | Canal 订阅过滤表达式 |
| `batchSize(Integer)` | `1` | 每次 `getWithoutAck` 的批量大小 |
| `timeout(Long)` | `1` | 获取数据的超时时间 |
| `unit(TimeUnit)` | `SECONDS` | 超时时间单位 |
| `setSubscribeTypes(List<EntryType>)` | `[ROWDATA]` | 订阅的 Entry 类型 |
| `messageHandler(MessageHandler)` | - | 消息消费回调 |
| `build(List<C>)` | - | 由连接器构建客户端 |

连接级设置（host / port / destination / 凭据，MQ 客户端则是 bootstrap servers 与
topic）属于你在 `build(...)` 中提供的官方 Canal 连接器。

## 8. 核心用法 / API

### 8.1 使用 `EntryHandler` 做行级分发

为行类型注册 `EntryHandler<R>`，同步消息处理器会把 `insert` / `update` / `delete`
事件路由给它：

```java
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.impl.SyncMessageHandlerImpl;
import com.alibaba.otter.canal.handler.impl.RowDataHandlerImpl;

public class UserEntryHandler implements EntryHandler<User> {
    @Override
    public void insert(User row)  { /* 行被创建 */ }
    @Override
    public void update(User before, User after) { /* 行被修改 */ }
    @Override
    public void delete(User row)  { /* 行被删除 */ }
}
```

### 8.2 注解式事件监听

在方法上标记 DML 注解（如 `@OnInsertEvent(schema = "db", table = "user")`），
并用 `CanalEventHolder` 绑定目标对象、方法与注解元数据。在处理器线程内调用
`CanalContext.getModel()` 可拿到当前事件的 `CanalModel`（库名、表名、
`CanalEntry.EventType`、执行时间）。

## 9. 测试与构建

```bash
./mvnw clean verify
```

- 构建配置了 JaCoCo Maven 插件（报告 + 绑定在 `verify` 阶段的 `check` 目标，
  行覆盖率规则为 90%；`haltOnFailure=false`）。
- **假设**：1.0.x 分支当前 `src/test` 下未提交测试源码；覆盖率门禁仅在存在测试时生效。
- 本 worktree 的 `.github/` 下无 CI 工作流文件。

## 10. 版本与分支

| 分支 | JDK | 版本 | 说明 |
| :--- | :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` | 当前分支，JDK 8 基线，维护中 |
| `feature/2.0.x` | 17 | `2.0.x.*` | JDK 17 版本线 |
| `feature/3.0.x` | 21 | `3.0.x.*` | JDK 21 版本线 |

维护策略：`1.0.x` 版本线接收针对 JDK 8 基线的缺陷修复与兼容性更新；面向新 JDK 的
新特性在 `2.0.x` / `3.0.x` 版本线开发。发布物通过阿里云 Maven 仓库与 GitHub
Releases 分发；项目尚未发布到 Maven Central。

## 11. 贡献与许可

欢迎通过 GitHub Issue 或 Pull Request 参与贡献。

本项目基于 [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt) 许可。
