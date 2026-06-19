# general-event-bus

## Cold start

**New to this story?** Start with [`COLDSTART.md`](COLDSTART.md) — branch setup, Gradle includes,
package map, class inventory, testIT scenarios, and verify commands. No other context required.

## Goal

Ship the **Mill application event bus foundation**: `MillEvent` contract (`EventType.id` + `payload`),
`EventPublisher` / `EventConsumer` ports, `EventRouter`, in-memory + Spring transports, and dynamic
Spring consumer bean registration — **ready for follow-on stories** to add domain producers and side
workers.

**Design baseline:** [`docs/design/platform/general-event-bus.md`](../../../design/platform/general-event-bus.md) (status **Designed** until **WI-314** closure)
**Branch:** `feat/events-bus`

## Scope (this story)

**In scope:** event contract, producer port, consumer port, router, transport plane, Spring
autoconfigure — **no production domain producers or concrete consumer workers**.

**Out of scope (follow-on stories):** metadata bridge, artifact persistence hooks, SQL→relation
consumer, search index consumer, global search API, related-content API.

## Motivation (future consumers — not built here)

| Future trigger | Future consumer reaction | Product outcome |
|----------------|---------------------------|-----------------|
| SQL artifact persisted | Parse SQL → schema refs | Chat ↔ schema relations (`relation_record`) |
| Metadata entity/facet change | Upsert/delete index docs | Global search (`GET /api/v1/search`) |
| Value-mapping refresh | Persist progress state | Observability |

## Constraints (locked)

- **Messaging-plane agnostic** — producers and consumers depend on **ports**, not Kafka/Spring/Rabbit.
  **`EventTransport`** is the only broker-aware layer (in-memory, Spring events, future Kafka/AMQP).
- **Routing key** = `event.type.id` only (payload is not used for routing).
- **Multicast** — one publish delivers to every handler subscribed to that `type.id`; failures are
  isolated (log + continue).
- **Per-type subscriptions** — handlers register with `on(EventType, handler)` (via `eventConsumer { }`
  DSL), not a single `onEvent` with internal `when (type.id)`.
- **Publish vs processing** — two independent axes: `PublishMode` (async default on `publish`) and
  `ProcessingMode` per subscription (async, sync, or `AFTER_COMMIT` pseudo-sync via Spring).
  Typical: **async publish + sync process** when the API must return before heavy work but a handler
  must run to completion in order on the dispatch worker.
- **Non-blocking publish (default)** — `PublishMode.ASYNC`; optional `PublishMode.SYNC` per call or
  via `mill.events.publish.mode=sync`.
- **`EventRouter` is always in-process** — broker transports feed the router on each JVM; router does
  not implement `EventConsumer`.
- **Consumers are dynamic Spring beans** — `List<EventConsumer>` → flattened `EventSubscription`s →
  `EventRouter`.
- **Foundation only** — no changes to `mill-metadata-*`, `mill-ai-*`, or `mill-service` production wiring.

## Modules

| Module | Role |
|--------|------|
| `core/mill-events` | Spring-free contracts: `MillEvent`, `EventPublisher`, `EventConsumer`, `EventRouter`, transports |
| `core/mill-events-autoconfigure` | Spring wiring, `mill.events.*` properties, `testIT` stub consumers |

## Work item order

| Seq | WI | Rationale |
|-----|-----|-----------|
| 1 | WI-311 | Contracts and router must exist before transports and Spring wiring |
| 2 | WI-312 | Transport + properties module skeleton |
| 3 | WI-313 | End-to-end Spring slice with test stub consumers |
| 4 | WI-314 | Design docs, backlog, reserved event type catalog |

## Follow-on stories (deferred)

| Story slug | Content |
|------------|---------|
| `event-bus-domain-producers` | Metadata bridge, artifact projector hooks, domain `EventPayload` types |
| `event-bus-consumers` | Search index, SQL→schema relations, value-mapping refresh |
| `global-search-api` | `GET /api/v1/search` + mill-ui backend |
| `distributed-event-transport` | Kafka + outbox |

## Work Items

- [x] WI-311 — MillEvent + producer/consumer/router contracts (`WI-311-mill-events-contracts.md`) — [COLDSTART § WI-311](COLDSTART.md#wi-311--mill-events-module)
- [x] WI-312 — Transport plane + properties (`WI-312-event-transport-plane.md`) — [COLDSTART § WI-312](COLDSTART.md#wi-312--wi-313--mill-events-autoconfigure-module)
- [x] WI-313 — Spring consumer wiring + testIT stubs (`WI-313-event-router-spring-consumers.md`)
- [x] WI-314 — Design docs, catalog, backlog (`WI-314-event-bus-foundation-docs.md`)

## Verify (story complete)

```bash
./gradlew :core:mill-events:test
./gradlew :core:mill-events-autoconfigure:test
./gradlew :core:mill-events-autoconfigure:testIT
```
