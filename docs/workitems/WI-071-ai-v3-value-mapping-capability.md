# WI-071 - AI v3 Value Mapping Capability

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: `A-62`

## Problem Statement

`ai/v1` handled an important practical NL2SQL concern that `ai/v3` still lacks: mapping
user-facing business values to the concrete stored values needed in SQL.

Examples:

- user says `active clients`, but the database stores `status = 'A'`
- user says `ultra clients`, but the database stores canonical enum values
- user says `EMEA`, but the database stores country codes or region ids

Without a dedicated value-mapping capability, SQL generation in `ai/v3` will remain weak on
real-world analytical questions even if:

- schema grounding exists
- SQL dialect grounding exists
- SQL validation/execution exists

This gap is visible in `ai/v1`, where `get-data`, `get-chart`, and `refine` prompts required
explicit `value-mapping` structures and placeholder handling.

## Goal

Define a reusable `ai/v3` capability that resolves user-facing phrases into structured value
mapping artifacts suitable for downstream SQL generation and refinement workflows.

The capability should:

- help the planner/LLM discover likely value mappings
- return structured mapping artifacts, not prose-only hints
- stay separate from `schema`, `sql-dialect`, and `sql-query`
- be reusable by future:
  - NL2SQL agents
  - chart agents
  - refine flows

## Capability Role

Recommended capability id:

- `value-mapping`

Recommended role split:

- `schema`
  - what entities and columns exist
- `value-mapping`
  - what stored values correspond to user-facing phrases
- `sql-query`
  - generate / validate / execute SQL

This keeps semantic value resolution separate from both schema grounding and SQL execution.

## Architectural Direction

Follow the same pattern as other `ai/v3` capabilities:

- `ValueMappingCapabilityProvider`
- `ValueMappingCapabilityDependency`
- `ValueMappingCapability`
- `ValueMappingToolHandlers`

The capability class should be wiring-only:

- descriptor
- manifest loading
- dependency extraction
- tool binding

Tool implementation logic should live in `ValueMappingToolHandlers`.

## Dependency Boundary

The capability should depend on an application-owned lookup boundary rather than embedding all
mapping logic in prompts.

Recommended dependency wrapper:

```kotlin
data class ValueMappingCapabilityDependency(
    val resolver: ValueMappingResolver,
) : CapabilityDependency
```

Where:

```kotlin
interface ValueMappingResolver {
    fun resolve(
        phrase: String,
        entityId: String? = null,
        columnId: String? = null,
    ): List<ValueMappingCandidate>
}
```

Exact interface shape may differ, but the dependency should remain:

- framework-free
- testable
- externalizable later

## Tool Set

The first implementation should stay compact.

### 1. `resolve_value_mapping`

Purpose:

- resolve one user-facing phrase against a schema/entity/column context

Suggested input:

```json
{
  "phrase": "ultra clients",
  "entityId": "MONETA.CLIENTS",
  "columnId": "MONETA.CLIENTS.SEGMENT"
}
```

Only `phrase` should be required initially.

Suggested output:

```json
{
  "phrase": "ultra clients",
  "matches": [
    {
      "entityId": "MONETA.CLIENTS",
      "columnId": "MONETA.CLIENTS.SEGMENT",
      "storedValue": "ULTRA",
      "displayValue": "ultra clients",
      "confidence": 0.93,
      "kind": "constant"
    }
  ]
}
```

### 2. `get_column_value_mappings`

Purpose:

- discover known or likely mappings for a specific column

Suggested input:

```json
{
  "columnId": "MONETA.CLIENTS.SEGMENT"
}
```

Suggested output:

- list of known values / labels / aliases for that column

This helps the LLM inspect the semantic space before generating predicates.

## Structured Artifacts

This capability should define a canonical structured mapping artifact, for example:

```json
{
  "artifactType": "value-mapping",
  "phrase": "ultra clients",
  "entityId": "MONETA.CLIENTS",
  "columnId": "MONETA.CLIENTS.SEGMENT",
  "storedValue": "ULTRA",
  "displayValue": "ultra clients",
  "kind": "constant",
  "confidence": 0.93
}
```

This artifact should be stable enough to feed later into:

- SQL generation
- SQL refinement
- chart refinement

## Protocols

The first implementation should support at least one structured protocol:

- `value-mapping.result`

Recommended mode:

- `STRUCTURED_FINAL`

This keeps the mapping result machine-readable and reusable in later agent steps.

## Planner Interaction

The planner should use this capability when:

- the user refers to business-facing values rather than obvious stored literals
- a refine turn introduces or changes filter values
- SQL generation needs to disambiguate human terms into stored constants

Examples:

- `show ultra and wealth clients`
- `filter only active customers`
- `group by client segment and keep only premium`

The planner should not assume mapping is always needed. It should use the capability when the
semantic gap between user phrase and stored value is likely non-trivial.

## Relation to `ai/v1`

This capability is the `ai/v3` replacement for the ad hoc value-mapping responsibilities embedded
in `ai/v1` prompts such as:

- `get-data`
- `get-chart`
- `refine`

In `ai/v3`, that logic should move into:

- reusable capability tools
- structured mapping artifacts
- planner-chosen steps

instead of remaining prompt-only behavior.

## Test Strategy

Primary tests:

- unit tests for `ValueMappingToolHandlers`
- capability tool-contract tests for manifest + dependency wiring

Test focus:

- phrase-to-value resolution returns structured results
- column-scoped discovery returns deterministic results
- unknown phrases return empty or low-confidence deterministic outputs
- outputs serialize cleanly as structured artifacts

## Acceptance Criteria

- A new `value-mapping` capability exists in `ai/v3`.
- The capability follows the standard provider/dependency/wiring/handlers pattern.
- The capability exposes at least:
  - `resolve_value_mapping`
  - `get_column_value_mappings`
- The capability returns structured value-mapping artifacts.
- The design is reusable by future:
  - SQL generation
  - chart generation
  - refine flows

## Deliverables

- This work item definition (`docs/workitems/WI-071-ai-v3-value-mapping-capability.md`).
- `value-mapping` capability implementation in `ai/mill-ai-v3-capabilities`.
- Manifest definition for prompts, tools, and protocol(s).
- Unit and tool-contract tests.
