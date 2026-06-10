# WI-206 — Design documentation, CLI smoke, parity cross-reference

Status: `done`  
Type: `📝 docs`  
Area: `ai`, `metadata`  
Milestone: `0.8.0`

## Problem

[**WI-204**](WI-204-mill-ai-v3-metadata-capability-core.md) / [**WI-205**](WI-205-mill-ai-v3-metadata-host-profile-spring.md) need traceable architecture text, operator instructions, and **WI-151** parity alignment so reviewers know how facet catalog tooling fits the agentic roadmap.

## Goal

1. **Design note** (`docs/design/agentic/` and/or `docs/design/metadata/`): facet tool contract; QUERY vs CAPTURE split (**`schema-exploration`** vs **`schema-authoring`**); **`metadata.faceting.capture`** **`structured_final`**, **`routingPolicy`** / **`last-metadata-facet-proposal`**; **[`metadata-urn-platform.md`](../../../design/metadata/metadata-urn-platform.md)** (URN / **`UrnSlug`**); **local `validateFacetPayload`** (classpath manifests, **no** hypothetical REST **`POST validate`**—see **`STORY` normative block**); **DEFINED vs OBSERVED** (M-32); **`schema-exploration`** read-only rationale.

2. **`ai/mill-ai-v3-cli/README.md`** — add **`schema-exploration`** example (**facet QUERY tools**) next to **`schema-authoring`** (**facet proposal CAPTURE** path).

3. **`WI-151`** parity matrix — cite **catalog-driven facet capture from NL** on **`schema-authoring`** (**WI-204 CAPTURE under `metadata-authoring`**).

## Depends on

Prefer completing **WI-204**/**WI-205**, or finalize docs trailing one commit (**RULES** tracker consistency).

## Acceptance Criteria

- Design doc echoes **WI-204**/host split: **reads** **`mill-py`-aligned HTTP**, validation **classpath-local**; **QUERY** on exploration, **CAPTURE + protocol + routing** on authoring; **`metadata-urn-platform`** linkage for **`metadataEntityId`**.

- CLI README lists **`schema-exploration`** copy-paste prerequisites (**service**, **`OPENAI_API_KEY`**, metadata-enabled Mill).

- Parity artefact (**WI-151**) links this delivery.


## Reference

[**`WI-151`**](../ai-v1-v3-parity-baseline/WI-151-ai-v1-v3-parity-matrix-and-capability-design-alignment.md), [**`mill-ai-v3-cli/README.md`**](../../../../ai/mill-ai-v3-cli/README.md).
