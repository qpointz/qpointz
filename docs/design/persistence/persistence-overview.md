# Persistence Overview

This document maps all persistence concerns across Mill and is the starting point for any
persistence work. Detailed design and conventions are in the sibling documents in this folder.

---

## Domains with Persistence Work

| Domain | Description | Key WIs |
|--------|-------------|---------|
| **Platform** | Central module bootstrap, Flyway, Spring Boot wiring | WI-073a |
| **AI v3** | Chat memory, conversation record, events, artifacts, relation indexing | WI-073 · WI-074 · WI-075 |
| **Metadata** | Relational persistence for metadata entities, facets, scopes, audit | WI-029 |
| **Source** | Source definition persistence and CRUD (planned) | S-8 |

---

## Central Persistence Module

All persistence work depends on the central `persistence/` module group:

```
persistence/
├── mill-persistence              # JPA entities, Flyway migrations, store adapters
└── mill-persistence-autoconfigure  # Spring Boot @AutoConfiguration wiring only
```

**Dependency rule:** functional modules define store interfaces; `mill-persistence` implements
them; `mill-persistence-autoconfigure` wires them. See
[persistence-bootstrap.md](./persistence-bootstrap.md) for the full design.

---

## AI v3 Persistence Lanes

`ai/v3` has four distinct persistence concerns, modelled as separate lanes to avoid
coupling between their consistency requirements and consumers.

```
Lane 3   Chat Memory        Model-facing context continuity (token-bounded, may be lossy)
Lane 1,2 Events/Transcript  Canonical durable record: events, turns, artifacts, pointers
Lane 4   Relation Index     Async derived relations from artifact content (best-effort)
```

### Lane 3 — Chat Memory (WI-073)

- Owned by: `ai/v3-core` (port), `mill-persistence` (adapter)
- Consumed by: LangChain4j planner / answer synthesis
- Consistency: synchronous for in-process session; eventual for durable store

**Ports (defined in `ai/v3-core`):**

```kotlin
interface ChatMemoryStore {
    fun load(conversationId: String): ConversationMemory?
    fun save(memory: ConversationMemory)
    fun clear(conversationId: String)
}

fun interface LlmMemoryStrategy {
    fun project(input: MemoryProjectionInput): List<ConversationMessage>
}
```

Key types: `ConversationMemory`, `MemoryProjectionInput`, `ConversationMessage`

First implementations: `InMemoryChatMemoryStore`, optional Caffeine-backed variant.

### Lanes 1,2 — Events / Conversation / Artifacts (WI-074)

- Owned by: `ai/v3-core` (ports + routing model), `mill-persistence` (adapters)
- Consumed by: conversation UX, run inspection, refinement workflows, future SSE
- Consistency: immediate for in-memory session state; eventual for durability side-effects

**Event routing (profile-declared):**

```kotlin
data class EventRoutingPolicy(val rules: List<EventRoutingRule>)

data class EventRoutingRule(
    val eventType: String,
    val exposeToConsumers: Boolean,
    val persistEvent: Boolean,
    val persistAsTranscript: Boolean,
    val persistAsArtifact: Boolean,
)
```

**Routed event envelope:**

```kotlin
data class RoutedAgentEvent(
    val runtimeType: String,
    val kind: String,
    val content: Map<String, Any?>,
    val route: EventRoute,
    val conversationId: String?,
    val runId: String?,
    val profileId: String,
)
```

**Ports (defined in `ai/v3-core`):**

| Port | Responsibility |
|------|----------------|
| `AgentEventPublisher` / `AgentEventListener` | Fan-out routing to multiple consumers |
| `RunEventStore` | Append + query raw runtime event log |
| `ConversationStore` | Append + load canonical user/assistant turns |
| `ArtifactStore` | Append-only; full history of machine-readable outputs |
| `ActiveArtifactPointerStore` | Latest relevant artifact per role (for refinement) |

**Artifact subtypes:** `GeneratedSqlArtifact`, `SqlValidationArtifact`, `SqlResultArtifact`,
`ValueMappingArtifact`, `MetadataCaptureArtifact`

First implementations: all in-memory; Caffeine optionally for active session cache and
active pointer cache.

### Lane 4 — Artifact Observers / Relation Indexing (WI-075)

- Owned by: `ai/v3-core` (ports), downstream observer implementations
- Consumed by: `mill-ui` related-object views, analytics, future artifact RAG
- Consistency: asynchronous, best-effort, fully rebuildable from artifact history

**Ports (defined in `ai/v3-core`):**

```kotlin
fun interface ArtifactObserver {
    fun onArtifactCreated(artifact: RunArtifact, context: ArtifactContext)
}

interface RelationStore {
    fun save(relation: ArtifactRelation)
    fun saveAll(relations: List<ArtifactRelation>)
    fun listByTarget(targetType: String, targetId: String): List<ArtifactRelation>
}

data class ArtifactRelation(
    val sourceType: String,
    val sourceId: String,
    val relationType: String,
    val targetType: String,
    val targetId: String,
)
```

Example derived relations: `conversation → table`, `artifact → column`,
`artifact → metadata entity`.

### AI v3 Delivery Order

```
1. WI-073a  Platform bootstrap (prerequisite for all)
2. WI-074   Lanes 1,2 — establish canonical record and artifact base first
3. WI-073   Lane 3  — can project from stable persisted model
4. WI-075   Lane 4  — downstream; depends on artifacts existing
```

---

## Metadata Persistence (WI-029)

- Owned by: `metadata/mill-metadata-core` (interfaces), `mill-persistence` (adapters)
- Current state: file-backed (`FileMetadataRepository`)
- Target state: relational DB as primary store, file import for bootstrap only

**Schema model:**

- entities, facets, scope ownership, audit history
- JSONB columns for flexible facet storage (PostgreSQL)
- optimistic concurrency on writes

**Repository modes (runtime-selectable):**

| Mode | Description |
|------|-------------|
| `file` | Current default; loads from YAML |
| `jpa` | Relational DB primary; target operational mode |
| `composite` | File + JPA merged; transition period |

Flyway migrations for metadata schema live in `mill-persistence/src/main/resources/db/migration/`
alongside the core schema baseline.

---

## Source Persistence (S-8)

Planned — source definitions (connection specs, format configs, blob source roots) need a
durable CRUD API so sources can be registered and managed at runtime without restart.

- Interfaces defined in source modules
- Adapter and Flyway migration in `mill-persistence` when implemented

---

## Store Interface Location Rules

| Where the interface lives | Where the adapter lives |
|---------------------------|------------------------|
| Functional module (`ai/v3-core`, `mill-metadata-core`, source modules) | `mill-persistence` |

Never import `mill-persistence` types into functional modules. Inject adapters from outside.

---

## In-memory First Rule

All persistence lanes implement in-memory stores first:

- `InMemory*Store` in the same module that defines the interface (functional module or
  `ai/v3-capabilities`)
- Caffeine-backed variants for bounded retention (active session, active pointers, memory
  windows)
- Spring/JPA adapters in `mill-persistence` when durability across process restarts is needed

---

## Flyway Lane Ownership (planned migration sequence)

| Version | Lane | Table(s) |
|---------|------|----------|
| V1 | bootstrap | `mill_schema_info` |
| V2 | chat-memory | `conversation_memory`, `chat_message` |
| V3 | events/conversation/artifacts | `run_event`, `conversation_turn`, `run_artifact`, `active_artifact_pointer` |
| V4 | relation index | `artifact_relation` |
| V5 | metadata | `metadata_entity`, `metadata_facet`, `metadata_scope` |

---

## Related Documents

| Document | Description |
|----------|-------------|
| [persistence-bootstrap.md](./persistence-bootstrap.md) | Module structure, Flyway conventions, H2 PostgreSQL mode, testing, lane-addition guide |
| [agentic/v3-persistence-lanes.md](../agentic/v3-persistence-lanes.md) | Detailed architecture for all four AI v3 persistence lanes |
| [agentic/v3-conversation-persistence.md](../agentic/v3-conversation-persistence.md) | LLM memory vs UX transcript distinction, event categorisation, artifact types |
| [metadata/metadata-service-design.md](../metadata/metadata-service-design.md) | Metadata service phases; Phase 5 covers JPA/composite persistence |
| [metadata/metadata-implementation-roadmap.md](../metadata/metadata-implementation-roadmap.md) | Metadata implementation phase plan |
