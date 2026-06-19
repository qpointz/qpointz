# WI-312 — Event transport plane + properties

| Field | Value |
|--------|--------|
| **Story** | [`general-event-bus`](STORY.md) |
| **Status** | `planned` |
| **Type** | `feature` |
| **Area** | `platform`, `core` |
| **Depends on** | [WI-311](WI-311-mill-events-contracts.md) |

## Tracker

- [ ] `InMemoryEventTransport` — default; `publish` enqueues via `EventDispatcher` → `EventRouter.dispatch` (non-blocking)
- [ ] `SpringEventTransport` — `ApplicationEventPublisher` bridge + listener → `EventDispatcher` → `EventRouter`
- [ ] Gradle module `core/mill-events-autoconfigure` skeleton in `settings.gradle.kts`
- [ ] **Java** `MillEventsProperties`: `mill.events.transport`, `mill.events.publish.mode`, `mill.events.async.enabled`
- [ ] Transport `@Bean` selection in autoconfigure
- [ ] `META-INF/spring-configuration-metadata.json` generated for properties
- [ ] Unit tests for both transports (in-memory pure; Spring transport with `@SpringBootTest` or slice)

## Goal

Add **pluggable transport implementations** and start **`mill-events-autoconfigure`** with
configuration properties. Domain code publishes via `EventPublisher`; transport is swappable.

## Configuration (locked)

| Property | Values | Default |
|----------|--------|---------|
| `mill.events.transport` | `in-memory`, `spring` | `in-memory` |
| `mill.events.publish.mode` | `async`, `sync` | `async` |
| `mill.events.async.enabled` | `true`, `false` | `true` |

Properties class must be **Java** (`@ConfigurationProperties`) per repo conventions.

## Transport behavior

`EventTransport` is the **messaging-plane adapter** — the only layer that knows the backend
(in-memory, Spring, future Kafka/AMQP). Producers use `EventPublisher`; consumers stay on
`EventRouter` subscriptions. See [`general-event-bus.md`](../../../design/platform/general-event-bus.md).

| Implementation | When to use |
|----------------|-------------|
| `InMemoryEventTransport` | Default single-JVM; `publish` accepts and enqueues; returns before handlers run |
| `SpringEventTransport` | Bridge to `ApplicationEventPublisher`; maps `ProcessingMode.AFTER_COMMIT` → `@TransactionalEventListener(AFTER_COMMIT)`; other modes → dispatcher → router |

## Acceptance

- `./gradlew :core:mill-events:test` and autoconfigure module tests green
- Switching `mill.events.transport` selects the correct transport bean in a slice test
- `publish()` with default `PublishMode.ASYNC` returns before a slow ASYNC handler completes
- `ProcessingMode.SYNC` handler runs to completion on dispatch worker while publish remains async
- `ProcessingMode.AFTER_COMMIT` honored in Spring transport slice test

## Non-goals

- Kafka or external broker
- `mill-service` dependency (WI-313)
- Production consumer beans
