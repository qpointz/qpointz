# AI v3 — Facet catalog tools and NL→facet inference (0.8.0)

**Milestone:** **0.8.0**

**Work items:** **WI-204** (**`mill-ai-v3`** capabilities + tools + protocols + tests) → **WI-205** (Spring **`MetadataReadPort`**, profile wiring) → **WI-206** (design docs, CLI, parity).

Expose **`metadata`** query tooling plus **facet-assignment proposals** grounded with **`schema`**. (**1**) **Defined** facet types and (**2**) merged **entity facets** and (**3**) **validation** are delivered as **QUERY** tools usable on **`schema-exploration`** (read-only, consistent with **`[SchemaExplorationAgentProfile](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/profile/SchemaExplorationAgentProfile.kt)`**). (**4**) **NL → structured facet-assignment proposals** use a **CAPTURE** tool **`propose_facet_assignment`** wired to a **`metadata.faceting.capture`** **STRUCTURED_FINAL** protocol (**same runtime pattern** as **`schema-authoring.capture`** in **[`capabilities/schema-authoring.yaml`](../../../../ai/mill-ai-v3/src/main/resources/capabilities/schema-authoring.yaml)**; see **[capabilities design §6.2 / CAPTURE](../../../../docs/design/agentic/developer-manual/v3-developer-capabilities-profiles-and-dependencies.md#62-query-vs-capture)**). **CAPTURE + protocol ship only on `schema-authoring`** (see **`[SchemaAuthoringAgentProfile](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/profile/SchemaAuthoringAgentProfile.kt)`**), **not** on **`schema-exploration`** — **exploration remains read-only**; authoring profile gains **`metadata`** (QUERY) **`+`** **`metadata-authoring`** (CAPTURE) capability ids—or equivalent split (**normative**, below).

---

## Architectural decision — profiles vs CAPTURE (**review**)

| Surface | Profiles / capability ids | Behaviour |
|---------|---------------------------|-----------|
| **Exploration-only** | **`schema-exploration`**: **`conversation`**, **`schema`**, **`metadata`** | **`metadata`** exposes **QUERY** tools only (**`list_facet_types`**, **`list_entity_facets`**, **`validate_facet_payload`**). Matches **`SchemaExplorationAgentProfile`**: no **`schema-authoring`**, **no CAPTURE**, **no capture-terminal protocols**. |
| **Authoring + proposals** | **`schema-authoring`**: existing capability ids **`+`** **`metadata`** **`+`** **`metadata-authoring`** _(second capability: **`propose_facet_assignment`** CAPTURE, **`protocol`** **`metadata.faceting.capture`)_ | Extend **`SchemaAuthoringAgentProfile.routingPolicy`** (**`artifactPointerKeys`**) for **`last-metadata-facet-proposal`** (parallel to **`last-schema-capture`**). **`metadata-authoring`** uses the same **`MetadataReadPort`** dependency type as **`metadata`**. |

**Rejected for this story:** Loading a single **`metadata`** capability on **`schema-exploration`** that mixes QUERY and CAPTURE (would contradict exploration-only unless every profile filtered tools—not how **`CapabilityRegistry`** works today).

**Implementation note:** Prefer **two** **`CapabilityProvider`** classes (**`MetadataCapability`**, **`MetadataAuthoringCapability`**) registered in **`META-INF/services`**; alternative **one YAML / two manifests** is acceptable only if tool registration clearly splits QUERY vs CAPTURE by provider.

---

## Normative decisions (technical)

- **Validation vs REST:** **`mill-py`** GET reads only (**no POST validate**). **`validateFacetPayload`** = classpath **`mill-metadata-core`** manifests (optionally cached from REST **`list_facet_types`**).

- **Entity identity:** Canonical **`urn:mill/model/…`**, **`UrnSlug`**; **`list_entity_facets`** ↔ **`GET …/facets`** (**`scope` / `context` / `origin`**) — [`metadata-urn-platform`](../../../design/metadata/metadata-urn-platform.md), [`MetadataEntityController`](../../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataEntityController.kt).

- **CAPTURE protocol:** **`propose_facet_assignment`** declares **`protocol: metadata.faceting.capture`**; **`capabilities/metadata-authoring.yaml`** (or sibling) defines **`metadata.faceting.capture`** as **`structured_final`** with **`finalSchema`** for proposal batch / facet payload (**WI-206** aligns JSON shape with **`schema-authoring.capture`** patterns). **`SchemaAuthoringAgentProfile.routingPolicy`** extends **`DefaultEventRoutingPolicy`** so **`protocol.final`** / observer synthesis receives **`last-metadata-facet-proposal`** (mirror **`SchemaAuthoringCapability`** **`artifactPointerKeys`** pattern).

## Gap (why this story exists)

**[`SchemaCapability`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/capabilities/schema/SchemaCapability.kt)** exposes structural **`list_*`** only. **`SchemaExplorationAgentProfile`** cannot list facet catalog entities or merged facets. **`SchemaAuthoringAgentProfile`** can emit **relational/descriptor** captures via **`schema-authoring`** but has **no** catalog-backed **facet-type** listing or generic facet proposal CAPTURE. This story fills those gaps **without** redefining **`schema-exploration`** as authoring.

## Prerequisites

- **[Completed mill-py client](../../completed/20260424-mill-py-metadata-client/STORY.md)** (**WI-192–WI-203**) — REST parity for reads.
- **[`metadata-facet-type-catalog-defined-and-observed`](../../../design/metadata/metadata-facet-type-catalog-defined-and-observed.md)** (DEFINED/OBSERVED).

**Coordinates:** [**WI-157**](../../planned/ai-value-mapping-capability/STORY.md), [**WI-172**/ **WI-173**](../../planned/metadata-value-mapping/STORY.md).

## Planned execution

### WI-204 [`WI-204-mill-ai-v3-metadata-capability-core.md`](WI-204-mill-ai-v3-metadata-capability-core.md)

- **`metadata`** (**QUERY**) + **`metadata-authoring`** (**CAPTURE** + **`metadata.faceting.capture`** protocol **`structured_final`**), **`META-INF/services`**, **`MetadataReadPort`**, tests (including CAPTURE + protocol emission contract vs **`schema-authoring` mirror).

### WI-205 [`WI-205-mill-ai-v3-metadata-host-profile-spring.md`](WI-205-mill-ai-v3-metadata-host-profile-spring.md)

- **`SchemaExplorationAgentProfile`**: **`metadata`** only (**QUERY** surface).
- **`SchemaAuthoringAgentProfile`**: **`metadata`** + **`metadata-authoring`**, **`routingPolicy`** artifact pointer for **`metadata.faceting.capture`**.
- Spring **`MetadataReadPort`**, **`testIT`** (**[`AiChatControllerIT`](../../../../ai/mill-ai-v3-service/src/testIT/kotlin/io/qpointz/mill/ai/service/AiChatControllerIT.kt)**): **required** SSE smoke for **`schema-exploration`** **and** **`schema-authoring`** (**both** mandatory per WI-205).

### WI-206 [`WI-206-mill-ai-v3-metadata-design-docs-cli-parity.md`](WI-206-mill-ai-v3-metadata-design-docs-cli-parity.md)

- Design note + **`mill-ai-v3-cli`**: **`schema-exploration`** (read facet tools) **`+`** **`schema-authoring`** (facet proposal CAPTURE path).

## Work Items

- [x] WI-204 [`WI-204-mill-ai-v3-metadata-capability-core.md`](WI-204-mill-ai-v3-metadata-capability-core.md)
- [x] WI-205 [`WI-205-mill-ai-v3-metadata-host-profile-spring.md`](WI-205-mill-ai-v3-metadata-host-profile-spring.md)
- [x] WI-206 [`WI-206-mill-ai-v3-metadata-design-docs-cli-parity.md`](WI-206-mill-ai-v3-metadata-design-docs-cli-parity.md)

## Related stories

- [`../../planned/ai-v1-v3-parity-baseline/STORY.md`](../../planned/ai-v1-v3-parity-baseline/STORY.md) (**WI-151**)
- [`../../planned/metadata-value-mapping/STORY.md`](../../planned/metadata-value-mapping/STORY.md) (**WI-172**, **WI-173**)
- [`../../planned/ai-value-mapping-capability/STORY.md`](../../planned/ai-value-mapping-capability/STORY.md) (**WI-157**)
