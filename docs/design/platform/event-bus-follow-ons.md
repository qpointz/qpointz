# Event bus follow-on stories

Outline for work **after** the [`general-event-bus`](../../workitems/planned/general-event-bus/STORY.md)
foundation (**WI-311**–**WI-314**). Assumes `:core:mill-events` and
`:core:mill-events-autoconfigure` are on `dev`.

## `event-bus-domain-producers`

**Goal:** Emit `MillEvent` from real domain flows.

| Producer | `EventType.id` | Module (likely) |
|----------|----------------|-----------------|
| Metadata change bridge | `metadata.entity.*`, `metadata.facet.updated` | `mill-metadata-autoconfigure` or service layer |
| SQL artifact projector | `artifact.sql.persisted` | `mill-ai-persistence` |
| Chat runtime | `chat.turn.completed` | `mill-ai` |

Deliverables:

- Concrete `EventPayload` DTOs per event type (one DTO per `EventType.id` by convention)
- `EventPublisher.publish(...)` calls at persistence/commit boundaries
- Unit tests with `RecordingEventPublisher` (or equivalent test double)

**Non-goals:** consumer side effects, search API.

## `event-bus-consumers`

**Goal:** Side workers as `@Bean EventConsumer` with per-type subscriptions.

| Consumer | Subscribes to | Outcome |
|----------|---------------|---------|
| Search index | `metadata.entity.*`, `metadata.facet.updated` | Upsert/delete index documents |
| SQL→schema relations | `artifact.sql.persisted` | `relation_record` rows |
| Value-mapping refresh | TBD (new event types) | Persist progress / metrics |

Deliverables:

- `@Bean eventConsumer { on(EventTypes.…) { … } }` per worker
- `testIT` per consumer module (isolated slice)
- Idempotent handlers where transport may redeliver

**Depends on:** `event-bus-domain-producers` (or test publishers in IT).

## `global-search-api`

**Goal:** `GET /api/v1/search` backed by index maintained by search consumer.

**Depends on:** `event-bus-consumers` (search index worker).

## `distributed-event-transport`

**Goal:** Scale beyond single JVM via broker + transactional outbox.

| Phase | Deliverable |
|-------|-------------|
| Outbox | `OutboxEventTransport` — same TX as business write; relay job |
| Kafka | `:core:mill-events-kafka` — produce + `@KafkaListener` → `EventRouter.dispatch` |
| Config | `mill.events.transport=kafka`, broker connection properties |
| Serialization | JSON or Avro for `MillEvent` + payload DTOs |

**Rules (unchanged):**

- Domain still uses `EventPublisher` only
- `EventRouter` stays in-process on each instance
- Handlers remain broker-agnostic

Optional later: `:core:mill-events-amqp` for RabbitMQ if product requires it.

## Backlog references

| # | Item |
|---|------|
| P-39 | Event bus domain producers |
| P-40 | Event bus side consumers |

See [`BACKLOG.md`](../../workitems/BACKLOG.md).
