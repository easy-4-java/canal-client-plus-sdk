# canal-client-plus-sdk

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![License](https://img.shields.io/badge/license-Apache%202.0-green)

> Spring Boot independent SDK on top of the Alibaba Canal client: typed clients for
> Simple / Cluster / Kafka / Pulsar / RocketMQ / RabbitMQ, message & row-data handlers,
> DML event annotations, and a thread-local context model.

## Table of Contents

- [1. Project Overview](#1-project-overview)
- [2. Features & Status](#2-features--status)
- [3. Requirements & Compatibility](#3-requirements--compatibility)
- [4. Architecture & Modules](#4-architecture--modules)
- [5. Installation](#5-installation)
- [6. Quick Start](#6-quick-start)
- [7. Configuration](#7-configuration)
- [8. Core Usage / API](#8-core-usage--api)
- [9. Testing & Build](#9-testing--build)
- [10. Versioning & Branches](#10-versioning--branches)
- [11. Contributing & License](#11-contributing--license)

## 1. Project Overview

`canal-client-plus-sdk` extends the official
[Alibaba Canal](https://github.com/alibaba/canal) client (`canal.client` 1.1.7) with a
higher-level, convention-friendly consumption model for binlog change events:

- **Typed clients** — `SimpleCanalClient`, `ClusterCanalClient`, and MQ-backed
  `KafkaCanalClient`, `PulsarMQCanalClient`, `RocketMQCanalClient`,
  `RabbitMQCanalClient`, all sharing one fluent `Builder` API.
- **Handlers** — `MessageHandler` (message level), `EntryHandler` (row level:
  `insert` / `update` / `delete`), `RowDataHandler`, with synchronous and
  asynchronous (`Async*`) implementations plus flat-message variants.
- **Annotations** — `@OnCanalEvent` / `@CanalTable` / `@CanalEventHandler` and the
  DML shortcuts `@OnInsertEvent`, `@OnUpdateEvent`, `@OnDeleteEvent`,
  `@OnCreateTableEvent`, `@OnAlertTableEvent`, `@OnDropTableEvent`,
  `@OnCreateIndexEvent`, `@OnDropIndexEvent`, `@OnRenameTableEvent`.
- **Context & models** — `CanalContext` (a `TransmittableThreadLocal`-backed holder)
  and the `CanalModel` describing schema / table / event type / execute time.

The SDK core has no Spring dependency (only the Spring *util* classes used by the
official client are reused); it is a plain library you wire into your own application.

What it is **not**:

- Not the Canal server / deployer — you still need a running Canal server (or MQ
  cluster) as the data source.
- Not a Spring Boot starter — no auto-configuration is provided.

Typical scenarios:

| Scenario | What you use |
| :--- | :--- |
| Consume binlog changes directly from a Canal server | `SimpleCanalClient` / `ClusterCanalClient` |
| Consume changes forwarded to Kafka / Pulsar / RocketMQ / RabbitMQ | `KafkaCanalClient` / `PulsarMQCanalClient` / `RocketMQCanalClient` / `RabbitMQCanalClient` |
| Row-level insert / update / delete dispatch | `EntryHandler` + `SyncMessageHandlerImpl` / `AsyncMessageHandlerImpl` |
| Annotate a POJO event listener per table | `@OnCanalEvent` family + `CanalEventHolder` |
| Propagate the current change context across threads | `CanalContext` + `CanalModel` |

## 2. Features & Status

| Capability | Status | Notes |
| :--- | :--- | :--- |
| Simple / Cluster canal clients | Stable | `SimpleCanalClient.Builder`, `ClusterCanalClient.Builder` |
| Kafka / Pulsar / RocketMQ / RabbitMQ clients | Stable | `KafkaCanalClient.Builder`, `PulsarMQCanalClient.Builder`, `RocketMQCanalClient.Builder`, `RabbitMQCanalClient.Builder` |
| Fluent builder options | Stable | `filter`, `batchSize`, `timeout`, `unit`, `messageHandler`, `setSubscribeTypes` |
| Message / row handlers | Stable | `MessageHandler`, `EntryHandler`, `RowDataHandler` + sync / async, flat / row-data implementations |
| DML event annotations | Stable | `@OnCanalEvent`, `@OnInsertEvent`, `@OnUpdateEvent`, `@OnDeleteEvent`, DDL annotations |
| Thread-local context | Stable | `CanalContext` (TransmittableThreadLocal) + `CanalModel` |
| Row model factories | Stable | `EntryColumnModelFactory` (CanalEntry -> POJO), `MapColumnModelFactory` (-> Map) |

## 3. Requirements & Compatibility

| Requirement | Version |
| :--- | :--- |
| JDK | 17+ |
| Maven | 3.x (Maven Wrapper `./mvnw` is included) |
| Canal server | 1.1.7-compatible (or an MQ cluster for the MQ clients) |

Version lines:

| Branch | JDK | Version |
| :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

## 4. Architecture & Modules

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

Single-module Maven project (`packaging: jar`). No child modules.

| Artifact | Responsibility |
| :--- | :--- |
| `io.github.easy4j:canal-client-plus-sdk` | Canal clients, handlers, annotations, context & models |

Key packages (all under `com.alibaba.otter.canal`):

| Package | Content |
| :--- | :--- |
| `.client` | `CanalClient`, `AbstractCanalClient`, `AbstractClientBuilder`, 6 client implementations |
| `.handler` | `MessageHandler`, `EntryHandler`, `RowDataHandler` + sync / async / flat implementations |
| `.annotation` | `@OnCanalEvent`, `@CanalTable`, `@CanalEventHandler`, DML / DDL event annotations, `CanalEventHolder` |
| `.context` / `.model` | `CanalContext`, `CanalModel` |
| `.factory` | `EntryColumnModelFactory`, `MapColumnModelFactory`, `IModelFactory` |
| `.util` | `CanalUtils`, `RowDataUtil`, `HandlerUtil`, `ThreadUtils`, `GenericUtil` |

## 5. Installation

The project is **not yet published to Maven Central**. Snapshots/releases are
distributed through the Aliyun Maven repository and GitHub Releases.

Maven:

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>canal-client-plus-sdk</artifactId>
    <version>2.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle:

```groovy
implementation 'io.github.easy4j:canal-client-plus-sdk:2.0.x.x.20260630-SNAPSHOT'
```

The official `canal.client` / `canal.protocol` (1.1.7) artifacts are resolved as
regular dependencies.

## 6. Quick Start

Consume changes directly from a Canal server with the simple client:

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
        .batchSize(100)                                  // 100 entries per batch
        .timeout(1L)
        .unit(TimeUnit.SECONDS)
        .messageHandler((destination, message) -> {
            // message is a com.alibaba.otter.canal.protocol.Message
            System.out.println("[" + destination + "] got " + message.getEntries().size() + " entries");
        })
        .build(Collections.singletonList(connector));

client.start();  // blocking consume loop; call client.stop() to terminate
```

Expected result: the client connects to the Canal server, subscribes to binlog
changes for destination `example`, and invokes the `MessageHandler` for every
received `Message`.

## 7. Configuration

The library has no configuration file or property prefix. Consume behaviour is
configured per client through the builder (or the matching setters on
`AbstractCanalClient`):

| Builder method | Default | Description |
| :--- | :--- | :--- |
| `filter(String)` | empty | Canal subscription filter expression |
| `batchSize(Integer)` | `1` | Batch size per `getWithoutAck` |
| `timeout(Long)` | `1` | Timeout for fetching data |
| `unit(TimeUnit)` | `SECONDS` | Time unit of the timeout |
| `setSubscribeTypes(List<EntryType>)` | `[ROWDATA]` | Entry types to subscribe |
| `messageHandler(MessageHandler)` | - | The message consumer callback |
| `build(List<C>)` | - | Build the client from connectors |

Connection-level settings (host / port / destination / credentials, or MQ bootstrap
servers and topics for the MQ clients) belong to the official Canal connectors you
provide to `build(...)`.

## 8. Core Usage / API

### 8.1 Row-level dispatch with `EntryHandler`

Register an `EntryHandler<R>` for a row type; the sync message handler routes
`insert` / `update` / `delete` events to it:

```java
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.handler.impl.SyncMessageHandlerImpl;
import com.alibaba.otter.canal.handler.impl.RowDataHandlerImpl;

public class UserEntryHandler implements EntryHandler<User> {
    @Override
    public void insert(User row)  { /* row created */ }
    @Override
    public void update(User before, User after) { /* row changed */ }
    @Override
    public void delete(User row)  { /* row deleted */ }
}
```

### 8.2 Annotated event listening

Mark a listener class with the DML annotations — e.g. `@OnInsertEvent(schema = "db",
table = "user")` on a method — and use `CanalEventHolder` to bind the target object,
method and annotation metadata. `CanalContext.getModel()` returns the
`CanalModel` (schema, table, `CanalEntry.EventType`, execute time) of the current
event inside the handler thread.

## 9. Testing & Build

```bash
./mvnw clean verify
```

- The build is configured with the JaCoCo Maven plugin (report + `check` goal with a
  90% line-coverage rule bound to the `verify` phase; `haltOnFailure=false`).
- **Assumption**: the 1.0.x branch currently checks in no test sources under
  `src/test`; coverage thresholds are therefore enforced only when tests exist.
- No CI workflow files are present under `.github/` in this worktree.

## 10. Versioning & Branches

| Branch | JDK | Version | Notes |
| :--- | :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` | Current branch, JDK 8 baseline, maintained |
| `feature/2.0.x` | 17 | `2.0.x.*` | JDK 17 line |
| `feature/3.0.x` | 21 | `3.0.x.*` | JDK 21 line |

Maintenance policy: the `1.0.x` line receives bug fixes and compatibility updates
for the JDK 8 baseline. New features targeting newer JDKs land on the `2.0.x` /
`3.0.x` lines. Releases are published to the Aliyun Maven repository and as
GitHub Releases; the project is not yet published to Maven Central.

## 11. Contributing & License

Contributions are welcome — please open issues or pull requests on GitHub.

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
