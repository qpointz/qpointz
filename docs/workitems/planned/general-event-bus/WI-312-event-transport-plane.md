# WI-312 — Event transport plane + properties

| Field | Value |
|--------|--------|
| **Story** | [`general-event-bus`](STORY.md) |
| **Status** | `planned` |
| **Type** | `feature` |
| **Area** | `platform`, `core` |
| **Depends on** | [WI-311](WI-311-mill-events-contracts.md) |

## Tracker

- [ ] `InMemoryEventTransport` — default; `publish` → `EventRouter.dispatch`
- [ ] `SpringEventTransport` — `ApplicationEventPublisher` bridge + listener → `EventRouter`
- [ ] Gradle module `core/mill-events-autoconfigure` skeleton in `settings.gradle.kts`
- [ ] **Java** `MillEventsProperties`: `mill.events.transport`, `mill.events.async.enabled`
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
| `mill.events.async.enabled` | `true`, `false` | `true` |

Properties class must be **Java** (`@ConfigurationProperties`) per repo conventions.

## Transport behavior

| Implementation | When to use |
|----------------|-------------|
| `InMemoryEventTransport` | Default single-JVM; direct router dispatch |
| `SpringEventTransport` | Bridge to Spring application events; supports AFTER_COMMIT listener path for future transactional publish |

## Acceptance

- `./gradlew :core:mill-events:test` and autoconfigure module tests green
- Switching `mill.events.transport` selects the correct transport bean in a slice test

## Non-goals

- Kafka or external broker
- `mill-service` dependency (WI-313)
- Production consumer beans
