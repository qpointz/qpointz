# Step-Back Interaction Model  
*(LLM interactions & data model only)*

This document describes the **Step-Back interaction pattern**:  
LLM calls, back-and-forth clarification, `reasoning-id`, and the data objects exchanged.

**Scope**
- Focuses only on **LLM interactions and data model**
- Infrastructure, persistence, storage, hot/cold layers are intentionally ignored

---

## 1. Core Concepts

### Step-Back
A **reasoning phase** whose goal is to:
- understand the user request
- detect ambiguity or missing information
- decide whether clarification is required

Step-Back **does not execute** queries, tools, or actions.

---

### Reasoning-ID
A **unique identifier** for a single reasoning attempt.

- Created **only when Step-Back pauses**
- Required to continue clarification
- Identifies *which reasoning state* is being resumed
- Invalidated once reasoning completes or is cancelled

Think of it as a **transaction ID for reasoning**.

---

## 2. High-Level Interaction Flow

```
User input
→ Step-Back (LLM)
→ (READY?) ──► yes → execution
          └─► no
              → reasoning-id created
              → clarification questions sent to user
              → WAITING_FOR_INPUT
              → user reply (with reasoning-id)
              → clarification interpretation (LLM)
              → Step-Back resume (LLM)
              → READY or WAITING again
```

---

## 3. Data Objects (Canonical)

> **Important note on JSON examples**  
> All JSON examples below show a **basic / minimal structure for demonstration purposes**.  
> In a real implementation, these objects **can be extended** with additional metadata, fields, confidence scores, schema hints, versioning, localization, policies, or domain-specific information.

---

### 3.1 StepBackResult (structured, internal)

Result of Step-Back reasoning.  
Used **only** for continuation and control.

```json
{
  "intent": "get_data",
  "schemaScope": "partial",
  "requiredTables": ["ORDERS"],
  "dimensions": ["country"],
  "metrics": ["revenue"],
  "missingInfo": ["time_range", "currency"],
  "confidence": 0.61
}
```

**Rules**
- Machine-readable
- No prose
- Never reconstructed from chat text
- Not user-facing
- Structure may be extended with additional reasoning metadata

---

### 3.2 ClarificationQuestion

```json
{
  "id": "time_range",
  "question": "Which time range should be used?",
  "expectedType": "TIME_RANGE"
}
```

Notes:
- `id` must be stable across turns
- `expectedType` defines normalization rules
- Additional attributes (e.g. enums, hints, priority) may be added

---

### 3.3 Reasoning State (conceptual)

The state tied to a `reasoning-id`.

```json
{
  "reasoningId": "r-8f3a2c",
  "status": "WAITING_FOR_INPUT",
  "round": 1,
  "stepBackResult": { "...": "see StepBackResult" },
  "questions": [ "... ClarificationQuestion ..." ]
}
```

Notes:
- This is a conceptual structure
- Fields may be freely extended
- Exists only to allow reasoning continuation

---

## 4. LLM Interaction Steps

### 4.1 Step-Back Reasoning (LLM)

**Purpose**
- Identify intent
- Determine completeness
- Produce structured `StepBackResult`
- Generate clarification questions if needed

**Input**
- User request
- Schema / domain context

**Output**
- `StepBackResult`
- Optional list of `ClarificationQuestion`
- A **separate user-facing explanation** (natural language)

---

### 4.2 Clarification Interpretation (LLM)

A **separate LLM step** with a narrow, constrained role.

**Purpose**
- Convert free-text user answers into structured values
- *Decorate* the existing StepBackResult

**Input (basic demo structure)**

```json
{
  "previousStepBackResult": { "...": "see StepBackResult" },
  "questions": [
    { "id": "time_range", "expectedType": "TIME_RANGE" },
    { "id": "currency", "expectedType": "CURRENCY" }
  ],
  "userAnswer": "Last 30 days, USD"
}
```

**Output (basic demo structure)**

```json
{
  "mappedAnswers": {
    "time_range": "LAST_30_DAYS",
    "currency": "USD"
  },
  "confidence": 0.94,
  "unmapped": []
}
```

**Hard rules**
- No re-reasoning
- No intent changes
- No new questions
- If unsure → return `unmapped`
- Structure may be extended with diagnostics or normalization details

---

### 4.3 Step-Back Resume (LLM)

**Purpose**
- Merge structured clarification answers
- Update completeness (`schemaScope`)
- Decide whether reasoning is complete

**Input**
- Previous StepBackResult
- Structured clarification answers

**Output**
- Updated StepBackResult
- Either:
  - `READY`
  - `WAITING_FOR_INPUT` (with new questions)

---

## 5. UI ↔ LLM Contract via Reasoning-ID

### 5.1 Pause (clarification required)

Assistant → UI:

```json
{
  "status": "WAITING_FOR_INPUT",
  "reasoningId": "r-8f3a2c",
  "questions": [ "... ClarificationQuestion ..." ]
}
```

UI must remember `reasoningId`.

---

### 5.2 Continue clarification

UI → backend → LLM:

```json
{
  "reasoningId": "r-8f3a2c",
  "action": "ANSWER_CLARIFICATION",
  "message": "Last 30 days, USD"
}
```

Without `reasoningId`, continuation is **not allowed**.

---

### 5.3 Cancel / reformulate

```json
{
  "action": "CANCEL_REASONING",
  "message": "Show revenue by country for last 30 days in USD"
}
```

Effect:
- Current reasoning attempt ends
- New Step-Back starts
- Old `reasoning-id` is discarded

---

## 6. Mandatory Invariants

1. Step-Back reasoning produces **structure**, not prose  
2. Clarification interpretation is a **separate LLM step**  
3. Step-Back resume never starts from scratch  
4. Continuation requires a valid `reasoning-id`  
5. Chat text is **narrative**, not **state**  
6. LLMs see **previous conclusions**, never past deliberation  

---

## 7. Final Mental Model

> **Step-Back decides what is missing**  
> **Clarification fills the gaps**  
> **Reasoning-ID binds user answers to a specific reasoning attempt**

This model enables deterministic, multi-turn, clarification-aware LLM workflows.
