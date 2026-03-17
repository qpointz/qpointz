# WI-075 - AI v3 Artifact Observers and Relation Indexing

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: `A-66`

## Problem Statement

`ai/v3` artifacts are useful not only for refinement and replay, but also for downstream UX and
analysis.

Examples:

- opening a table/object view and showing related chats
- showing related generated SQL for a domain object
- identifying which objects users are most interested in

This requires a downstream component that consumes persisted artifacts and derives related-object
links and analytics-friendly projections, without affecting the chat loop itself.

This WI depends on the persistence foundation from `WI-073a` and the artifact persistence lane
from `WI-074`.

## Goal

Implement a first downstream artifact-observer lane for `ai/v3` that:

- observes artifact create/update events
- derives relation links to domain/UI objects
- persists those derived relations
- remains asynchronous and best-effort

## Scope

In scope:

- `ArtifactObserver` contract
- `ArtifactRelationIndexer`
- `RelationStore`
- in-memory first implementation
- first derived relation types for:
  - conversation -> object
  - artifact -> object
  - run -> object

Out of scope:

- direct impact on planner/prompt behavior
- mandatory synchronous indexing on the user-facing path
- full analytics dashboards
- full artifact-derived RAG implementation

## Design Requirements

- This lane must not influence chat correctness.
- It must be asynchronous/best-effort.
- The relation index must be derived and rebuildable from artifact history.
- The artifact store remains the source of truth.
- The first implementation should use injected in-memory repositories.

## Primary Contracts

Suggested interfaces/types:

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

## Implementation Outline

1. Define observer and relation-store ports in `ai/v3-core`.
2. Implement first indexer for:
   - generated SQL artifacts
   - metadata capture artifacts
   - value-mapping artifacts where useful
3. Implement in-memory `RelationStore`.
4. Wire observer subscription after artifact persistence.
5. Add tests for relation derivation.

## Example Derived Relations

- `conversation:C1 --REFERENCES_TABLE--> retail.customers`
- `artifact:A7 --USES_COLUMN--> retail.customers.country`
- `artifact:A8 --TARGETS_ENTITY--> retail.orders.customer_id`

## Testing Strategy

- unit tests for relation extraction from artifact payloads
- unit tests for in-memory relation store
- integration-style test proving artifact persistence triggers observer/indexing path

## Acceptance Criteria

- Artifact observer contract exists and is wired outside the critical chat loop.
- A relation store exists with in-memory implementation.
- At least one artifact type produces derived related-object edges.
- Derived relations are queryable independently of the artifact store.
- The indexing flow is asynchronous/best-effort by design.
- The implementation builds on the shared persistence-module foundation from `WI-073a`.
