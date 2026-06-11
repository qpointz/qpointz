# WI-306 — Capability manifests + data-analysis profile

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Story: [`STORY.md`](STORY.md)

## Depends on

- **WI-303** (YAML `artifacts:` schema + canonical descriptor fields).

## Goal

Populate capability manifests with artefact descriptors and add **`data-analysis`** profile for cross-profile SQL POC.

## Profile capability matrix (clarified)

| Profile | `capabilityIds` | POC | Notes |
|---------|-----------------|-----|-------|
| **`data-analysis`** | `conversation`, `schema`, `metadata`, `sql-dialect`, `sql-query` | SQL (A) | **Excludes** `metadata-authoring` — no facet tools |
| **`schema-authoring`** | above + `schema-authoring`, `metadata-authoring` | SQL (A) + facet (B) | Existing profile; unchanged intent |

“SQL-only” means **no metadata-authoring / facet authoring**, not literally one capability.

## Deliverables

**Capability YAML (use canonical descriptor fields from WI-303):**

- [ ] [`sql-query.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/sql-query.yaml):
  - `sql-validation` descriptor — `sourceEvent: tool.result`, `persistKind: sql.validation`
  - `generated-sql` descriptor — `sourceEvent: protocol.final`, `persistKind: sql.generated`, `wirePartType: sql`
  - `validate_sql.emitsOnSuccess` → `generated-sql` when `passed=true`
  - Prompt update: step 4 emission is automatic after pass (no model protocol call)
- [ ] [`metadata-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/metadata-authoring.yaml) — `inferred-facet`, `OnCaptureSuccess`
- [ ] [`schema-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/schema-authoring.yaml) — capture descriptors as needed

**Profile:**

- [ ] [`DataAnalysisAgentProfile.kt`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/profile/DataAnalysisAgentProfile.kt) — capability set per matrix above
- [ ] Register in [`DefaultProfileRegistry`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/profile/ProfileRegistry.kt)
- [ ] Update [`ProfileRegistryTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/profile/ProfileRegistryTest.kt):
  - `knownIds` includes `data-analysis`
  - **`registeredProfiles()` sort order** — profiles sorted by id; expect `hello-world`, `data-analysis`, `schema-authoring`, `schema-exploration` (update exact list assertions)

**Tests:**

- [ ] `ProfileCapabilityMatrixTest` — `data-analysis` lacks `metadata-authoring`; `schema-authoring` includes it
- [ ] Tool load test — `data-analysis` resolves `validate_sql`; does not resolve `propose_facet_assignment`

## Acceptance criteria

- [ ] `GET /api/v1/ai/profiles` includes `data-analysis` (service IT in WI-308).
- [ ] Same `generated-sql` descriptor active for both `data-analysis` and `schema-authoring`.
- [ ] `data-analysis` cannot load `propose_facet_assignment` tool.
- [ ] Profile registry ordering tests updated for four profiles.
