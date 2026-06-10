# WI-288 — Design and public documentation

**Story:** [`ai-configuration-restructure`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `📝 docs` |
| **Area** | `ai` |
| **Depends on** | [**WI-284**](WI-284-mill-ai-configuration-property-bindings.md)–[**WI-287**](WI-287-mill-ai-configuration-tests.md) (substance final) |
| **Enables** | Story closure |

## Goal

Replace outdated **`mill.ai.*`** documentation with the layered model; give operators a migration
table and full property reference.

## Deliver

### Design (authoritative)

Rewrite [`docs/design/ai/mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md):

- Layers: `providers` → `models` → `vector-stores` → `data.embedding` → `chat`.
- `data.embedding` profile: `model`, `vector-store`, `max-content-length`, `refresh`, `sources[]`.
- `sources[].type` = pluggable adapter; `metadata-facets` is `{ type: metadata-facets }` only in v1.
- `vector-store.backend` ref vs inline resolution.
- `chat` = basic defaults; capability `embedding` refs; agent profiles stay in registry.
- Resolution flows and migration table from legacy keys.
- Link [`value-mapping-indexing-facet-types.md`](../../../design/metadata/value-mapping-indexing-facet-types.md) for per-column metadata policy.

### Public mirror

Update [`docs/public/src/reference/mill-ai-configuration.md`](../../../public/src/reference/mill-ai-configuration.md) (abbreviated operator view).

### Inventory

Update [`docs/design/platform/CONFIGURATION_INVENTORY.md`](../../../design/platform/CONFIGURATION_INVENTORY.md): remove `mill.ai.model`, `mill.ai.embedding-model`, `mill.ai.value-mapping`, singleton `mill.ai.vector-store`; add new prefixes.

### README index

Refresh [`docs/design/ai/README.md`](../../../design/ai/README.md) scope line if needed.

## Acceptance

- [ ] Design doc matches implemented bindings (post WI-284–287).
- [ ] Public reference and inventory list new keys with defaults.
- [ ] Migration table documents clean break for external operators.
