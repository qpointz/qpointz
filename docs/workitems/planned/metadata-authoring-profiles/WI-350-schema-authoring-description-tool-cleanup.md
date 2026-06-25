# WI-350 — Remove `schema-authoring` capability and per-facet `capture_*` tools

Status: `planned`  
Type: `🔧 refactoring`  
Area: `ai`  
Depends on: [WI-347](WI-347-metadata-authoring-capability.md), [WI-351](WI-351-multi-artifact-protocol-runtime.md)  
**Stage:** 7 — branch `refactor/remove-capture-tools` (see [`STORY.md`](STORY.md))

## Problem Statement

Facet capture is split across the legacy **`schema-authoring`** capability (typed `capture_*`,
`schema-authoring.capture` protocol) and **`metadata-authoring`** (`propose_facet_assignment`).

**Normative rule ([`GAPS.md`](GAPS.md) §7):** discontinue **`schema-authoring` capability** entirely.
Roles: **`schema`** + **`metadata`** = read-only tooling; **`metadata-authoring`** = capture only.

## Goal

Remove **`schema-authoring.yaml`**, **`SchemaAuthoringCapability`** provider, and all **`capture_*`**
tools. All authoring prompts live on **`metadata-authoring`** (delivered in WI-347).

## In Scope

1. **Delete** `capabilities/schema-authoring.yaml` and unregister [`SchemaAuthoringCapability`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/schema/SchemaAuthoringCapability.kt) (`ServiceLoader` / descriptor registry)
2. **Remove** `capture_description`, `capture_relation`, and any other `capture_*` from shipped manifests
3. **Migrate / delete** `schema-authoring.*` prompts — replacements on **`metadata-authoring.yaml`** (WI-347)
4. **`request_clarification`** — move to **`conversation`** capability YAML (**locked** GAPS §7)
5. **Profiles (WI-348 YAML)** — remove capability `schema-authoring`; **omit deprecated profile id `schema-authoring`** from platform seeds
6. **Tests / scenarios** — `propose_facet_assignment` only
7. **Artifact wire** — new captures: **`metadata.faceting.capture`** only; **[`FacetProposalWire`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/artifact/FacetProposalWire.kt) unchanged** — no legacy replay compat ([`GAPS.md`](GAPS.md) §11)

## Out of Scope

- Changing `metadata.faceting.capture` / `facet-proposal` wire shape
- SQL / value-mapping capabilities
- Profile id rename / default-profile change (§8 — document in WI-349)
- Adding new `capture_*` aliases

## Acceptance Criteria

- [ ] No `schema-authoring` capability in registry; no `capabilities/schema-authoring.yaml`
- [ ] Zero `capture_<facet>` tools in any shipped capability YAML
- [ ] **`metadata-authoring`** is the only capability with CAPTURE tools for facets
- [ ] `ProfileCapabilityMatrixTest` / MCP inventory: no `schema-authoring` capability tools
- [ ] Design doc states capability role split (§7)

## Suggested commit

`[refactor] WI-350: remove schema-authoring capability and capture_* tools`
