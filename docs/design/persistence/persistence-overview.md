# Persistence Overview

This document gives the high-level map of persistence work across Mill.
Detailed design rules live in the sibling documents in this folder, especially
[persistence-bootstrap.md](./persistence-bootstrap.md).

---

## Domains with Persistence Work

| Domain | Description | Notes |
|---|---|---|
| Platform | Centralized schema ownership, Flyway, shared persistence primitives | Owned by `persistence/` modules |
| AI v3 | Chat memory, transcript, run events, artifacts, pointers | Uses shared persistence principles and relation model |
| Metadata | Relational storage for metadata entities, facets, scopes, audit | Future shared relation reuse expected |
| Source | Durable source definitions and runtime CRUD | Planned |

---

## Module Principles

Mill persistence is split into:

```text
persistence/
  mill-persistence                 # centralized schema + shared persistence primitives
  mill-persistence-autoconfigure   # generic persistence bootstrap

<domain>/
  <domain>-core                    # functional contracts
  <domain>-persistence             # domain-specific JPA entities/repositories/adapters
  <domain>-autoconfigure           # domain-specific bean wiring
```

Key rule:

- schema history is centralized in `mill-persistence`
- domain-specific adapters do not have to live in `mill-persistence`

For the detailed ownership rules, see
[persistence-bootstrap.md](./persistence-bootstrap.md).

---

## Shared Cross-Model Relations

Mill should use a generalized relation mechanism rather than bespoke join tables
for every pair of domains.

Shared persistence concepts:

- `EntityRef`
- relation records with:
  - source id/type/urn
  - target id/type/urn
  - relation kind
  - optional metadata

URN is the persistence-level global identifier for relation-capable entities.

First-pass format:

- `urn:<type-path>:<id>`

Examples:

- `urn:agent/conversation-turn:<turnId>`
- `urn:agent/artifact/sql-query:<artifactId>`
- `urn:model/concept:<conceptId>`

The full `EntityRef` and URN rules are defined in
[persistence-bootstrap.md](./persistence-bootstrap.md).

---

## AI v3 Persistence Shape

AI v3 currently distinguishes these persistence concerns:

- chat metadata
- chat memory
- canonical transcript
- run events
- artifact history
- active artifact pointers
- cross-model relations

Ownership direction:

- `ai/mill-ai-v3`
  - ports and in-memory stores
- `ai/mill-ai-v3-persistence`
  - ai-v3-specific JPA entities, repositories, adapters, converters
  - H2 PostgreSQL-mode repository and store integration tests
- `ai/mill-ai-v3-autoconfigure`
  - ai-v3 bean wiring and JPA-backed override activation
- `persistence/mill-persistence`
  - Flyway migrations and shared relation primitives

Detailed ai-v3 design lives in:

- [agentic/v3-chat-service.md](../agentic/v3-chat-service.md)
- [agentic/v3-persistence-lanes.md](../agentic/v3-persistence-lanes.md)
- [agentic/v3-conversation-persistence.md](../agentic/v3-conversation-persistence.md)

---

## Database Strategy

Current persistence design targets:

- `H2` for tests and local development
- `Postgres` for production compatibility

Flyway principle:

- prefer one portable migration set
- keep product-specific behavior isolated

---

## In-Memory First

Each persistence lane should start with in-memory stores in the functional
module and gain durable adapters only when needed.

That means:

- functional module defines the interface
- functional module can provide `InMemory*Store`
- domain persistence module later provides durable JPA implementation

---

## Related Documents

| Document | Description |
|---|---|
| [persistence-bootstrap.md](./persistence-bootstrap.md) | Core persistence principles, module ownership, Flyway rules, `EntityRef`, URN |
| [../agentic/v3-persistence-lanes.md](../agentic/v3-persistence-lanes.md) | AI v3 lane architecture |
| [../agentic/v3-conversation-persistence.md](../agentic/v3-conversation-persistence.md) | AI v3 transcript and artifact model |
