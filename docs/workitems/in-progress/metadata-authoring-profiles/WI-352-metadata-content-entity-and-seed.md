# WI-352 — `MetadataContent` entity, seeds, and `FacetProposalMerger`

Status: `planned`  
Type: `✨ feature`  
Area: `metadata`  
Depends on: [WI-345](WI-345-metadata-authoring-design-contract.md)  
**Stage:** 2 — branch `feat/metadata-content` (see [`STORY.md`](STORY.md) § Staged delivery)

## Problem Statement

Facet **definitions** (`FacetTypeDefinition`) must stay free of LLM ergonomics (examples, category
cookbooks). Authoring support content — few-shot payloads, category guidance — needs a separate
attachment model keyed by target URN.

## Goal

Introduce **`MetadataContent`** in metadata modules: JPA entity, domain type, seed import, repository.
Ship v1 seeds for **`facet-type-example`** and **`facet-type-category`**, plus default
**`FacetProposalMerger`** for capture-time scope merge semantics (**WI-353** handler).

## In Scope

### 1. Domain + persistence (`mill-metadata-core`, `mill-metadata-persistence`)

- **`MetadataContent`** data class (audit quad, `uuid`, `contentUrn`, `contentKind`, `targetUrn`, optional `scopeUrn`, `title`, `description`, `content` body, `mediaType`, `sortOrder`, `enabled`, `schemaVersion`)
- Flyway DDL **`metadata_content`** (standard metadata row shape — see [`GAPS.md`](GAPS.md) §4)
- JPA **`MetadataContentEntity`** + **`MetadataContentAuditListener`**
- **`MetadataContentRepository`** (list by `targetUrn` / `contentKind`, get by `contentUrn`)
- **`MetadataUrns.facetTypeCategory(slug)`** → `urn:mill/metadata/facet-type-category:<slug>`
- Extend [`MetadataYamlSerializer`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/io/MetadataYamlSerializer.kt): `kind: MetadataContent` in seed stream

### 2. Seed files (`mill.metadata.seed.resources`)

| File | `contentKind` | Min rows |
|------|---------------|----------|
| `platform-facet-category-guidance.yaml` | **`facet-type-category`** | `general`, `relation`, `data-quality` — `targetUrn` = `urn:mill/metadata/facet-type-category:<slug>` |
| `platform-facet-authoring-examples.yaml` | **`facet-type-example`** | `descriptive`, `relation-source`, **`relation-target`**, `dq-null-check`, `dq-predicate` — `targetUrn` = facet type URN |

**Seed order:** facet type definitions → category guidance → examples (examples validate payload against target type `contentSchema` on import).

**`facet-type-example` body** (`mediaType: application/json`): `{ "metadataEntityId"?, "payload": {…} }`.

**`facet-type-category` body** (`mediaType: application/json`): `category`, `summary`, `signalPhrases[]`, `antiSignalPhrases[]`, `typicalEntityKinds[]`, `exampleFacetTypeKeys[]`, `nextStep` — see GAPS §4.

### 3. `FacetProposalMerger` (`mill-metadata-core`)

Pluggable merge planner — invoked by **`FacetArtifactPersistedHandler`** (**WI-353**) on **`artifact.facet.persisted`**:

```kotlin
interface FacetProposalMerger {
    fun planAssignments(
        proposal: FacetProposalMergerInput,
        facetTypeCardinality: FacetTargetCardinality,
    ): List<FacetAssignmentPlan>
}
```

**Default `CardinalityAwareFacetProposalMerger`:**

| `targetCardinality` | Behaviour per scope |
|---------------------|---------------------|
| **`SINGLE`** | One row, `merge_action: SET` — overrides prior effective assignment in scope |
| **`MULTIPLE`** | One row, `merge_action: SET` — adds to collection |

Aligns with read merge in [`MetadataReader`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/service/MetadataReader.kt). **`TOMBSTONE` / `CLEAR`** — operator lifecycle only.

### 4. Unit tests

- Seed import: invalid `facet-type-example` payload fails validation
- Category content: `targetUrn` slug matches JSON `category` field
- Merger: SINGLE vs MULTIPLE plans

## Out of Scope

- AI tools (`list_content`, `get_facet_type` join) — **WI-347**
- `MetadataReadPort` wiring — **WI-346**
- REST CRUD for content; chat-scoped content rows
- Promotion REST / UI (M-23); chat-scope lifecycle — **WI-353**
- Normative design prose — [`metadata-content.md`](../../../design/metadata/metadata-content.md) completed in **WI-345** skeleton + **this WI** ([`GAPS.md`](GAPS.md) §13)
- `examples[]` on `FacetTypeDefinition`

## Acceptance Criteria

- [ ] `metadata_content` table + entity with full audit quad
- [ ] `kind: MetadataContent` imports via `mill.metadata.seed.resources`
- [ ] ≥3 `facet-type-category` rows + ≥5 `facet-type-example` rows in platform seeds (incl. `relation-source` and `relation-target`)
- [ ] `FacetProposalMerger` interface + default impl with unit tests
- [ ] No `examples[]` added to `FacetTypeDefinition` / `FacetTypeManifest`
- [ ] [`metadata-content.md`](../../../design/metadata/metadata-content.md) entity/seed sections updated (§13)

## Suggested commit

`[feat] WI-352: MetadataContent entity, seeds, and FacetProposalMerger`
