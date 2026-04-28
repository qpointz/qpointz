# WI-205 — Host wiring: `MetadataReadPort` bean, `schema-exploration` profile, dependency factory

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `metadata`, `spring`  
Milestone: `0.8.0`

## Problem

[**WI-204**](WI-204-mill-ai-v3-metadata-capability-core.md) delivers **`metadata`** **+** **`metadata-authoring`** capability **code**, but chats need a **`MetadataReadPort`** **Spring bean** that implements **REST reads** (**`mill-py`** parity **[`completed` story](../../completed/20260424-mill-py-metadata-client/STORY.md)**) **plus** **classpath-backed `validateFacetPayload`** (same rules as WI-204—not a hypothetical HTTP validation API), **profile wiring for both** **`schema-exploration`** (QUERY only) and **`schema-authoring`** (QUERY + CAPTURE + routing), and **service-level verification**.

## Goal

1. **`SchemaExplorationAgentProfile`** — **`metadata`** **`capabilityId`** only (**`conversation`**, **`schema`**, **`metadata`**). **Do not** list **`metadata-authoring`** (exploration-only; see **[`SchemaExplorationAgentProfile`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/profile/SchemaExplorationAgentProfile.kt)**).

2. **`SchemaAuthoringAgentProfile`** — add **`metadata`** **`+`** **`metadata-authoring`** to **`capabilityIds`**, and extend **`routingPolicy`** (**`artifactPointerKeys`**) so **`metadata.faceting.capture`** / **`protocol.final`** resolves **`last-metadata-facet-proposal`** (parallel **`last-schema-capture`** — see **[`SchemaAuthoringAgentProfile`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/profile/SchemaAuthoringAgentProfile.kt)** / **[`SchemaAuthoringCapability`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/capabilities/schema/SchemaAuthoringCapability.kt)**).

3. **`SchemaFacingCapabilityDependencyFactory`** + **`SpringCapabilityDependencyAssembler`** **`MetadataReadPort`**.

4. **Spring `MetadataReadPort` bean** (`mill-ai-v3-autoconfigure` / **`mill-ai-v3-service`**) — HTTP client for **`/api/v1/metadata`** facets + entities (**GET** parity with **`mill-py`**); **`validateFacetPayload`** invokes **`mill-metadata-core`** validation (**shared** classpath types, optionally reusing manifests from `listFacetTypes()` cache)—**no reliance** on unavailable REST validation routes.

5. **Unit test** — **`SchemaFacingCapabilityDependencyFactoryTest`** (exploration + authoring + stub).

6. **Integration tests (`testIT`)** — extend or supplement **`AiChatControllerIT`** in **`mill-ai-v3-service`**: (**a**) **`GET /api/v1/ai/profiles/schema-exploration`** includes **`metadata`** and **does not** include **`metadata-authoring`** in **`capabilityIds`**; (**b**) **`GET …/schema-authoring`** (or equivalent) includes **`metadata`** **and** **`metadata-authoring`**; (**c**) at least **one chat message** SSE path for **`schema-exploration`** resolves **without `5xx`** once **`MetadataReadPort`** is autowired; (**d**) **required:** at least **one chat message** SSE path for **`schema-authoring`** resolves **without `5xx`** and **exercises end-to-end assembly** of **`metadata`** **+** **`metadata-authoring`** (same bar as (**c**) for exploration—**not** optional). Add dedicated `@Test` methods if clearer than widening existing coverage.

## Depends on

[**WI-204**](WI-204-mill-ai-v3-metadata-capability-core.md).

## Out of Scope

YAML text / handler internals (**WI-204**). Docs/README (**WI-206**).

## Acceptance Criteria

- **Factory IT** passes.

- **Service testIT** **mandatorily** proves both: (**1**) **`schema-exploration` + metadata (QUERY)** SSE path and (**2**) **`schema-authoring` + metadata + metadata-authoring (CAPTURE path)** wiring end-to-end—**required**, not factory-only; neither profile’s service-level check is optional.

## Reference

[**`SpringCapabilityDependencyAssembler`**](../../../../ai/mill-ai-v3-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/dependencies/SpringCapabilityDependencyAssembler.kt), [**`AiChatControllerIT`**](../../../../ai/mill-ai-v3-service/src/testIT/kotlin/io/qpointz/mill/ai/service/AiChatControllerIT.kt).
