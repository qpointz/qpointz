# Agentic Runtime v3 — Authoring Protocol Layers

**Status:** Gap / follow-up design
**Date:** 2026-03-15
**Scope:** `ai/v3` schema metadata authoring — structured protocol boundaries deferred from WI-068 stage 1

---

## 1. Purpose

This document captures the deferred protocol design from WI-068 §5.1.

WI-068 stage 1 implemented a single-layer capture flow:

```
grounding tools → capture tool (args inferred inline) → STRUCTURED_FINAL synthesis
```

WI-068 §5.1 specifies a three-layer flow where each boundary is an explicit structured LLM
call with a JSON schema contract:

```
layer 1: planner intent        → { task, subtype, targetHints, needsClarification }
layer 2: authored request      → { subtype, target, payload, rationale }
layer 3: capture result        → { captureType, targetEntityId, serializedPayload, validationWarnings }
```

Only layer 3 (`schema-authoring.capture` / `STRUCTURED_FINAL`) was implemented in stage 1.
Layers 1 and 2 are the subject of this document.

---

## 2. Current state (stage 1)

`SchemaExplorationAgent` uses the LangChain4j native function-calling (ReAct) loop. Intent
classification is implicit — the model selects tools based on the system prompt and message
history rather than emitting a verifiable intent JSON. The authored-request extraction step
does not exist as a separate LLM call; the model generates capture tool arguments inline from
context.

The `schema-authoring.intent` and `schema-authoring.request` prompts exist in the manifest
but are injected as system-prompt context only — they are not invoked as explicit structured
protocol steps.

`PlannerDecision` carries `task` and `subtype` fields, but they are only populated by
`LangChain4jAgent.planWithModel()` (the JSON-schema planner path), not by
`SchemaExplorationAgent`.

---

## 3. Layer 1 — Planner intent protocol

### 3.1 Purpose

Force the model to commit to a verifiable intent classification before any schema grounding
or capture tool is called. This makes intent auditable and allows the runtime to reject
unsupported task/subtype combinations early.

### 3.2 Output schema

```json
{
  "task": "AUTHOR_METADATA",
  "subtype": "relation",
  "targetHints": ["orders", "customers", "customer_id"],
  "needsClarification": false
}
```

Fields:

| Field | Type | Required | Notes |
|---|---|---|---|
| `task` | string enum | yes | EXPLORE_SCHEMA / AUTHOR_METADATA / ASK_CLARIFICATION |
| `subtype` | string | when task=AUTHOR_METADATA | description / relation / future subtypes |
| `targetHints` | string[] | no | Entity names or fragments the model inferred from the user input |
| `needsClarification` | boolean | yes | true when the model cannot determine target unambiguously |

### 3.3 `targetHints` field gap

`PlannerDecision` does not currently carry `targetHints`. Adding it requires:

- `targetHints: List<String> = emptyList()` field on `PlannerDecision`
- Population in the planner JSON schema for agents that use `planWithModel()`
- No observer or executor changes needed

### 3.4 Implementation approach

In `SchemaExplorationAgent`, add a pre-loop intent classification step:

1. Build an intent-classification `ChatRequest` using the `schema-authoring.intent` prompt
   and a `ResponseFormat` with the intent JSON schema.
2. Parse `task`, `subtype`, `targetHints`, `needsClarification`.
3. If `needsClarification = true`, skip the tool loop and return `PlannerDecision.askClarification()`.
4. Store intent in `RunState` context for use by the observer and synthesizer.
5. Enter the ReAct tool loop as normal.

This requires one additional non-streaming LLM call per turn.

---

## 4. Layer 2 — Authored metadata request protocol

### 4.1 Purpose

After schema grounding and before calling a capture tool, the model produces a complete
structured authored request. This is a stable, subtype-specific JSON contract that:

- can be logged as a distinct audit event
- can be surfaced to the user for confirmation before the capture tool runs
- can be consumed by external systems independently of the capture result format
- provides a validation gate before the capture tool is invoked

### 4.2 Output schema — description

```json
{
  "subtype": "description",
  "target": {
    "entityType": "TABLE",
    "entityId": "retail.orders"
  },
  "payload": {
    "description": "Contains customer purchase orders.",
    "displayName": "Orders"
  },
  "rationale": "User provided an explicit business description."
}
```

### 4.3 Output schema — relation

```json
{
  "subtype": "relation",
  "target": {
    "sourceTableId": "retail.orders",
    "targetTableId": "retail.customers"
  },
  "payload": {
    "name": "orders_to_customers",
    "sourceColumnIds": ["retail.orders.customer_id"],
    "targetColumnIds": ["retail.customers.id"],
    "description": "Orders belong to customers through customer_id."
  },
  "rationale": "User explicitly described a customer linkage."
}
```

### 4.4 Implementation approach

In `SchemaExplorationAgent`, when the observer detects that grounding is complete and the
next action is a capture:

1. Build an authored-request `ChatRequest` using the `schema-authoring.request` prompt,
   the full message history (including grounding tool results), and a `ResponseFormat` with
   the subtype-specific authored-request JSON schema.
2. Emit an `AgentEvent` carrying the authored-request payload (new event subtype needed).
3. Pass the authored-request payload as arguments to the capture tool call rather than
   letting the model generate arguments inline.

This decouples capture tool argument construction from the ReAct loop and makes the
authored-request shape a first-class event observable in the CLI and test harness.

### 4.5 New `AgentEvent` subtype required

```kotlin
data class AuthoredRequest(
    val subtype: String,
    val payload: Map<String, Any?>,
) : AgentEvent()
```

This event would be emitted after the authored-request LLM call completes and before the
capture tool is invoked.

---

## 5. When to implement

These layers become necessary when one or more of the following is true:

- The capture result needs user confirmation before being committed (patch-review flow).
- External systems subscribe to authored-request events as a separate feed from capture results.
- Multiple authoring subtypes make the dynamic intent catalog required (§1.4 of WI-068).
- Audit requirements demand a verifiable intent classification separate from tool selection.
- The `schema-authoring` capability is exposed via an API and downstream consumers need a
  stable authored-request contract independent of the capture format.

Stage 1 of the authoring flow (in-memory capture, no persistence) does not require either
layer.

---

## 6. Follow-up work item scope

A follow-up WI covering layers 1 and 2 should include:

1. Add `targetHints: List<String>` to `PlannerDecision`.
2. Add layer 1 intent classification call to `SchemaExplorationAgent` (pre-loop).
3. Add layer 2 authored-request extraction call to `SchemaExplorationAgent` (pre-capture).
4. Add `AgentEvent.AuthoredRequest` event subtype to `mill-ai-v3-core`.
5. Wire `AgentEvent.AuthoredRequest` rendering in the CLI.
6. Add authored-request JSON schemas to `schema-authoring.yaml` as protocol declarations.
7. Add unit tests for intent classification parsing and authored-request schema validation.
8. Add integration test scenarios for the three-layer flow end to end.

This WI is a prerequisite for any patch-review or user-confirmation authoring flow.
