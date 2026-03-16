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
    fun getMappedAttributes(tableId: String): List<MappedAttribute>

    fun resolveValues(
        tableId: String,
        attributeName: String,
        requestedValues: List<String>,
    ): List<ValueResolution>
}
```

Exact interface shape may differ, but the dependency should remain:

- framework-free
- testable
- externalizable later
- scoped to already chosen table/attribute coordinates from schema/query planning

## Tool Set

The first implementation should stay compact.

### 1. `get_value_mapping_attributes`

Purpose:

- tell the LLM which attributes of a chosen table are governed by value mapping

Suggested input:

```json
{
  "table": "MONETA.CLIENTS"
}
```

Suggested output:

```json
{
  "table": "MONETA.CLIENTS",
  "attributes": [
    {
      "attribute": "STATUS",
      "mapped": true
    },
    {
      "attribute": "SEGMENT",
      "mapped": true
    },
    {
      "attribute": "CLIENT_ID",
      "mapped": false
    }
  ]
}
```

Detailed behavior:

- The table must already be chosen by schema/query planning.
- This tool does not choose columns for the LLM.
- It only answers: for this already chosen table, which attributes require value lookup.
- The output should stay simple. The LLM does not need internal implementation details such as
  static-vs-dynamic source classification.

Recommended input shape:

```json
{
  "table": "MONETA.CLIENTS"
}
```

Recommended output shape:

```json
{
  "table": "MONETA.CLIENTS",
  "attributes": [
    {
      "attribute": "STATUS",
      "mapped": true
    },
    {
      "attribute": "SEGMENT",
      "mapped": true
    },
    {
      "attribute": "COUNTRY",
      "mapped": true
    },
    {
      "attribute": "CLIENT_ID",
      "mapped": false
    }
  ]
}
```

Behavior notes:

- A compact variant is also acceptable:

```json
{
  "table": "MONETA.CLIENTS",
  "mappedAttributes": ["STATUS", "SEGMENT", "COUNTRY"]
}
```

- If the table has no mapped attributes, return an empty list.
- The tool may be backed by metadata facets, RAG, or another resolver implementation, but those
  details should remain hidden from the LLM-facing contract.

Planner usage:

- Use after schema/query planning has chosen the table and candidate attributes.
- Use before generating predicates with user-provided constants.
- If an attribute used in the query is marked `mapped: true`, call `get_value_mapping` for the
  specific user values on that attribute.

Failure / ambiguity behavior:

- Unknown table: structured not-found result or tool error.
- The tool must not infer alternative tables.

### 2. `get_value_mapping`

Purpose:

- map one or more user values for a specific chosen attribute to stored database values

Suggested input:

```json
{
  "table": "MONETA.CLIENTS",
  "attribute": "SEGMENT",
  "values": ["ultra", "wealth"]
}
```

Suggested output:

```json
{
  "table": "MONETA.CLIENTS",
  "attribute": "SEGMENT",
  "results": [
    {
      "requestedValue": "ultra",
      "mappedValue": "ULTRA"
    },
    {
      "requestedValue": "wealth",
      "mappedValue": "WEALTH"
    }
  ]
}
```

Detailed behavior:

- The table and attribute must already be chosen by schema/query planning.
- The tool must not infer or suggest attributes.
- One call may resolve multiple values for the same attribute.
- This directly supports `IN (...)`, repeated `OR` predicates, and refine turns that add more
  values for an already chosen attribute.
- The tool is authoritative for stored constants on mapped attributes.

Planner usage:

- Use only when the query already involves a concrete table and attribute.
- Use when an attribute marked as mapped participates in a filter or other constant-bearing
  expression.
- Call once per mapped attribute with all requested user values for that attribute.

Recommended output shape:

```json
{
  "table": "MONETA.CLIENTS",
  "attribute": "COUNTRY",
  "results": [
    {
      "requestedValue": "Schweiz",
      "mappedValue": "ISO-CH"
    },
    {
      "requestedValue": "Deutschland",
      "mappedValue": "ISO-DE"
    }
  ]
}
```

Partial resolution example:

```json
{
  "table": "MONETA.CLIENTS",
  "attribute": "COUNTRY",
  "results": [
    {
      "requestedValue": "Schweiz",
      "mappedValue": "ISO-CH"
    },
    {
      "requestedValue": "Deutschland",
      "mappedValue": "ISO-DE"
    },
    {
      "requestedValue": "DACH",
      "mappedValue": null
    }
  ]
}
```

Behavior notes:

- Array-based results are preferred over a raw map because they handle partial failures more
  explicitly and can be extended later with confidence or explanation fields.
- If a value cannot be resolved, return `mappedValue: null` for that entry rather than guessing.
- The tool may use metadata-backed mappings, RAG, dynamic lookup, or other application logic
  internally.

Failure / ambiguity behavior:

- Unknown table or attribute: structured not-found result or tool error.
- If the attribute is not mapped, either:
  - return all entries with `mappedValue: null`, or
  - return a structured error indicating that the attribute is not value-mapped.
- The tool should never fabricate a mapped value.

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

## How `ai/v1` Actually Worked

`ai/v1` combined three different mechanisms:

### 1. Prompt-level placeholder discipline

The `get-data`, `get-chart`, and `refine` prompts forced the model to:

- replace every constant with a placeholder like `@{SCHEMA.TABLE.COLUMN:name}`
- emit a parallel `value-mapping` array
- avoid putting raw user literals directly into SQL

This gave the model a structured place to declare intended constants, but it did **not**
guarantee those constants were correct.

### 2. Post-generation value resolution

After SQL generation, `MapValueProcessor` walked the emitted `value-mapping` entries and replaced
placeholders by calling `ValueMapper.mapValue(...)`.

That means the final SQL could be corrected after generation, but only for values the model had
already decided to represent as placeholders.

### 3. Optional tool-based lookup during generation

`ai/v1` also introduced `value-mapping-tool`, which let the model ask for a mapped constant during
generation time. This was directionally correct, because it moved constant resolution from prompt
guessing into a runtime lookup boundary.

However, this tool remained generic and detached from schema-grounding. The schema metadata shown to
the model did not explicitly say which columns had controlled vocabularies or value mappings.

## How Value Mapping Was Reflected In Metadata

In the metadata model, value mapping lives on the **attribute** through `ValueMappingFacet`.

Current facet shape:

- `context`
- `similarity-threshold`
- `mappings[]`
  - `user-term`
  - `database-value`
  - `display-value`
  - `description`
  - `language`
  - `aliases[]`
- `sources[]`
  - SQL-backed population sources for dynamic mappings

Operationally, `ai/v1` ingested these metadata entries into a vector-backed `ValueRepository`.
Each mapping became a lookup document keyed by the fully qualified target column:

- target = `[SCHEMA, TABLE, COLUMN]`
- value = canonical stored database value
- text = user term + aliases + attribute context + description

So the metadata system already carried the right business signal, but the NL2SQL prompts and schema
messages did not expose that signal cleanly enough to make tool usage mandatory.

## Key Weakness In `ai/v1`

The main weakness was not absence of mapping data. It was the **control path**:

- schema grounding told the model what columns exist
- prompts told the model to emit placeholders
- a tool existed to resolve values
- but nothing made the model reliably understand:
  - which columns are value-mapped columns
  - when a lookup is required
  - that mapped constants must come from a tool, not model inference

As a result, `ai/v1` could still partially hallucinate:

- the target column for a phrase
- the resolved constant
- whether mapping was needed at all

## v3 Capability Direction

The important capability-level rule for `ai/v3` should be:

- the LLM may infer that a phrase probably needs value mapping
- the LLM must **not** invent the stored constant
- the stored constant must come from a `value-mapping` tool result

So `value-mapping` should become the authoritative runtime boundary for constants, not just a
helper after SQL generation.

## Recommended Improvements Beyond The Initial Tool Set

The first implementation should keep the LLM-facing contract small:

- `get_value_mapping_attributes(table)`
- `get_value_mapping(table, attribute, values[])`

That gives the model only the information it actually needs:

- for a chosen table, which attributes are mapped
- for a chosen mapped attribute, what stored values correspond to the requested user values

The implementation can still evolve behind that contract.

### Option A. Make RAG / hinting an internal concern of `value-mapping`

The capability can combine multiple hint sources behind one dependency boundary:

- direct metadata inspection of `ValueMappingFacet`
- indexed/RAG lookup over mapping aliases and descriptions
- dynamic source awareness
- column-level context once schema/query planning has already chosen the target column

This can still be represented as one application-facing dependency, for example:

```kotlin
data class ValueMappingCapabilityDependency(
    val resolver: ValueMappingResolver,
    val hintProvider: ValueMappingHintProvider,
) : CapabilityDependency
```

Where:

```kotlin
interface ValueMappingHintProvider {
    fun getMappedAttributes(tableId: String): List<MappedAttribute>

    fun resolveValues(
        tableId: String,
        attributeName: String,
        requestedValues: List<String>,
    ): List<ValueResolution>
}
```

The implementation may use schema tools/services internally, but callers should only see the
two compact `value-mapping` tools.

### Option B. Keep schema support optional and replaceable

If the first implementation wants schema-backed hinting, that is fine, but it should stay behind
the `value-mapping` boundary.

In other words:

- good: `value-mapping` handler reads schema metadata or calls schema services internally
- risky: planner must call `schema` to discover whether value mapping exists
- risky: `schema` tool outputs become permanently coupled to value-mapping-specific semantics

That keeps concerns looser and makes it easier to replace schema-backed hints later with a better
semantic index or domain resolver.

## Suggested Planner / Prompt Contract

The planner and capability prompts should state a hard rule:

- First identify target tables and columns through schema/query planning.
- After the relevant table is chosen, call `get_value_mapping_attributes(table)` if the query
  contains user-provided constants.
- If an attribute used in the query is marked as mapped, do not write a stored literal until
  `get_value_mapping(table, attribute, values[])` has been called.
- If no mapping is found, surface uncertainty instead of guessing.

This is stronger than `ai/v1`, where the model was mainly instructed to produce placeholder
structures and the system tried to repair values afterwards.

## Recommended Artifact Split

To support auditable downstream use, separate:

- `displayPhrase`: exact user/business phrase
- `resolvedStoredValue`: canonical database value returned by tool
- `targetColumnId`
- `resolutionSource`
  - `static-metadata`
  - `dynamic-source`
  - `fallback-none`
- `confidence`

This makes it obvious whether SQL literals came from an actual lookup or from an unsafe fallback.

## Test Strategy

At minimum, tests should prove:

- a value-mapped column is discoverable as value-mapped through schema/support tooling
- the planner calls `value-mapping` tools before emitting SQL predicates for such columns
- stored constants in final SQL come from tool outputs, not model-authored literals
- refine flows re-resolve constants instead of carrying forward hallucinated literals
- absence of mappings produces explicit uncertainty rather than guessed constants

Primary tests:

- unit tests for `ValueMappingToolHandlers`
- capability tool-contract tests for manifest + dependency wiring

Test focus:

- mapped-attribute discovery returns deterministic results for a chosen table
- batch value resolution returns structured results in input-aligned order
- unknown phrases return `mappedValue: null` rather than guessed constants
- outputs serialize cleanly as structured artifacts

## Acceptance Criteria

- A new `value-mapping` capability exists in `ai/v3`.
- The capability follows the standard provider/dependency/wiring/handlers pattern.
- The capability exposes at least:
  - `get_value_mapping_attributes`
  - `get_value_mapping`
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
