# Cold start — general-event-bus

**Audience:** implementer with no prior context on this story.  
**Branch:** `feat/events-bus` (rebase on `origin/dev` before MR).  
**Design:** [`docs/design/platform/general-event-bus.md`](../../../design/platform/general-event-bus.md)  
**Backlog:** P-38 in [`BACKLOG.md`](../../BACKLOG.md)

## Read order

1. This file (setup + file map + verify commands)
2. [`STORY.md`](STORY.md) — constraints and WI order
3. [`general-event-bus.md`](../../../design/platform/general-event-bus.md) — architecture
4. Current WI file — implement one WI, check boxes in `STORY.md`, commit, next WI

## Story exit criteria (all four WIs)

```bash
./gradlew :core:mill-events:test
./gradlew :core:mill-events-autoconfigure:test
./gradlew :core:mill-events-autoconfigure:testIT
```

No Spring/Kafka on `:core:mill-events` compile classpath. No production `EventConsumer` beans outside
`mill-events-autoconfigure` testIT. No wiring in `mill-service`, `mill-metadata-*`, or `mill-ai-*`.

## Gradle registration

Add to [`settings.gradle.kts`](../../../../settings.gradle.kts) (with other `:core:*` includes):

```kotlin
include(":core:mill-events")
include(":core:mill-events-autoconfigure")
```

Add dokka deps in [`core/build.gradle.kts`](../../../../core/build.gradle.kts):

```kotlin
dokka(project(":core:mill-events"))
dokka(project(":core:mill-events-autoconfigure"))
```

**Reference modules:** pure Kotlin core → [`metadata/mill-metadata-core`](../../../../metadata/mill-metadata-core); autoconfigure + testIT → [`ai/mill-ai-persistence`](../../../../ai/mill-ai-persistence), [`metadata/mill-metadata-autoconfigure`](../../../../metadata/mill-metadata-autoconfigure).

## Package layout

| Module | Base package |
|--------|----------------|
| `mill-events` | `io.qpointz.mill.events` |
| `mill-events-autoconfigure` | `io.qpointz.mill.events.configuration` (Kotlin); `MillEventsProperties` in **Java** same package |

## Module skeleton

### WI-311 — `mill-events` module

```
core/mill-events/
  build.gradle.kts
  src/main/kotlin/io/qpointz/mill/events/
    model/          EventType, EventPayload, MillEvent, PublishMode, PublishOptions, ProcessingMode
    api/            EventPublisher, EventTransport, EventDispatcher, EventHandler, EventConsumer
    router/         EventSubscription, EventRouter
    dispatch/       DirectEventDispatcher, ExecutorEventDispatcher
    transport/      InMemoryEventTransport          # WI-312
    publisher/      DefaultEventPublisher
    catalog/        EventTypes
    dsl/            EventConsumerDsl.kt             # eventConsumer { on(...) { } }
  src/test/kotlin/io/qpointz/mill/events/
    router/EventRouterTest.kt
    dispatch/...Test.kt
    testkit/        RecordingEventPublisher, TestEventPayload
```

`build.gradle.kts` pattern: copy [`metadata/mill-metadata-core/build.gradle.kts`](../../../../metadata/mill-metadata-core/build.gradle.kts) — Kotlin + `io.qpointz.plugins.mill`, **no** Spring plugins. `mill { publishArtifacts = true }`. Dependencies: `libs.bundles.logging`, test: JUnit 5, AssertJ, Mockito.

### WI-312 / WI-313 — `mill-events-autoconfigure` module

```
core/mill-events-autoconfigure/
  build.gradle.kts
  src/main/java/io/qpointz/mill/events/configuration/
    MillEventsProperties.java
  src/main/kotlin/io/qpointz/mill/events/configuration/
    MillEventsAutoConfiguration.kt
    SpringEventTransport.kt
    MillEventSpringListener.kt          # @EventListener / @TransactionalEventListener bridge
    MillEventPublished.kt               # Spring ApplicationEvent wrapper holding MillEvent
  src/main/resources/META-INF/spring/
    org.springframework.boot.autoconfigure.AutoConfiguration.imports
  src/test/kotlin/...                   # unit/slice tests for transports
  src/testIT/kotlin/io/qpointz/mill/events/configuration/
    MillEventsTestApplication.kt        # @SpringBootConfiguration minimal app
    MillEventsAutoConfigurationIT.kt
    StubEventConsumersConfiguration.kt  # two @Bean eventConsumer stubs
```

`build.gradle.kts`: copy [`metadata/mill-metadata-autoconfigure`](../../../../metadata/mill-metadata-autoconfigure/build.gradle.kts) — `api(project(":core:mill-events"))`, `implementation(libs.boot.starter)`, `annotationProcessor(libs.boot.configuration.processor)`. Register **testIT** suite per [`ai/mill-ai-persistence/build.gradle.kts`](../../../../ai/mill-ai-persistence/build.gradle.kts).

`AutoConfiguration.imports` single line:

```text
io.qpointz.mill.events.configuration.MillEventsAutoConfiguration
```

## Class responsibilities (implement exactly once)

| Class | Module | Role |
|-------|--------|------|
| `DefaultEventPublisher` | mill-events | Implements `EventPublisher`; merges per-call `PublishOptions` with default from properties (autoconfigure passes default) |
| `EventRouter` | mill-events | Build `Map<String, List<EventSubscription>>` at construct; `dispatch` multicasts; SYNC handlers run to completion on dispatch thread; ASYNC `executor.submit`; catch/log per handler |
| `ExecutorEventDispatcher` | mill-events | `PublishMode.ASYNC` → submit `router.dispatch` on executor; `SYNC` → run dispatch on caller thread and wait for SYNC handlers |
| `DirectEventDispatcher` | mill-events | Always inline `router.dispatch` — unit tests only |
| `InMemoryEventTransport` | mill-events | `publish` → `dispatcher.dispatch(event, router, options.publishMode)` |
| `SpringEventTransport` | autoconfigure | `ApplicationEventPublisher.publishEvent(MillEventPublished(event))` |
| `MillEventSpringListener` | autoconfigure | Receives wrapper; routes `AFTER_COMMIT` subscriptions via Spring phase; others → `EventDispatcher` |
| `MillEventsAutoConfiguration` | autoconfigure | Beans: `EventRouter`, `EventPublisher`, `EventTransport`, `EventDispatcher`, `Executor` (if needed) |

## `EventTypes` catalog

Production constants in `EventTypes` (object or class with `val`):

| Constant | `id` |
|----------|------|
| `METADATA_ENTITY_CREATED` | `metadata.entity.created` |
| `METADATA_ENTITY_UPDATED` | `metadata.entity.updated` |
| `METADATA_ENTITY_DELETED` | `metadata.entity.deleted` |
| `METADATA_FACET_UPDATED` | `metadata.facet.updated` |
| `ARTIFACT_SQL_PERSISTED` | `artifact.sql.persisted` |
| `CHAT_TURN_COMPLETED` | `chat.turn.completed` |

Test-only (define in **testIT** or `src/test`, not production catalog):

| Constant | `id` |
|----------|------|
| `TEST_EVENT` | `mill.test.event` |

## Configuration (`MillEventsProperties`)

Prefix: `mill.events`

| Property | Type | Default |
|----------|------|---------|
| `transport` | `in-memory` \| `spring` | `in-memory` |
| `publish.mode` | `async` \| `sync` | `async` |
| `async.enabled` | boolean | `true` |

Map `publish.mode` → `PublishMode`. When `async.enabled=false`, wire `DirectEventDispatcher` for tests.

## Dispatch algorithm (pseudocode)

```text
dispatch(event):
  handlers = index[event.type.id] ?: return
  for sub in handlers:
    if sub.processing == SYNC:
      runCatching { sub.handler.onEvent(event) }.onFailure { log }
    elif sub.processing == ASYNC:
      executor.submit { runCatching { sub.handler.onEvent(event) }.onFailure { log } }
    elif sub.processing == AFTER_COMMIT:
      # in-memory: treat as SYNC on dispatch thread
      # spring: listener registered with AFTER_COMMIT for this subscription
```

`AFTER_COMMIT` in Spring: either register dedicated listeners per subscription at startup, or tag
`MillEventPublished` and use `@TransactionalEventListener` in `MillEventSpringListener` for the
AFTER_COMMIT subset.

## Spring beans (locked)

```kotlin
@Bean
fun eventRouter(consumers: ObjectProvider<List<EventConsumer>>): EventRouter =
    EventRouter(consumers.getIfAvailable { emptyList() }.flatMap { it.subscriptions() })

@Bean
@ConditionalOnMissingBean(EventPublisher::class)
fun eventPublisher(transport: EventTransport, properties: MillEventsProperties): EventPublisher =
    DefaultEventPublisher(transport, properties.toDefaultPublishOptions())

@Bean
@ConditionalOnMissingBean(EventTransport::class)
fun eventTransport(...): EventTransport = /* in-memory or spring from properties */
```

Use `ObjectProvider<List<EventConsumer>>` if empty-list injection is ambiguous in your Spring version.

## testIT scenarios (WI-313)

1. **Fan-out:** two stubs subscribe to `EventTypes.TEST_EVENT`; one publish → both invoked.
2. **Failure isolation:** stub A throws; stub B still invoked.
3. **Async publish:** publish returns before slow ASYNC handler completes (`CountDownLatch`).
4. **Sync process:** stub with `ProcessingMode.SYNC` completes on dispatch worker before publish returns when using `PublishMode.SYNC` (optional assertion).

Stub pattern:

```kotlin
@Configuration
class StubEventConsumersConfiguration {
    @Bean
    fun stubA(recorder: EventRecorder) = eventConsumer {
        on(EventTypes.TEST_EVENT) { recorder.record("A", it) }
    }
}
```

## WI workflow (commits)

| Step | WI | Commit when |
|------|-----|-------------|
| 1 | WI-311 | `:core:mill-events:test` green; check WI-311 + `STORY.md` |
| 2 | WI-312 | transports + properties + autoconfigure skeleton tests green |
| 3 | WI-313 | `testIT` green |
| 4 | WI-314 | docs/backlog/inventory updated; story archived path per `RULES.md` at MR time |

Prefix: `[feat]` for code WIs, `[docs]` for WI-314. One logical commit per WI.

## Common pitfalls

- Putting Spring or Kafka on `mill-events` compile classpath — **forbidden**.
- Making `EventRouter` implement `EventConsumer` — **forbidden** (circular collection).
- Blocking `publish()` on ASYNC handlers by default — use `PublishMode.ASYNC` + dispatcher queue.
- Production domain consumers in foundation — stubs in **testIT only**.
- Kotlin `@ConfigurationProperties` without metadata — use **Java** `MillEventsProperties`.

## Deferred (do not implement in this story)

Domain producers, real indexers, `mill-service` dependency, Kafka module — see
[`event-bus-follow-ons.md`](../../../design/platform/event-bus-follow-ons.md).
