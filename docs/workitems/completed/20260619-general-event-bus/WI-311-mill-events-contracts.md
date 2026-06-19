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
- [ ] `EventPublisher`, `EventTransport`, `EventDispatcher`, `EventHandler`, `EventSubscription`, `EventConsumer`, `EventRouter` ports
- [ ] `PublishMode`: `ASYNC` (default), `SYNC`; `PublishOptions` on `publish(event, options)`
- [ ] `ProcessingMode`: `ASYNC` (default), `SYNC`, `AFTER_COMMIT` (pseudo-sync; noop timing in in-memory, honored by Spring in WI-312)
- [ ] `EventTypes` — reserved `EventType.id` constants (catalog; no domain payloads)
- [ ] `EventRouter` — `type.id → handlers` index; multicast; SYNC runs to completion on dispatch worker; ASYNC on executor
- [ ] `EventDispatcher` port + direct impl for unit tests
- [ ] `eventConsumer { on(EventType, ProcessingMode) { … } }` builder
- [ ] Test doubles (`RecordingEventPublisher` or equivalent)
- [ ] Unit tests: fan-out, failure isolation, **async publish + sync process**, async publish returns before slow ASYNC handler, optional sync publish waits for SYNC handlers
- [ ] KDoc on all public API down to parameter level

## Goal

Introduce **`core/mill-events`**: a Spring-free Kotlin library with the Mill application event
abstraction. Routing uses **`event.type.id`** only. Handlers subscribe **per event type**. **Publish**
and **processing** are independent: default async/async; **async publish + sync process** supported.

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

enum class PublishMode { ASYNC, SYNC }

data class PublishOptions(
    val publishMode: PublishMode = PublishMode.ASYNC,
)

interface EventPublisher {
    fun publish(event: MillEvent)
    fun publish(event: MillEvent, options: PublishOptions)
}

interface EventTransport {
    fun publish(event: MillEvent, options: PublishOptions = PublishOptions())
}

interface EventDispatcher {
    fun dispatch(event: MillEvent, router: EventRouter, publishMode: PublishMode)
}

enum class ProcessingMode { ASYNC, SYNC, AFTER_COMMIT }

fun interface EventHandler {
    fun onEvent(event: MillEvent)
}

data class EventSubscription(
    val type: EventType,
    val handler: EventHandler,
    val processing: ProcessingMode = ProcessingMode.ASYNC,
)

interface EventConsumer {
    fun subscriptions(): List<EventSubscription>
}

class EventRouter(subscriptions: List<EventSubscription>) {
    fun dispatch(event: MillEvent)
}
```

`EventRouter` indexes by `type.id`, **multicasts**, and isolates failures (log + continue).

Example — async publish, mixed processing:

```kotlin
eventPublisher.publish(event)   // ASYNC publish — returns after accept

eventConsumer {
    on(EventTypes.METADATA_ENTITY_UPDATED, ProcessingMode.SYNC) { e -> updateCache(e) }
    on(EventTypes.METADATA_ENTITY_UPDATED, ProcessingMode.ASYNC) { e -> indexAsync(e) }
}
```

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

## Implementation map

Full file tree and class list: [`COLDSTART.md`](COLDSTART.md).

| Deliverable | Location |
|-------------|----------|
| Gradle module | `core/mill-events/`, `settings.gradle.kts` + `core/build.gradle.kts` dokka |
| Packages | `io.qpointz.mill.events.{model,api,router,dispatch,publisher,catalog,dsl}` |
| `DefaultEventPublisher` | `publisher/DefaultEventPublisher.kt` |
| `EventRouter` + tests | `router/EventRouter.kt`, `router/EventRouterTest.kt` |
| `eventConsumer { }` DSL | `dsl/EventConsumerDsl.kt` |
| Test kit | `testkit/RecordingEventPublisher.kt`, `TestEventPayload.kt` |

## Non-goals

- Transport implementations (WI-312)
- Spring autoconfigure (WI-313)
- Domain `EventPayload` types in production modules
