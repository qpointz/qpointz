# general-event-bus

## Goal

Ship the **Mill application event bus foundation**: `MillEvent` contract (`EventType.id` + `payload`),
`EventPublisher` / `EventConsumer` ports, `EventRouter`, in-memory + Spring transports, and dynamic
Spring consumer bean registration — **ready for follow-on stories** to add domain producers and side
workers.

**Design baseline:** [`docs/design/platform/general-event-bus.md`](../../../design/platform/general-event-bus.md)  
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
- **Routing key** = `event.type.id` only (payload is not used for routing).
- **Consumers are dynamic Spring beans** — `subscribedEventTypeIds()` + `List<EventConsumer>` → `EventRouter`.
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

- [ ] WI-311 — MillEvent + producer/consumer/router contracts (`WI-311-mill-events-contracts.md`)
- [ ] WI-312 — Transport plane + properties (`WI-312-event-transport-plane.md`)
- [ ] WI-313 — Spring consumer wiring + testIT stubs (`WI-313-event-router-spring-consumers.md`)
- [ ] WI-314 — Design docs, catalog, backlog (`WI-314-event-bus-foundation-docs.md`)
