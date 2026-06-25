# Metadata facet catalog — catalog-generic authoring (v3)

Normative design for **metadata** QUERY tools, **`metadata-authoring`** CAPTURE, YAML agent profiles,
and **`MetadataContent`** wiring. Supersedes the legacy **`schema-authoring`** profile split in earlier drafts.

**Story:** [`metadata-authoring-profiles`](../../../workitems/planned/metadata-authoring-profiles/STORY.md) (stage 1: WI-354, WI-356, WI-358).

**See also:** [`metadata-content.md`](../metadata/metadata-content.md), [`ai-v3-chat-metadata-scope.md`](ai-v3-chat-metadata-scope.md), [`dq-rule-facet-types.md`](../metadata/dq-rule-facet-types.md).

## Table of contents

1. [Capability role split](#capability-role-split)
2. [Agent profiles (YAML)](#agent-profiles-yaml)
3. [Tool matrix](#tool-matrix)
4. [Authoring loop](#authoring-loop)
5. [Prompt enforcement](#prompt-enforcement)
6. [Multi-facet batch](#multi-facet-batch)
7. [Mixed SQL + facet artefacts](#mixed-sql--facet-artefacts)
8. [Chat artefact contract](#chat-artefact-contract)
9. [Facet lifecycle (overview)](#facet-lifecycle-overview)
10. [Relation facet key rule](#relation-facet-key-rule)
11. [Worked example](#worked-example)

## Capability role split

| Capability | Role |
| ---------- | ---- |
| **`schema`** | Physical schema RO: `list_schemas`, `list_tables`, `list_columns`, … |
| **`metadata`** | Catalog RO: facet categories/types, validation, scopes |
| **`metadata-authoring`** | Single CAPTURE: **`propose_facet_assignment`** |
| **`schema-authoring`** | **Discontinued** — remove typed `capture_*` tools (WI-361) |

**Normative rule:** no `capture_<specific facet>` tools in any capability. All facet writes use **`propose_facet_assignment`** with `(target, facetType, payload)`.

## Agent profiles (YAML)

Profiles are **`kind: AgentProfile`** multi-document YAML, loaded via **`mill.ai.profiles.seed.resources`** (default: `classpath:profiles/platform-agent-profiles.yaml`).

| Profile id | Capabilities | Notes |
| ---------- | ------------ | ----- |
| `hello-world` | `conversation`, `demo` | Smoke / harness |
| `schema-exploration` | `conversation`, `schema`, `metadata` | QUERY only — no CAPTURE |
| `metadata-authoring` | `+ metadata-authoring` | Facet proposals, no SQL |
| `data-analysis` | `+ sql-dialect`, `sql-query`, `value-mapping`, **`metadata-authoring`** | Mixed documentary + query utterances |

**Deprecated:** profile id **`schema-authoring`** — do not ship in platform seeds.

## Tool matrix

| Tool | Capability | Summary |
| ---- | ---------- | ------- |
| `list_facet_categories` | `metadata` | Categories + joined **`facet-type-category`** [`MetadataContent`](../metadata/metadata-content.md) |
| `list_facet_types` | `metadata` | Summary rows for reasoning — **no** `contentSchema` on list |
| `get_facet_type` | `metadata` | Full manifest + `contentSchema` + synthetic `examples[]` from content |
| `list_content` / `get_content` | `metadata` | Query content rows (WI-359) |
| `list_metadata_scopes` | `metadata` | Context-sensitive scopes with `access` (`r` \| `w` \| `rw`) |
| `validate_facet_payload` | `metadata` | Schema + optional `applicableTo` when `metadataEntityId` supplied |
| `propose_facet_assignment` | `metadata-authoring` | `(metadataEntityId, facetTypeKey, payload)` — runtime stamps `writeScopeUrns[]` |
| `capture_description`, `capture_relation`, … | — | **Remove** (WI-361) |

## Authoring loop

```
ground target (schema tools) → metadataEntityId
→ list_metadata_scopes()
→ list_facet_categories()
→ list_facet_types [filters]
→ get_facet_type(facetTypeKey)
→ validate_facet_payload(facetType, payload [, target])
→ propose_facet_assignment(target, facetType, payload)
→ facet-proposal artefact (pending; writeScopeUrns[])
→ artifact.facet.persisted → scope assign (FacetProposalMerger)
→ UI Accept | Reject
```

## Prompt enforcement

When **`metadata-authoring`** is on the profile, documentary utterances must run the catalog loop — not prose-only answers and not SQL unless the user asks to **retrieve** data.

| Prompt | Role |
| ------ | ---- |
| `metadata-authoring.intent` | `AUTHOR_FACET` vs explore vs `QUERY_DATA` |
| `metadata-authoring.reasoning` | Category → ground → type → validate → capture |
| `metadata.faceting.system` | Grounding + capture gate |
| `metadata.faceting.request` | Structured fields before capture |
| `metadata-authoring.batch` | Multi-tuple decomposition (WI-359) |

**Cross-capability:** on `data-analysis`, `metadata-authoring.intent` wins for documentary turns; `sql-query` wins only for `QUERY_DATA`.

## Multi-facet batch

| Rule | Detail |
| ---- | ------ |
| Decompose | Independent `(target, facetType, payload)` per implied facet |
| Emit all | Parallel captures in one model round when possible |
| Partial failure | Persist every success; remediate failures next iteration |
| Artefacts | Each success → separate `facet-proposal` row |
| Runtime | **N** captures per turn (WI-355 batch protocol + SSE) |

Prefer one **`ProtocolFinal`** with `results[]` on `metadata.faceting.capture` (WI-355); interim: N × `ProtocolFinal` must not drop facets in SSE.

## Mixed SQL + facet artefacts

| Kind | When |
| ---- | ---- |
| `generated-sql` | User asks for data |
| `facet-proposal` | User documents metadata |

`data-analysis` loads **`sql-query`** and **`metadata-authoring`** so one turn may emit both.

## Chat artefact contract

| Layer | Value |
| ----- | ----- |
| Wire `partType` | **`facet-proposal`** (all facet types) |
| `persistKind` | `metadata.faceting.capture` |
| Per-type identity | `facetTypeKey`, `metadataEntityId`, `payload`, `writeScopeUrns[]`, `status` in body |

## Facet lifecycle (overview)

Capture does **not** write metadata inline. Side effects use **`:core:mill-events`**:

1. Persist `facet-proposal` (`pending`, `writeScopeUrns[]`)
2. **`artifact.facet.persisted`** → `FacetProposalMerger` → scope rows with `sourceArtifactId`
3. **Accept** → status only
4. **Reject** → **`artifact.retracted`** → tombstone by `sourceArtifactId`

Full normative detail: [`ai-v3-chat-metadata-scope.md`](ai-v3-chat-metadata-scope.md) § lifecycle rewrite (WI-360).

## Relation facet key rule

| Grounded entity role | Facet type key |
| -------------------- | -------------- |
| Table as **source** of FK | `relation-source` |
| Table as **target** of FK | `relation-target` |
| Schema / model edge | `relation` |

## Worked example

> User: “In table **orders**, column **customer_id** must not be null.”

1. Intent: **`AUTHOR_FACET`**
2. Ground → attribute URN for `orders.customer_id`
3. `list_facet_types` → **`dq-null-check`**
4. Payload from `contentSchema`: `{ "name": "customer_id_not_null", "description": "…", "severity": "error" }`
5. `validate_facet_payload` → `propose_facet_assignment`

**Not:** `SELECT … WHERE customer_id IS NULL` unless the user asked for violating rows.

## References

- [`metadata-urn-platform.md`](../metadata/metadata-urn-platform.md)
- [`MetadataReadPort`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/MetadataReadPort.kt) (WI-357)
- Legacy capture removal: **WI-361**
