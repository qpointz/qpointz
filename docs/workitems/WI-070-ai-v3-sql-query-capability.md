# WI-070 - AI v3 SQL Query Capability

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: `A-61`

## Problem Statement

`ai/v3` now has the foundations for capability composition, schema grounding, dialect grounding,
and structured protocol output, but it still lacks the capability that turns those ingredients
into actual SQL work.

Future data agents need a single capability family that can:

- generate SQL
- validate SQL
- execute SQL

and produce a structured SQL artifact that the runtime and later UX can recognize explicitly as
"generated SQL" rather than plain assistant text.

The downstream SQL engine cannot be assumed to return rich structured validation diagnostics.
In practice, validation may only provide:

- pass / fail
- a free-text error message

The capability/runtime must therefore wrap these minimal validator results in stable structured
artifacts without depending on engine-native structured errors.

## Goal

Define the first `ai/v3` SQL query capability that provides:

- SQL generation
- SQL validation
- SQL execution
- a structured generated-SQL artifact contract
- a structured validation-result contract
- a structured SQL result-reference contract

The capability should be designed so that later UX layers can detect and render generated SQL
reliably from structured output, not by scraping prose.

## Architectural Direction

This capability should be a new `ai/v3` capability, separate from:

- `schema`
- `sql-dialect`

Recommended capability id:

- `sql-query`

Recommended role split:

- `schema`
  - what entities exist
- `sql-dialect`
  - how SQL should be written for the current engine
- `sql-query`
  - generate, validate, and execute SQL

This keeps grounding separate from action.

## Dependency Boundary

The capability should not embed SQL generation or execution logic directly in prompts.

It should depend on application-owned services/adapters that expose:

- SQL validation
- SQL execution

Recommended dependency wrapper in `ai/mill-ai-v3-capabilities`:

```kotlin
data class SqlQueryCapabilityDependency(
    val validator: SqlValidationService,
    val executor: SqlExecutionService,
) : CapabilityDependency
```

Exact service names may differ, but the capability should depend on explicit collaborators rather
than building everything into one class.

This work item should also include mock implementations of:

- `SqlValidationService`
- `SqlExecutionService`

These mocks are only for local/manual validation and tests. Real integration with the downstream
query/result service is explicitly out of scope.

## Tool Set

The first implementation should expose these tools and interaction model.

### 1. SQL generation is a model action, not a tool

The LLM should generate SQL as part of a planned action using structured output.

This step should produce the structured generated SQL artifact directly.

Generation should not be modeled as an application tool because it is:

- model-native
- context-sensitive to schema + dialect + user goal
- best represented as a structured planner/output boundary

### 2. `validate_sql`

Purpose:
- validate a SQL statement before execution

Expected output:
- structured validation result
- `passed: true|false`
- free-text validation message
- optional normalized SQL if available

### 3. `execute_sql`

Purpose:
- execute validated SQL against the current query engine

Expected output:
- execution result reference artifact
- optional `resultId`
- result metadata only
- statement artifact linkage back to the SQL that was executed

The tool should not return full result rows to the agent runtime by default.

The execution side effect should be modeled as:

- send validated SQL to a downstream query/result boundary
- store or materialize the result outside the chat runtime
- return only a structured result reference artifact

## Structured SQL Artifact

This work item should explicitly define a structured object representing generated SQL.

Recommended stage-1 shape:

```json
{
  "artifactType": "generated-sql",
  "sql": "select * from retail.orders limit 10",
  "dialectId": "CALCITE",
  "statementKind": "select",
  "source": "generated",
  "validationWarnings": []
}
```

Required fields:

- `artifactType`
- `sql`
- `dialectId`
- `statementKind`
- `source`

Optional fields:

- `validationWarnings`
- `notes`
- `parameters`

The important part is that this object must be stable and explicit enough that later UX can
recognize it as generated SQL without inspecting free text.

## Structured Validation Result Artifact

The validator result should use a structured wrapper even if the underlying SQL engine only
returns a free-text error.

Recommended stage-1 shape:

```json
{
  "artifactType": "sql-validation",
  "passed": false,
  "message": "Column CUSTOMER_NAME not found",
  "attempt": 2,
  "normalizedSql": null
}
```

Required fields:

- `artifactType`
- `passed`
- `attempt`

Optional fields:

- `message`
- `normalizedSql`

This contract should not assume structured downstream engine diagnostics.

## Structured SQL Result Reference Artifact

This work item should also define a structured execution-result reference artifact.

Recommended stage-1 shape:

```json
{
  "artifactType": "sql-result",
  "statementId": "stmt_123",
  "resultId": "res_456",
  "rowCount": 1250,
  "columns": [
    {"name": "customer_id", "type": "STRING"},
    {"name": "revenue", "type": "DECIMAL"}
  ],
  "truncated": false
}
```

Required fields:

- `artifactType`
- `statementId`

Optional fields:

- `resultId`
- `rowCount`
- `columns`
- `truncated`
- `notes`

The important rule is:

- `resultId` may be present when the result service persisted the execution result
- `resultId` may be absent for non-durable or expired results

In both cases the event still represents a SQL execution result artifact that the client can
reason about.

This design intentionally separates:

- durable conversation artifacts
- non-durable query results

The durable part is the generated SQL statement artifact. The query result itself is non-durable
by default and may need to be requeried later.

## Interaction Model

The intended runtime loop for this capability is:

1. LLM generates structured SQL artifact
2. runtime calls `validate_sql`
3. if validation passes:
   - runtime calls `execute_sql`
4. if validation fails:
   - runtime feeds the last SQL and the validator message back to the LLM
   - LLM attempts to revise SQL
   - runtime validates again
5. after `N` failed validation attempts:
   - ask clarification if the failure appears to reflect ambiguity
   - otherwise fail safely with a structured SQL generation failure artifact

Recommended initial bound:

- `maxValidationAttempts = 3`

This loop should not require structured engine diagnostics; the free-text validation message is
the corrective signal provided back to the model.

After `N` failed validations:

- if the runtime/observer detects ambiguity in the user intent or unresolved schema grounding,
  it should ask clarification
- otherwise it should stop safely and return a structured SQL generation failure artifact

The runtime must never execute SQL that failed validation.

## Client Interaction Contract

The client/UX should receive structured events/artifacts that include:

- generated SQL artifact
- validation artifact(s)
- optional result reference artifact

The generated SQL artifact is the durable conversation artifact and must be enough to identify the
statement later.

If `resultId` is present:

- the client can retrieve the actual data out-of-band through the result service

If `resultId` is absent or expired:

- the client should requery through the result service using the durable SQL artifact

The result service implementation is not part of this work item, but all capability structures and
events should assume that boundary exists.

## Durability and Reuse Model

This capability should be designed for the planned `ai/v3` persistence model:

- conversations and conversation artifacts are durable
- query results are non-durable by default

That means:

- the generated SQL artifact should be durable and reusable
- the validation artifact may be durable as run history
- the execution result reference artifact is ephemeral and may expire

When a past conversation is reopened later:

- the stored generated SQL artifact should be enough to requery through the result service
- a missing or expired `resultId` should not invalidate the conversation artifact
- the system should not need to call the LLM again just to recover the SQL statement

This is also the intended foundation for later chart persistence:

- a future chart capability may persist chart specification artifacts durably
- reopened conversations should be able to reuse the stored SQL artifact and stored chart spec
- actual query data should still be fetched or requeried out-of-band, not restored from chat memory

So the durable boundary for this work item is:

- SQL statement and related structured artifacts

and the non-durable boundary is:

- query result payloads

## SQL Generation Failure Artifact

When the bounded validation loop fails, the runtime should return a structured failure artifact.

Recommended shape:

```json
{
  "artifactType": "sql-generation-failure",
  "lastSql": "select customer_name from retail.orders",
  "attempts": 3,
  "lastValidationMessage": "Column CUSTOMER_NAME not found",
  "resolution": "clarification_or_manual_review_required"
}
```

This gives the runtime and future UX a stable representation of failure without requiring
structured engine errors.

## Result Service Boundary

`execute_sql` should be defined as a side-effecting tool that sends the validated SQL statement
to a result service.

The result service is responsible for:

- executing SQL
- storing execution results when configured to do so
- returning a result reference

This work item should assume that such a result-service boundary exists, but it should not
implement the result service itself.

The capability/runtime should not embed result rows into:

- LLM-visible tool outputs beyond lightweight metadata
- chat memory
- conversation persistence

This is required to avoid:

- data leaking to the LLM
- full datasets becoming part of chat history/memory

### Client / UX contract

The client should receive a structured event that includes:

- generated SQL artifact
- optional SQL result reference artifact

If `resultId` is present:
- client may fetch the result directly through the result service

If `resultId` is missing:
- client should treat the execution as requiring requery when data is needed again

This is important for past conversations where:

- chat messages remain durable
- result storage may be non-durable or expired

In those cases, the conversation should still retain the generated SQL artifact so the client can
re-run the query via the result service when the user reopens the chat.

Durability rule for this work item:

- conversations are durable
- query results are non-durable by default

The capability contract should therefore treat the SQL artifact as the durable conversation
artifact, and the result reference as an optional short-lived execution artifact.

## Protocol Requirement

This capability should use structured output for SQL artifacts.

Recommended protocols:

- `sql-query.generated-sql`
- `sql-query.validation`
- `sql-query.result-ref`

Mode:

- `STRUCTURED_FINAL`

These protocols should carry:

- generated SQL artifact
- validation result artifact
- SQL result reference artifact

The generated SQL protocol may also be reused after validation if the validated SQL becomes the
canonical statement.

The runtime should treat this as a first-class artifact, not just a text message.

## Capability / Handler Pattern

Follow the same architecture as schema and planned sql-dialect capability:

- `SqlQueryCapabilityProvider`
- `SqlQueryCapabilityDependency`
- `SqlQueryCapability`
- `SqlQueryToolHandlers`

Responsibilities:

- `SqlQueryCapability`
  - manifest loading
  - dependency extraction
  - tool wiring
  - protocol declaration
- `SqlQueryToolHandlers`
  - pure mapping/adapter logic around validation and execution services
  - no runtime/capability orchestration logic

## In Scope

1. Define the capability shape and dependency boundary.
2. Define model-driven SQL generation as a structured planner/output step.
3. Define `validate_sql` and `execute_sql`.
4. Define the generated SQL artifact schema.
5. Define the validation result artifact schema.
6. Define the structured protocols for SQL/query artifacts.
7. Ensure generated SQL is surfaced as a first-class structured artifact.
8. Ensure SQL execution results are surfaced as structured result-reference artifacts, not row payloads.
9. Keep the first implementation suitable for later UX recognition of generated SQL and requery behavior.

## Out of Scope

- Charts and visualization.
- Result explanation/narrative quality.
- Multi-step reconciliation or anomaly workflows.
- Full UI rendering.
- SQL persistence/history UI.
- Result-grid retrieval API details.
- Result service implementation details or storage internals.

## Recommended Runtime Flow

For a future NL2SQL-style agent, the expected flow is:

1. ground schema using `schema`
2. ground dialect using `sql-dialect`
3. LLM generates structured SQL artifact
4. call `validate_sql`
5. if valid, call `execute_sql`
6. if invalid, revise SQL and validate again up to the configured limit
7. return:
   - generated SQL artifact
   - validation artifact
   - execution result reference artifact
   - final answer

The generated SQL artifact should be emitted before or alongside execution so that downstream UX
can always show the produced statement explicitly.

The execution result artifact should include `resultId` only when the result service persisted the
result. Otherwise the client must treat the SQL artifact as the durable source for requery.

The capability, handlers, and emitted events should therefore be designed as if the result service
already exists, even though implementing that service is outside the scope of this item.

## Manifest / Resource Shape

The capability should have a manifest resource, for example:

- `ai/mill-ai-v3-capabilities/src/main/resources/capabilities/sql-query.yaml`

It should declare:

- capability id: `sql-query`
- prompt assets for SQL generation/validation guidance
- tool schemas for:
  - `validate_sql`
  - `execute_sql`
- protocols:
  - `sql-query.generated-sql`
  - `sql-query.validation`
  - `sql-query.result-ref`

## Test Strategy

Tests should be split into:

### Handler tests

Target `SqlQueryToolHandlers` directly.

Prefer:
- deterministic fake validation/execution services
- deterministic fake result service

because this capability orchestrates multiple collaborators and needs predictable assertions.

Required coverage:
- `generate_sql` returns structured generated SQL artifact
- `validate_sql` returns structured pass/fail result plus free-text message
- invalid SQL correction loop can be tested deterministically with fake validator messages
- `execute_sql` links result reference artifact to the executed SQL artifact
- `execute_sql` returns metadata without embedding full result rows
- `execute_sql` handles both persisted (`resultId` present) and requery (`resultId` absent) cases
- bounded validation failure returns structured failure artifact

### Capability wiring tests

Verify:
- dependency extraction
- manifest-driven tool wiring
- generated SQL protocol presence
- validation protocol presence
- result-reference protocol presence

## Acceptance Criteria

- A new `sql-query` capability exists in `ai/v3`.
- The capability exposes:
  - `validate_sql`
  - `execute_sql`
- The capability follows the standard provider/dependency/capability/handlers pattern.
- A structured generated SQL artifact is explicitly defined.
- A structured validation result artifact is explicitly defined.
- A structured SQL result reference artifact is explicitly defined.
- The generated SQL artifact is emitted through a structured protocol, not just plain text.
- The validation result artifact is emitted through a structured protocol.
- The SQL result reference artifact is emitted through a structured protocol.
- The artifact contract is stable enough for later UX recognition of generated SQL.
- The design ensures execution results are not embedded into LLM/chat memory as full datasets.
- The design supports a bounded LLM revision loop driven by pass/fail + free-text validator messages.
- Unit tests cover handler logic and structured SQL/validation/result-reference artifact generation.

## Deliverables

- This work item definition (`docs/workitems/WI-070-ai-v3-sql-query-capability.md`).
- `sql-query` capability implementation in `ai/mill-ai-v3-capabilities`.
- Structured generated SQL, validation, and SQL result reference artifact contracts.
- Handler unit tests for validation, execution, and result-reference paths.
