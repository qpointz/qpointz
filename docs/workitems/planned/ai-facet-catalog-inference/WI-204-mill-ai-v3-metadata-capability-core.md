# WI-204 — `mill-ai-v3`: `metadata` + `metadata-authoring` capabilities, `MetadataReadPort`, facet tools & handlers

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `metadata`  
Milestone: `0.8.0`

## Problem

Agents need **`metadata`** read/validate tooling **and** (**separately**) **facet-assignment proposal CAPTURE** in **`mill-ai-v3`** (framework-free layer): a **`MetadataReadPort`** contract, **`MetadataCapability`** (QUERY-only) **`+`** **`MetadataAuthoringCapability`** (CAPTURE + protocol per **[`STORY.md`](STORY.md)**), YAML + **`META-INF/services`** registration, and **deterministic** handler unit tests—all **without** depending on Spring. **QUERY and CAPTURE must not ship as one undifferentiated capability** on **`schema-exploration`** (exploration-only profile).

## Validation strategy (normative)

**[`mill-py` REST`](../../../../clients/mill-py/mill/metadata/client.py)** exposes **GET** catalog + entity-facet reads (`list_facet_types`, `get_entity_facets`, …)—**not** a **`POST /validate`**. Therefore:

- **`validateFacetPayload`** on the port is implemented **locally** using **`mill-metadata-core`** (**`FacetTypeManifest`**, existing normalisers / validation helpers aligned with service behaviour), optionally fed by facet definitions **cached from REST** `list_facet_types`.

- A future Mill **validate endpoint** may be added later; the port signature stays stable so an adapter can delegate **without** changing capability YAML.

## Goal

Implement in **`ai/mill-ai-v3`**:

1. **`MetadataReadPort`** — (**a**) **`list_facet_types`** / optional **`get_facet_type`**, (**b**) **`list_entity_facets`** (entity **`urn:mill/model/…`** + optional **`scope` / `context` / `origin`** mirroring REST), (**c**) **`validateFacetPayload(...)`** as above.

2. **`MetadataCapability`** (QUERY only) — tools: **`list_facet_types`**, **`list_entity_facets`**, **`validate_facet_payload`** (final YAML names as in **[`STORY.md`](STORY.md)**). **No** CAPTURE tools; profile **`schema-exploration`** loads this capability only.

3. **`MetadataAuthoringCapability`** — **CAPTURE** **`propose_facet_assignment`** with **`protocol: metadata.faceting.capture`** and **`structured_final`** (mirror **`schema-authoring.capture`** in **[`schema-authoring.yaml`](../../../../ai/mill-ai-v3/src/main/resources/capabilities/schema-authoring.yaml)**). Handler emits structured **`{ facetTypeKey, metadataEntityId, payload, rationale }`** (or batch shape aligned in **WI-206**), calling the **same** **`validateFacetPayload`** before **`ToolResult` success**. YAML: **`capabilities/metadata-authoring.yaml`** (or equivalent split).

4. **Prompts `metadata.faceting.*`** — grounding order **`schema`** → **canonical URNs** (see **[`metadata-urn-platform.md`](../../../design/metadata/metadata-urn-platform.md)**) → **`list_entity_facets`**.

5. **Tests** — fakes covering: invalid applicability, payload errors; **`propose_facet_assignment`** rejection vs valid capture; **`CapabilityRegistry.validateDependencies`** with **`metadata`** and **`metadata-authoring`**; assertion that **`metadata`** capability registry **does not** register CAPTURE tools (exploration-safe).

## Acceptance Criteria

- Story headline outcomes (**3**) QUERY on exploration and (**4**) CAPTURE on authoring: **`propose_facet_assignment`** exists **only** under **`metadata-authoring`**, is wired to **`metadata.faceting.capture`** / **`structured_final`**, and is exercised in tests—not mixed into **`MetadataCapability`**.

- **WI-151** / WI-206 parity text can cite **catalog-driven NL facet proposals** on the **`schema-authoring`** path.

## Out of Scope

Spring beans (**WI-205**). Standalone docs (**WI-206**).

## Depends on

None within repo (coordinate with WI-206 for early design stubs if desired).

## Reference

[**`STORY.md`**](STORY.md), **`metadata/mill-metadata-core`**, **`[MetadataEntityController#getEntityFacets](../../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataEntityController.kt)`**.
