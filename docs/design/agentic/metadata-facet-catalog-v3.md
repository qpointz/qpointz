# Metadata facet catalog — AI v3 capability split

This note aligns [**WI-204**](../../../workitems/planned/ai-facet-catalog-inference/WI-204-mill-ai-v3-metadata-capability-core.md) / [**WI-205**](../../../workitems/planned/ai-facet-catalog-inference/WI-205-mill-ai-v3-metadata-host-profile-spring.md) with the agentic stack.

## QUERY vs CAPTURE

| Profile | Capability ids | Tools | Protocol / capture |
|--------|----------------|-------|---------------------|
| **`schema-exploration`** | `metadata` | Facet **read** tools (catalog, list facet types, validate payload structure). | No CAPTURE tools. |
| **`schema-authoring`** | `metadata`, `metadata-authoring` | Same reads **plus** **`propose_facet_assignment`** under CAPTURE for NL-driven proposals. | **`metadata.faceting.capture`**, `structured_final`; router stores pointer id **`last-metadata-facet-proposal`** (see `DefaultAgentEventRouter` in `mill-ai-v3`). |

**`schema-exploration`** stays read-only so exploration chats do not propose writes. **Authoring** stacks **metadata-authoring** for capture.

## Validation and URNs

- **Local validation**: `validateFacetPayload` / structure checks use the **classpath facet-type manifest** via `MetadataReadPort` (aligned with **mill-py** expectations). There is **no** separate HTTP `POST validate` in this story; hosts may add HTTP later.
- **URNs**: Facet keys and entity references follow [`metadata-urn-platform.md`](../metadata/metadata-urn-platform.md) (`UrnSlug`, `metadataEntityId`).

## DEFINED vs OBSERVED (M-32)

Catalog entries may be **DEFINED** (authoritative type definition) or **OBSERVED** (inferred). Tools return this distinction where the port exposes it so agents do not over-trust inferred rows.

## Host wiring

- **`MetadataReadPort`**: Spring hosts register a real port or rely on `EmptyMetadataReadPort` when autoconfig cannot resolve metadata.
- **`PersistenceAutoConfiguration`** owns `@EnableJpaRepositories` for `io.qpointz.mill.persistence`. Applications also using **`spring-boot-starter-data-jpa`** must **not** duplicate Boot’s `JpaRepositoriesAutoConfiguration` (exclude that auto-config or disable `spring.data.jpa.repositories.enabled`) alongside Mill persistence autoconfigure.

## References

- **[`metadata-urn-platform.md`](../metadata/metadata-urn-platform.md)**
- **WI-151** parity: [`WI-151`](../../../workitems/planned/ai-v1-v3-parity-baseline/WI-151-ai-v1-v3-parity-matrix-and-capability-design-alignment.md) — catalog-driven facet capture on **`schema-authoring`**.
