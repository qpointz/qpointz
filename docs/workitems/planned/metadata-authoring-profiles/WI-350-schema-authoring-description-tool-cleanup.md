# WI-350 — Remove per-facet capture tools (`capture_*`)

Status: `planned`  
Type: `🔧 refactoring`  
Area: `ai`  
Depends on: [WI-347](WI-347-metadata-authoring-capability.md), [WI-351](WI-351-multi-artifact-protocol-runtime.md)

## Problem Statement

Facet capture is split across **typed capture tools** and the **generic** metadata path:

| Path | Tool | Facet types | Protocol |
|------|------|-------------|----------|
| `schema-authoring` | `capture_description` | descriptive (hard-coded) | `schema-authoring.capture` |
| `schema-authoring` | `capture_relation` | relation (specialized args) | `schema-authoring.capture` |
| `metadata-authoring` | `propose_facet_assignment` | **any catalog type** (intended) | `metadata.faceting.capture` |

**Normative rule (story):** there must be **no `capture_<specific facet>`** tools — no
`capture_description`, `capture_relation`, or future per-type captures. All facet writes go through
**`propose_facet_assignment`** with a catalog **`facetTypeKey`** and manifest-aligned **`payload`**.

Prompts in [`schema-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/schema-authoring.yaml)
steer the model toward the legacy typed tools. [`FacetProposalWire`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/artifact/FacetProposalWire.kt)
bridges `captureType: description` → `descriptive` for chat wire.

## Goal

Remove **all** per-facet **`capture_*`** tools and align every authoring profile on the **catalog +
generic capture** loop from WI-347.

## In Scope

1. **Remove from `schema-authoring.yaml` and [`SchemaAuthoringCapability`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/schema/SchemaAuthoringCapability.kt):**
   - `capture_description`
   - `capture_relation`
   - Any other `capture_<facet>` if present
2. **Rewrite** schema-authoring prompts that reference typed captures (`schema-authoring.intent`,
   `.request`, `.parallel`, `.remediation`, `.clarification`) → catalog + `propose_facet_assignment`
3. **Update** [`SchemaExplorationAgent`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/SchemaExplorationAgent.kt) system text if it names `capture_description` / `capture_relation`
4. **Capability descriptor** — stop claiming "DescriptiveFacet and RelationFacet capture artifacts"
   on `schema-authoring`; facet capture is **`metadata-authoring`** only
5. **Audit** — grep repo for `capture_` tool registrations outside tests; none in shipped manifests
6. **Tests / scenarios** — migrate to `propose_facet_assignment` with appropriate `facetTypeKey`
   (`descriptive`, `relation-source`, …)
7. **Artifact wire** — all facet captures use **`metadata.faceting.capture`** → **`facet-proposal`** only; simplify/remove legacy `schema.authoring.capture` facet bridging in [`FacetProposalWire`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/artifact/FacetProposalWire.kt)

## Out of Scope

- Changing `metadata.faceting.capture` / `facet-proposal` wire shape
- Removing `request_clarification` from `schema-authoring`
- SQL / value-mapping tools
- Adding new `capture_*` aliases "for convenience"

## Acceptance Criteria

- [ ] Zero tools matching `capture_<facet>` in any capability YAML under `ai/mill-ai/src/main/resources/capabilities/`
- [ ] Descriptive, relation, and DQ scenarios: same chat artefact kind (`facet-proposal`), different `facetTypeKey` in content
- [ ] `ProfileCapabilityMatrixTest` / MCP tool inventory: no `capture_description` or `capture_relation`
- [ ] Design doc states the **no per-facet capture** rule

## Suggested commit

`[refactor] WI-350: remove capture_* tools; catalog-only propose_facet_assignment`
