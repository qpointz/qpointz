# WI-314 — Event bus foundation docs and backlog

| Field | Value |
|--------|--------|
| **Story** | [`general-event-bus`](STORY.md) |
| **Status** | `planned` |
| **Type** | `docs` |
| **Area** | `platform` |
| **Depends on** | [WI-313](WI-313-event-router-spring-consumers.md) |

## Tracker

- [ ] Promote [`general-event-bus.md`](../../../design/platform/general-event-bus.md) — status **Implemented (foundation)** (architecture pre-documented; verify against shipped code)
- [ ] Confirm design covers: two-axis publish/process model, multicast, per-type subscriptions, `EventTransport` as broker adapter, transport scale path
- [ ] [`event-bus-follow-ons.md`](../../../design/platform/event-bus-follow-ons.md) — review / extend if implementation diverged
- [ ] Update [`module-inventory.md`](../../../design/platform/module-inventory.md) — `:core:mill-events`, `:core:mill-events-autoconfigure`
- [ ] Update [`BACKLOG.md`](../../BACKLOG.md) — **P-38** `done`; **P-39**, **P-40** remain planned follow-ons
- [ ] Update [`README.md`](../../../design/platform/README.md) index — status **Implemented (foundation)**
- [ ] Confirm [`COLDSTART.md`](COLDSTART.md) matches shipped packages and verify commands

## Goal

Align design docs and backlog with the shipped foundation so follow-on stories can add domain
producers and consumers without re-deciding the contract.

## BACKLOG rows (locked)

| # | Item | Status after closure |
|---|------|----------------------|
| **P-38** | Mill application event bus foundation (`mill-events`, **WI-311**–**WI-314**) | `done` |
| **P-39** | Event bus domain producers (metadata bridge, artifact persist hooks) | `planned` |
| **P-40** | Event bus side consumers (search index, SQL→schema relations, …) | `planned` |

## Reserved event type catalog (document)

| `EventType.id` | Future producer | Future consumer |
|----------------|-----------------|-----------------|
| `metadata.entity.created` | Metadata bridge | Search index consumer |
| `metadata.entity.updated` | Metadata bridge | Search index consumer |
| `metadata.entity.deleted` | Metadata bridge | Search index consumer |
| `metadata.facet.updated` | Metadata bridge | Search index consumer |
| `artifact.sql.persisted` | AI persistence projector | SQL→schema relation consumer |
| `chat.turn.completed` | Chat runtime | Chat search index consumer |

## Acceptance

- Design doc stands alone for onboarding a developer adding a new `EventConsumer` bean
- BACKLOG and module inventory reflect shipped modules
- [`README.md`](../../../design/platform/README.md) index entry updated for `general-event-bus.md`

## Non-goals

- `global-search-api` or `related-content-api` implementation docs beyond follow-on pointers
- Public user docs (`docs/public/src/`) — no user-visible feature yet
