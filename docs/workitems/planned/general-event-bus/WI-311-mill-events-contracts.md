# WI-311 — Mill event bus core contracts

| Field | Value |
|--------|--------|
| **Story** | [`general-event-bus`](STORY.md) |
| **Status** | `planned` |
| **Type** | `feature` |
| **Area** | `platform`, `core` |
| **Depends on** | — |

## Tracker

- [ ] Gradle module `core/mill-events` registered in `settings.gradle.kts`
- [ ] `EventType`, `MillEvent`, `EventPayload` marker + `TestEventPayload` for unit tests
- [ ] `EventPublisher`, `EventConsumer`, `EventRouter`, `EventTransport` ports
- [ ] `DeliveryMode`: `SYNC`, `ASYNC`
- [ ] `EventTypes` — reserved `EventType.id` constants (catalog; no domain payloads)
- [ ] `EventRouter` — `eventTypeId → consumers` index; dispatch; failure isolation; async hook
- [ ] Test doubles (`RecordingEventPublisher` or equivalent)
- [ ] Unit tests: routing by `type.id`, multi-consumer fan-out, failure isolation, async smoke
- [ ] KDoc on all public API down to parameter level

## Goal

Introduce **`core/mill-events`**: a Spring-free Kotlin library with the Mill application event
abstraction. Routing uses **`event.type.id`** only; payload type is not a routing key.

## Core contracts (locked)

```kotlin
data class EventType(val id: String)

interface EventPayload

data class MillEvent(
    val eventId: String,
    val type: EventType,
    val payload: EventPayload,
    val correlationId: String,
    val partitionKey: String? = null,
    val occurredAt: Instant,
    val schemaVersion: Int = 1,
)

interface EventPublisher {
    fun publish(event: MillEvent)
}

interface EventConsumer {
    fun subscribedEventTypeIds(): Set<String>
    fun onEvent(event: MillEvent)
    fun deliveryMode(): DeliveryMode = DeliveryMode.SYNC
}
```

`EventRouter` is built from `List<EventConsumer>`, indexes by `subscribedEventTypeIds()`, and
**must not** implement `EventConsumer`. Consumer failures are isolated (log + continue), matching
[`MetadataChangeObserverChain`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/MetadataChangeObserver.kt).

## Reserved event type ids (`EventTypes`)

Document and constantize only — **no producers or consumers** in this WI:

- `metadata.entity.created`, `metadata.entity.updated`, `metadata.entity.deleted`
- `metadata.facet.updated`
- `artifact.sql.persisted`
- `chat.turn.completed`

## Acceptance

- `./gradlew :core:mill-events:test` green
- No Spring, JPA, or Kafka dependencies on compile classpath
- `publishArtifacts = true`

## Non-goals

- Transport implementations (WI-312)
- Spring autoconfigure (WI-313)
- Domain `EventPayload` types in production modules
