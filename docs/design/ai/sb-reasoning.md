# MILL — Step-Back Reasoning for NL→SQL  
## Conceptual Architecture (Reasoning Only)

This document defines the Step-Back Reasoning layer for AIMILL NL→SQL.  
It focuses exclusively on **query understanding**, **gap detection**, **clarification**, **metadata improvement**, and **high-quality single-query SQL generation**.  
Scenario workflows are intentionally excluded and covered in a separate document.

---

# 1. Purpose of Step-Back Reasoning

Step-Back Reasoning introduces a high-level abstraction layer *before* intent classification and SQL generation.  
Its goals:

1. Improve SQL quality through conceptual understanding.
2. Detect ambiguities and missing information early.
3. Identify metadata gaps and missing business definitions.
4. Provide interpretable reasoning.
5. Support metadata enrichment (concept evolution).

Step-Back forces the model to “zoom out”, restate the task, and evaluate what information is missing.

---

# 2. Step-Back Reasoning Principles

Before determining any NL→SQL intent, the model must:

### 2.1 Abstract the user query  
Produce a high-level reformulation of the task (abstract-task):

- What type of analysis is requested?  
- Which concepts and business entities are involved?  
- What relationships must logically exist?

### 2.2 Identify core concepts  
List domain concepts referenced or implied by the user.

### 2.3 Infer required relations  
Identify relationships needed to interpret the user query.

### 2.4 Detect ambiguities  
Find any unclear or underspecified aspects, such as:

- undefined metrics  
- unclear time ranges  
- ambiguous business terms  
- missing join paths  
- unclear data fields

### 2.5 Decide whether the query can be grounded  
If ambiguities block SQL generation → Step-Back requests clarification.

---

# 3. Step-Back JSON Structure

```json
"step-back": {
  "abstract-task": "High-level interpretation of the user's request.",
  "core-concepts": ["conceptA", "conceptB"],
  "required-concepts": ["entity1", "entity2"],
  "required-relations": ["tableA->tableB", "tableB->tableC"],
  "ambiguities": ["unclear definition of metric X", "missing date filter"],
  "needs-clarification": true
}
```

If `needs-clarification = true`, SQL generation must not continue.

---

# 4. Clarification Layer (Self-Ask)

When Step-Back finds insufficient information, the system returns:

```json
"need-clarification": true,
"reasoning-id": "new",
"questions": [
  "What definition of 'active user' should be used?",
  "Which date field should be considered for filtering?",
  "What time period is required?"
]
```
`reasoning-id` here is an **action keyword**, not the actual identifier; post-processing interprets it and attaches/clears the real id in the UI payload.

Rules:

- Maximum 3 questions  
- Questions must be specific, answerable, and aligned with user language  
- No SQL or intent classification may proceed until questions are answered
- `reasoning-id` is **not** minted by the LLM; it is produced in post-processing.  
  - `"new"` → post-processor creates a fresh reasoning-id and marks the session as paused for clarification.  
  - `"reset"` → post-processor clears/invalidates the previous reasoning-id because clarification is resolved.  
  - `"continue"` → reasoning is still incomplete after clarification; post-processor retains the previously issued reasoning-id and requests more user input.  
  - Any other value means the initial reasoning succeeded and `need-clarification = false` (no id creation).  
  - The created id must be echoed with any follow-up answer to resume the same reasoning attempt.

---

# 5. Reasoning Layer (After Step-Back)

If no critical ambiguities remain, the model proceeds to:

- intent classification: `get-data`, `get-chart`, `explain`, `enrich-model`, `refine`, `do-conversation`, `unsupported`
- identifying requiredTables
- assigning schemaScope & schemaStrategy
- generating explanation (same language as user)
- producing a stable `query-name`

Example reasoning output:

```json
"reasoning": {
  "intent": "get_data",
  "requiredTables": ["ORDERS", "CUSTOMERS"],
  "schemaScope": "partial",
  "schemaStrategy": "default",
  "query-name": "orders-by-customer",
  "explanation": "..."
}
```

---

# 6. Chain-of-Verification (Quality Checks)

Before generating SQL, the model performs logical verification:

```json
"verification": {
  "checks": [
    "Table CUSTOMERS exists",
    "JOIN CUSTOMERS.id = ORDERS.customer_id is valid",
    "Time filter is explicitly defined"
  ],
  "all-passed": true
}
```

If `all-passed = false` → return clarification questions or corrected reasoning.

Verification dramatically reduces SQL errors.

---

# 7. SQL Generation Layer

Only when:

- Step-Back is complete  
- Clarification (if needed) is resolved  
- Reasoning is valid  
- Verification is passed  

→ The model finally generates SQL strictly in JSON format.

---

# 8. Metadata Evolution Through Step-Back

Step-Back analysis reveals:

- missing business definitions  
- undefined metrics  
- unclear semantic concepts  
- schema gaps  
- inconsistent terminology  
- implicit relationships not captured formally  

These can be returned as:

```json
"metadata-gaps": [
  "Definition of 'LTV' missing",
  "Concept 'active user' not present in metadata model",
  "Relationship orders.product_id -> products.id should be documented"
]
```

The enrich-model intent can be used to iteratively refine schema documentation and semantic layers.

---

# 9. Benefits of Step-Back Reasoning

### ✔ Higher SQL accuracy  
### ✔ Early detection of errors and ambiguities  
### ✔ Deep interpretability  
### ✔ Progressive metadata improvement  
### ✔ Reduced LLM variability  
### ✔ Cleaner multi-pass dialogue  
### ✔ Strong foundation for scenario planning (in next document)

---

# 10. Summary

Step-Back transforms NL→SQL from a direct text-to-SQL generator into a robust semantic understanding engine.  
By separating abstraction, clarification, reasoning, verification, and SQL generation, AIMILL achieves:

- higher stability  
- higher correctness  
- metadata evolution  
- predictable behavior  

Scenario workflows build on top of this layer and are described separately.

---

# End of Document

## Implementation Tracking

### Milestones (manual review after each)
- [x] Step-Back reasoner implemented in isolation (new data models, prompts, and post-processors) without changing `ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/ChatApplication.java`; covered by unit/integration tests.
- [ ] Metadata surfaced and handled in clarifications (concepts/rules aligned with `enrich-model` intent; reasoner reacts to metadata gaps).
- [x] `ChatApplication` extended with feature flag to enable Step-Back reasoner.
- [x] UX changes to interact with Step-Back flow (partial — clarification message routing missing on backend).

Integration tests should target the Moneta model using the configuration in `apps/mill-service/application-moneta-local.yml`.

### Planned Step-Back Reasoner (Reasoner.java)
- [x] Add a new `StepBackReasoner` alongside `DefaultReasoner` (`ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/reasoners`) implementing `Reasoner` to orchestrate the Step-Back prompt/response pipeline before intent selection.
- [x] Ensure the Step-Back reasoner consumes the dedicated templates in `templates/nlsql/stepback/`, deserializes their output into typed models, and enriches the downstream `ReasoningResponse`.
- [x] Keep `DefaultReasoner` available as the fallback path; choose between them via the Step-Back feature flag so rollout stays safe.
- [x] Update `ChatApplication` (and builders such as `ChatProcessor`) to inject `StepBackReasoner` whenever the flag `mill.ai.nl2sql.reasoner=stepback` is set.
- [x] Cover the new reasoner with unit and integration tests (e.g., `ai/mill-ai-core/src/testIT/java/.../stepback/StepBackReasonerIntegrationTest`) to validate Step-Back JSON, clarification gating, and verification handling before passing control to intents.

### Data Model
- [x] Define typed Step-Back structures (abstract-task, core-concepts, required-relations, ambiguities, needs-clarification, metadata-gaps) in shared AI models (`ai/mill-ai-core/src/main/java/io/qpointz/mill/ai/nlsql/models/stepback`).
- [x] Add clarification payload type with max 3 questions and gating flag (`ClarificationQuestion`, `StepBackResponse`).
- [x] Model reasoning/verification payloads (intent, requiredTables, schemaScope/Strategy, query-name, checks, all-passed) — existing `ReasoningResponse`.
- [ ] Wire JSON/HTTP and gRPC/proto mappings for these payloads.
- [x] Add validation rules (non-empty ambiguities -> needs-clarification=true) via `SyncClarificationFlagProcessor`.
- [x] Cover serialization/validation with unit tests in `ai/mill-ai-core` (`StepBackPostProcessorsTest`).

### Prompting Layer
- [x] Create separated prompt templates for step-back abstraction, clarification (max 3 questions), reasoning, and verification (`ai/mill-ai-core/src/main/resources/templates/nlsql/stepback`).
- [ ] Enforce language mirroring and blocking when needs-clarification or verification failures occur (prompts mirror user language; blocking logic in StepBackCall).
- [x] Add guidance for handling clarification responses (evaluate sufficiency, combine with original query) — implemented in `StepBackReasoner.continueReasoning()`.

### Feature Flag
- [x] Introduce a config-driven feature flag to enable/disable the step-back layer; default is **off** (`mill.ai.nl2sql.reasoner=default`). Wire into AI pipeline configuration via `ValueMappingConfiguration.reasoner`.

### Pipeline & Guardrails
- [x] Insert step-back stage ahead of intent/SQL and branch to clarification when ambiguities exist (configurable via `mill.ai.nl2sql.reasoner`).
- [ ] Gate SQL generation on successful verification and resolved clarifications.
- [x] Ensure non-empty ambiguities automatically trigger needs-clarification=true (`SyncClarificationFlagProcessor`).
- [x] Keep intent classification aligned with current keys: `get-data`, `get-chart`, `explain`, `enrich-model`, `refine`, `do-conversation`, `unsupported`.
- [x] Implement intelligent clarification response handling (evaluate sufficiency, proceed or request more) — `StepBackReasoner.continueReasoning()`.

### Metadata Evolution
- [x] Surface metadata-gaps from step-back analysis in responses (included in `StepBackResponse.metadataGaps`).
- [ ] Normalize metadata-gaps to the same structures used by `enrich-model` (model/rule/concept/relation) so they can flow into enrichment without translation (shared under `io.qpointz.mill.ai.nlsql.models.enrichment`).
- [ ] Optionally enqueue/store gaps for enrichment workflows.

### Service Exposure
- [x] HTTP endpoints return step-back, clarification, reasoning, verification, and metadata-gap payloads in message content when enabled.
- [x] Provide backward compatibility via feature flag (`mill.ai.nl2sql.reasoner=default`, default off).
- [ ] **BLOCKING**: Support clarification response detection via request content metadata — `ChatProcessor.processRequest()` must extract `reasoning-id` from `request.content()` and use `ChatUserRequests.clarify()` instead of `.query()`.

### UX Implementation
- [x] Create ClarificationMessage component for natural language clarification display (`services/mill-grinder-ui/src/component/chat/intents/ClarificationMessage.tsx`).
- [x] Create AssistantMessage component for user-facing messages (`services/mill-grinder-ui/src/component/chat/intents/AssistantMessage.tsx`).
- [x] Update ChatMessageList to show user messages and handle clarification flow (`ChatMessageList.tsx`).
- [x] Hide StepBackCard for simple queries (debug-only) — no separate card; clarification flow only when `need-clarification=true`.
- [x] Track clarification context in ChatProvider and include in request content (`ChatProvider.tsx` — reasoningId tracked and sent).

### Telemetry & Observability
- [ ] Log step-back outputs and verification results with PII masking.
- [ ] Emit metrics for ambiguity frequency and clarification rates.

### Testing
- [x] Add prompt goldens and validator unit tests (`StepBackPostProcessorsTest`).
- [x] Add integration tests for clear queries, ambiguous queries (`StepBackReasonerIntegrationTest`).
- [x] Add tests for clarification response handling (sufficient vs insufficient) (`clarificationFlowResolvesPremiumDefinition`).
- [ ] Add integration tests for failed verification, metadata-gap emission.

### Rollout
- [x] Guard the step-back layer behind a feature flag (`mill.ai.nl2sql.reasoner`).
- [ ] Optionally start as non-blocking logging before enforcing blocking behavior.

---

## Remaining Work Summary

### Critical (blocking clarification flow)
1. **ChatProcessor reasoning-id passthrough**: `ChatProcessor.processRequest()` must extract `reasoning-id` from `request.content()` and call `ChatUserRequests.clarify(msg, reasoningId)` when present. Without this, clarification replies are treated as new queries.

### Important (feature completeness)
2. **Metadata-gap normalization**: Align `metadata-gaps` strings with `enrich-model` structures so they can feed enrichment workflows.
3. **Verification gating**: Gate SQL generation on verification pass; currently verification check structure exists in prompts but not enforced in pipeline.
4. **Language mirroring**: Enforce response language matching user query language in prompts.

### Nice to have
5. **Telemetry**: Structured logging with PII masking; metrics for ambiguity/clarification rates.
6. **Proto/gRPC mappings**: Wire step-back payloads to proto definitions for gRPC clients.
7. **Non-blocking mode**: Optionally log step-back results without enforcing clarification gating.

---

## Session Notes (current state)
**Branch: poc/step-back-reasoning**

- Core implementation complete: `StepBackReasoner`, `StepBackCall`, `StepBackResponse`, post-processors, prompt templates.
- Feature flag operational: `mill.ai.nl2sql.reasoner=stepback` enables Step-Back pipeline.
- UX components ready: `ClarificationMessage`, `AssistantMessage`, `ChatProvider` with clarification context.
- **Blocking issue**: Backend `ChatProcessor` does not extract `reasoning-id` from request content — clarification replies currently bypass the cached reasoning context.
- Integration tests passing: `StepBackReasonerIntegrationTest` covers initial reasoning, clarification detection, and clarification resolution flow.
