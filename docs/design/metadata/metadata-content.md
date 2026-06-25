# MetadataContent — authoring support attachments

Normative design for **`MetadataContent`** rows: few-shot facet examples and facet-category guidance
that stay **out of** [`FacetTypeDefinition`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/FacetTypeDefinition.kt).

**Related:** [`metadata-facet-catalog-v3.md`](../agentic/metadata-facet-catalog-v3.md) (tool matrix, authoring loop), [`metadata-urn-platform.md`](metadata-urn-platform.md).

## Purpose

| Layer | Holds |
| ----- | ----- |
| `FacetTypeDefinition` | Contract: `contentSchema`, `applicableTo`, cardinality |
| **`MetadataContent`** | LLM ergonomics: category cookbooks, few-shot payloads |

**Forbidden:** `examples[]` on facet type definitions or capability manifests.

## Entity shape

Table: **`metadata_content`** (Flyway `V13__metadata_content.sql`).

| Field | Role |
| ----- | ---- |
| `contentUrn` | Stable id, e.g. `urn:mill/metadata/content:facet-type-example/…` |
| `contentKind` | `facet-type-example` \| `facet-type-category` |
| `targetUrn` | Facet type URN (examples) or `urn:mill/metadata/facet-type-category:<slug>` (guidance) |
| `scopeUrn` | Optional; `null` = platform-global |
| `contentBody` | JSON text (`mediaType: application/json`) |
| `sortOrder`, `enabled` | Wire join ordering and filtering |

## Content kinds

### `facet-type-category`

Body fields: `category`, `summary`, `signalPhrases[]`, `antiSignalPhrases[]`, `typicalEntityKinds[]`, `exampleFacetTypeKeys[]`, `nextStep`.

Served by **`list_facet_categories`** (WI-359) — joined onto distinct catalog categories.

### `facet-type-example`

Body: `{ "metadataEntityId"?, "payload": {…} }` — payload must conform to target type `contentSchema` (validated on seed import when type is known).

Served synthetically as `examples[]` on **`get_facet_type`** wire output only (not stored on definitions).

## Seed files

Loaded via **`mill.metadata.seed.resources`** **after** facet type definition seeds:

| File | Min rows |
| ---- | -------- |
| `platform-facet-category-guidance.yaml` | `general`, `relation`, `data-quality` |
| `platform-facet-authoring-examples.yaml` | `descriptive`, `relation-source`, `relation-target`, `dq-null-check`, `dq-predicate` |

YAML: multi-document stream with `kind: MetadataContent` (see [`MetadataYamlSerializer`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/io/MetadataYamlSerializer.kt)).

## FacetProposalMerger

On **`artifact.facet.persisted`** (WI-360), [`CardinalityAwareFacetProposalMerger`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/facet/FacetProposalMerger.kt) plans scope rows:

| Cardinality | Per write scope |
| ----------- | --------------- |
| `SINGLE` | One `merge_action: SET` row |
| `MULTIPLE` | One `merge_action: SET` row (read merge applies collection semantics) |

Rows are stamped **`sourceArtifactId`** for idempotent retract on **Reject**.

## Lifecycle pointer

Chat-scope assign, Accept/Reject, and **`artifact.retracted`** are specified in [`ai-v3-chat-metadata-scope.md`](../agentic/ai-v3-chat-metadata-scope.md) and implemented in **WI-360** (stage 4).
