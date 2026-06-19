# General event bus (Mill-wide)

**Status:** Implemented (foundation) — delivered by
[`general-event-bus`](../../workitems/completed/20260619-general-event-bus/STORY.md) (**WI-311**–**WI-314**,
branch `feat/events-bus`). Modules: `:core:mill-events`, `:core:mill-events-autoconfigure`.

## Purpose

Mill needs **decoupled, multicast notifications** across `core/`, `metadata/`, `ai/`, and
`persistence`: producers emit lifecycle facts; multiple independent consumers react without
compile-time coupling. Today teams use **Spring `ApplicationEventPublisher`**, **direct callbacks**,
or **orchestrator** beans. The Mill event bus makes **contracts**, **routing**, **delivery mode**,
and **transport** explicit and swappable.

**Interim (until foundation ships):** Spring application events + `@TransactionalEventListener` remain
valid; they do not replace the documented Mill contract for cross-cutting domains.

## Architecture

### Layered responsibilities

```text
Domain module
    EventPublisher.publish(MillEvent)     ← returns immediately (non-blocking)
            │
            ▼
    EventTransport.publish(event)         ← accept / enqueue; does not wait for handlers
            │   in-memory | spring | kafka | rabbitmq | …
            ▼
    [optional broker / Spring event bus]
            │
            ▼
    EventDispatcher                       ← async hand-off to worker (default)
            │
            ▼
    EventRouter.dispatch(event)           ← always in-process on this JVM
            │   multicast: type.id → List<handler>
            ▼
    per-type handlers (EventSubscription) ← run on dispatch / executor threads
```

| Component | Responsibility | Broker-aware? |
|-----------|----------------|---------------|
| **`EventPublisher`** | Domain-facing emit API; **does not block** on handler completion | No |
| **`EventTransport`** | Accept event and hand off to dispatcher/broker; **returns before handlers run** | **Yes** |
| **`EventDispatcher`** | Queues `EventRouter.dispatch` on a worker thread (in-memory/Spring); broker transports use native async | No |
| **`EventRouter`** | In-process **multicast** to handlers subscribed by `event.type.id` | No |
| **Handlers / subscriptions** | Side effects (index, metrics, relations, …) | No |

Producers and consumers depend on **ports** in `:core:mill-events` only — never on Kafka, Spring, or
Rabbit APIs directly.

### Modules

| Module | Role |
|--------|------|
| `:core:mill-events` | Spring-free contracts: `MillEvent`, ports, `EventRouter`, in-memory transport |
| `:core:mill-events-autoconfigure` | Spring wiring, `mill.events.*` properties, transport bean selection |
| `:core:mill-events-kafka` (future) | Kafka transport + listener ingress → `EventRouter` |
| `:core:mill-events-amqp` (future) | RabbitMQ / AMQP transport (if needed) |

Broker modules are **add-ons**; `mill-events` core stays broker-free on the compile classpath.

## Event envelope

Events use a **loosely typed envelope** — not a sealed hierarchy at the bus boundary (contrast:
`AgentEvent` in `:ai:mill-ai`).

```kotlin
data class EventType(val id: String)

interface EventPayload   // marker; concrete DTOs per domain event in follow-on stories

data class MillEvent(
    val eventId: String,
    val type: EventType,
    val payload: EventPayload,
    val correlationId: String,
    val partitionKey: String? = null,   // optional per-key ordering (broker partitions)
    val occurredAt: Instant,
    val schemaVersion: Int = 1,
)
```

| Aspect | Rule |
|--------|------|
| **Routing key** | `event.type.id` only — payload class is **not** used for routing |
| **Payload typing** | Convention per `EventType.id`; producers and consumers agree on DTOs; optional cast helpers |
| **Evolution** | `schemaVersion` on envelope; broker payloads must be serializable in distributed transport |

## Multicast

One published event is delivered to **every** handler whose subscription includes `event.type.id`.
Handlers are **independent**:

- Multiple handlers may subscribe to the same type (fan-out).
- A failing handler is logged and **does not** block siblings (same pattern as
  [`MetadataChangeObserverChain`](../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/MetadataChangeObserver.kt)).

```text
publish(MillEvent(type = metadata.entity.updated, …))
    ├── SearchIndexHandler
    ├── CacheInvalidationHandler
    └── MetricsHandler
```

There is **no** guaranteed order across handlers unless a future policy adds it (e.g. ordered
execution per `partitionKey`).

## Consumer subscriptions

Consumers register **per event type** at subscribe time — not a single `onEvent` with internal
`when (type.id)`.

```kotlin
fun interface EventHandler {
    fun onEvent(event: MillEvent)
}

data class EventSubscription(
    val type: EventType,
    val handler: EventHandler,
    val processing: ProcessingMode = ProcessingMode.ASYNC,
)

class EventRouter(subscriptions: List<EventSubscription>) {
    // indexes type.id → handlers; dispatch with failure isolation
}
```

Spring modules contribute `@Bean EventConsumer` beans built via a small DSL:

```kotlin
@Bean
fun searchIndexConsumer() = eventConsumer {
    on(EventTypes.METADATA_ENTITY_UPDATED) { event -> indexEntity(event) }
    on(EventTypes.METADATA_ENTITY_CREATED) { event -> indexEntity(event) }
}
```

`eventConsumer { … }` expands to one or more `EventSubscription` entries for the router. Optional
typed helpers may cast `payload` when `type.id` is known (follow-on stories).

`EventRouter` **must not** implement `EventConsumer`.

## Publish vs processing (two independent axes)

Publish and handler execution are **separate choices**. Both async and sync (or pseudo-sync) must be
supported on each axis.

```text
                    PROCESSING (per subscription)
                    ASYNC          SYNC           PSEUDO_SYNC (AFTER_COMMIT)
PUBLISH   ASYNC     default        async pub +    Spring: handler after
(default)                          sync handler   TX commit; pub still async
          SYNC      rare: wait     wait through   Spring: sync pub +
                    until          SYNC handlers  AFTER_COMMIT handler
                    dispatch       on worker
                    enqueued
```

### Publish axis (`PublishMode`)

Controls whether the **caller thread** waits after handing the event to the transport.

| Mode | Default? | Behavior |
|------|----------|----------|
| `ASYNC` | **Yes** | `publish()` returns once the transport **accepts** the event (enqueue, Spring publish, outbox insert). Does **not** wait for handlers. |
| `SYNC` | No | `publish()` blocks until **dispatch completes** for all **SYNC** handlers on that event; **ASYNC** handlers are submitted (started) before return unless `publishSyncAwaitAsync=true` (tests only). |

```kotlin
interface EventPublisher {
    fun publish(event: MillEvent)
    fun publish(event: MillEvent, options: PublishOptions)
}

data class PublishOptions(
    val publishMode: PublishMode = PublishMode.ASYNC,
)
```

Global default: `mill.events.publish.mode=async`. Per-call `PublishOptions` overrides for rare cases
(tests, critical path acknowledgment).

### Processing axis (`ProcessingMode`, per subscription)

Controls **how a handler runs** once the router dispatches — independent of publish mode.

| Mode | Default? | Behavior |
|------|----------|----------|
| `ASYNC` | **Yes** | Handler submitted to an executor; parallel fan-out; does not block dispatch worker or producer. |
| `SYNC` | No | Handler runs **to completion** on the dispatch worker before the next **SYNC** handler for that event. Combine with **async publish** for “decoupled emit, ordered synchronous processing”. |
| `AFTER_COMMIT` | No | **Pseudo-sync** — Spring transport only; `@TransactionalEventListener(AFTER_COMMIT)` runs handler after the publishing transaction commits. In-memory transport treats as `SYNC` on dispatch (documented limitation). |

```kotlin
enum class ProcessingMode { ASYNC, SYNC, AFTER_COMMIT }

data class EventSubscription(
    val type: EventType,
    val handler: EventHandler,
    val processing: ProcessingMode = ProcessingMode.ASYNC,
)
```

Example — **async publish, sync process** (common for “don’t block API, but process this handler in order”):

```kotlin
eventConsumer {
    on(EventTypes.METADATA_ENTITY_UPDATED, ProcessingMode.SYNC) { event ->
        updateCache(event)   // runs to completion on dispatch worker
    }
    on(EventTypes.METADATA_ENTITY_UPDATED, ProcessingMode.ASYNC) { event ->
        indexAsync(event)    // parallel on executor
    }
}
```

### Typical combinations

| Publish | Processing | Use when |
|---------|------------|----------|
| ASYNC | ASYNC | Default — indexing, metrics, notifications |
| ASYNC | SYNC | API must return fast; handler must finish before next SYNC handler on same event |
| ASYNC | AFTER_COMMIT | Emit in TX; side effect only after successful commit (Spring) |
| SYNC | SYNC | Tests, or caller must know SYNC handlers finished before continuing |
| SYNC | ASYNC | Rare — dispatch confirmed; handlers still background |

### Dispatch plumbing

```text
producer thread                dispatch / worker thread(s)
     │                                    │
     │  publish(event)  [ASYNC]           │
     │──────── enqueue ──────────────────►│ router.dispatch(event)
     │  return immediately                │   ├── SYNC handler (runs to completion here)
     │                                    │   ├── AFTER_COMMIT → Spring schedules post-commit
     │                                    │   └── ASYNC handler → executor.submit(...)
```

| Rule | Detail |
|------|--------|
| **Default** | `PublishMode.ASYNC` + `ProcessingMode.ASYNC` |
| **Dispatch queue** | In-memory / Spring transports enqueue via `EventDispatcher` when publish is async |
| **Handler isolation** | Failing handler logged; does not block siblings or producer (unless publish SYNC awaiting that handler) |
| **Brokers** | Kafka/AMQP publish is always async from caller; processing sync only within consuming JVM |

`mill.events.async.enabled=false` forces direct/same-thread dispatch for **tests** only.

## Transport plane (`EventTransport`)

`EventTransport` is the **messaging-plane adapter**. Domain code calls `EventPublisher`; autoconfigure
selects the transport implementation.

```kotlin
interface EventTransport {
    fun publish(event: MillEvent)
}
```

### Foundation transports (WI-312)

| `mill.events.transport` | Implementation | When |
|-------------------------|----------------|------|
| `in-memory` (default) | `InMemoryEventTransport` | Tests, single JVM; `publish` enqueues → `EventDispatcher` → `EventRouter.dispatch` |
| `spring` | `SpringEventTransport` | Bridge to `ApplicationEventPublisher`; listener enqueues → `EventRouter`; `AFTER_COMMIT` for publish-after-commit |

### Scale path (follow-on: `distributed-event-transport`)

```text
in-memory  →  spring  →  transactional outbox  →  Kafka / AMQP
```

| Backend | Publish path | Consume path |
|---------|--------------|--------------|
| **In-memory** | enqueue → `EventDispatcher` → `router.dispatch` (non-blocking `publish`) | handlers on worker / executor threads |
| **Spring events** | `ApplicationEventPublisher` (returns immediately) → listener enqueues → `router.dispatch` | `@EventListener` / `@TransactionalEventListener` → dispatcher → handlers |
| **Kafka** | serialize → produce or outbox insert (returns after accept) | `@KafkaListener` → deserialize → `router.dispatch` |
| **RabbitMQ / AMQP** | serialize → send (returns after accept) | `@RabbitListener` → `router.dispatch` |

**Important:** `EventRouter` is **always local to the JVM**. Kafka/AMQP scale **between** instances;
each instance runs its own router and handler set. Cross-cluster multicast is a **product/design**
choice (consumer groups, competing consumers, idempotency).

Distributed transport requires (follow-on):

- Serializable `EventPayload` DTOs (JSON/Avro)
- Outbox + relay for transactional publish
- At-least-once delivery → **idempotent** handlers (`eventId` dedup)
- `partitionKey` → Kafka partition / ordering semantics

## Configuration (foundation)

| Property | Values | Default |
|----------|--------|---------|
| `mill.events.transport` | `in-memory`, `spring` | `in-memory` |
| `mill.events.publish.mode` | `async`, `sync` | `async` |
| `mill.events.async.enabled` | `true`, `false` | `true` (executor dispatch; `false` = test direct dispatch) |

Properties class: Java `@ConfigurationProperties` in `mill-events-autoconfigure` per repo conventions.

## Reserved event type catalog

Constants in `EventTypes` — **no producers or consumers** in the foundation story:

| `EventType.id` | Future producer | Future consumer |
|----------------|-----------------|-----------------|
| `metadata.entity.created` | Metadata bridge | Search index |
| `metadata.entity.updated` | Metadata bridge | Search index |
| `metadata.entity.deleted` | Metadata bridge | Search index |
| `metadata.facet.updated` | Metadata bridge | Search index |
| `artifact.sql.persisted` | AI persistence projector | SQL→schema relations |
| `chat.turn.completed` | Chat runtime | Chat search index |

Domain `EventPayload` types ship in **`event-bus-domain-producers`**; side workers in
**`event-bus-consumers`**. See [`event-bus-follow-ons.md`](event-bus-follow-ons.md).

## Spring wiring (foundation)

```kotlin
@Bean
fun eventRouter(consumers: List<EventConsumer>): EventRouter =
    EventRouter(consumers.flatMap { it.subscriptions() })

@Bean
fun eventPublisher(transport: EventTransport): EventPublisher =
    DefaultEventPublisher(transport)
```

Any module may add `@Bean EventConsumer` in future stories; router and transport code unchanged.

## Candidate use cases

| Domain | Scenario | Notes |
|--------|----------|--------|
| **Value mapping** | Long-running refresh: begin, progress, complete; persist state, emit metrics | Until bus ships: callbacks on `syncFromSource` or thin orchestrator ([`WI-184`](../../workitems/completed/20260417-value-mapping-facets-vector-lifecycle/WI-184-value-mapping-refresh-state-persistence.md)) |
| **Metadata** | Entity/facet changes → cache invalidation, search index, downstream sync | Metadata bridge in follow-on story |
| **AI artifacts** | SQL artifact persisted → schema relation extraction | `artifact.sql.persisted` |
| **Observability** | Micrometer / structured logs with `correlationId` | Align with [`value-mapping-observability-actions.md`](../ai/value-mapping-observability-actions.md) |

## Foundation scope vs follow-ons

**In scope (WI-311**–**WI-314):** contracts, router, in-memory + Spring transports, autoconfigure,
test stub consumers — **no** production domain producers or real side workers.

**Out of scope (follow-on stories):**

| Story | Content |
|-------|---------|
| `event-bus-domain-producers` | Metadata bridge, artifact hooks, domain `EventPayload` types |
| `event-bus-consumers` | Search index, SQL→schema relations, value-mapping refresh |
| `global-search-api` | `GET /api/v1/search` |
| `distributed-event-transport` | Outbox, Kafka, optional AMQP modules |

## Non-goals (foundation)

- Distributed broker in foundation WI — in-process first; bridge via `EventTransport` later
- Guaranteed delivery across restarts — operational concern for distributed transport
- Strict sealed typing at bus boundary — domain DTOs per `EventType.id` by convention
- Changes to `mill-metadata-*`, `mill-ai-*`, or `mill-service` production wiring in foundation story

## Related documents

- [`COLDSTART.md`](../../workitems/completed/20260619-general-event-bus/COLDSTART.md) — implementer setup (branch, Gradle, file map)
- [`event-bus-follow-ons.md`](event-bus-follow-ons.md) — deferred stories outline
- [`general-event-bus` story](../../workitems/completed/20260619-general-event-bus/STORY.md) — WI tracking
- [`mill-configuration.md`](mill-configuration.md) — platform configuration map
- [`CONFIGURATION_INVENTORY.md`](CONFIGURATION_INVENTORY.md) — Spring beans and properties
