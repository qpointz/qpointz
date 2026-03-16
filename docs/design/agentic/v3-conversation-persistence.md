# Agentic Runtime v3 — Conversation Persistence

**Status:** Design
**Date:** March 2026
**Scope:** `ai/v3` conversation state — in-memory continuity, LLM memory, UX record

---

## 1. Two Distinct Persistence Concerns

Conversation persistence in `v3` covers two fundamentally different concerns that must be
designed and implemented separately.

### 1.1 LLM Chat Memory

What gets fed back into the model's context window so it can reason correctly across turns.

- Optimized for the **model**, not the user
- Token-budget bounded — context window is finite
- Can be lossy — windowed, summarized, or compressed as conversations grow
- May diverge from what was actually said (e.g. a summary replaces 20 turns)
- Contains model-facing artifacts that have no UX meaning: system prompts, raw tool
  call/result interleaving, compression summaries

### 1.2 UX Conversation Record

What gets stored so a user can resume, review, or continue a conversation across sessions.

- Optimized for the **user and UI**, not the model
- Complete and faithful — full fidelity of what happened
- Grows unboundedly as the conversation progresses
- Must support UI rendering: messages, tool outputs, results, timestamps
- Supports audit, sharing, and cross-session resume

---

## 2. Why They Must Be Separated

These two concerns appear combinable via a "projection" pattern: store the full UX record
once and derive LLM memory from it on each request. This holds for simple windowing but
breaks for any serious production runtime.

### 2.1 Summarization forces a second store

Once a conversation grows beyond a context window, LLM memory must compress old turns into
a summary. That summary:

- is produced by an LLM call — it is expensive to recompute
- must itself be stored between requests
- does not belong in the UX record (it is a model-facing artifact, not a user message)

At this point a second store exists whether it was designed in or not.

### 2.2 Different access patterns

|                   | LLM Memory              | UX Record                       |
|-------------------|-------------------------|---------------------------------|
| Read on           | Every request           | Conversation load / UI scroll   |
| Latency budget    | Sub-millisecond         | Hundreds of ms acceptable       |
| Size              | Bounded (context window)| Unbounded                       |
| Storage           | Hot cache / in-process  | Durable DB                      |
| Load strategy     | Full load every turn    | Paginated / lazy                |

For large conversations, loading thousands of UX record rows on every request to derive a
10-turn window is untenable. Separation lets LLM memory live in a fast, bounded store
independently of the full record.

### 2.3 Content diverges in both directions

LLM memory contains things that do not belong in the UX record:

- System prompts (re-injected each turn, not a user-visible message)
- Raw tool call/result interleaving that the model needs for ReAct reasoning
- Summarization artifacts

UX record contains things LLM memory does not need:

- Timestamps, user IDs, session metadata
- Ratings, reactions, UI annotations
- Structured protocol outputs (charts, tables, rendered artifacts)
- Audit trail of model version, capability set, run IDs

---

## 3. Target Architecture

```
ConversationRecord              UX-facing, durable, append-only, complete
  turns: List<ConversationTurn>   userInput, assistantAnswer, toolCallAudit, timestamp
        │
        │  (shared conversationId foreign key only)
        │
ConversationMemory              LLM-facing, managed separately, hot store
  window: recent N turns          derived from record — cheap, no extra storage
  summary: compressed old turns   stored — computed once per compaction, not recomputed
        │
        │  LlmMemoryStrategy.project(memory)
        ▼
List<ChatMessage>               what the model actually sees this request
```

The two stores share only a `conversationId`. They evolve and are accessed independently.

### 3.1 LlmMemoryStrategy

A pluggable projection from `ConversationMemory` to `List<ConversationMessage>`:

```kotlin
fun interface LlmMemoryStrategy {
    fun project(memory: ConversationMemory): List<ConversationMessage>
}
```

Built-in strategies:

| Strategy | Behaviour |
|----------|-----------|
| `FullHistoryStrategy` | Replay all turns — default, suitable for short conversations |
| `WindowStrategy(n)` | Last N turns only |
| `TokenBudgetStrategy(n)` | Fit within N tokens, drop oldest first |
| `SummaryBufferStrategy` | Summarize old turns, keep recent in full |

The strategy is a runtime concern — it does not affect storage schema.

---

## 4. Current State (WI-072)

WI-072 delivered **in-memory conversation continuity** for the CLI REPL. This is the
starting point, not the production shape.

`ConversationSession` currently serves both concerns simultaneously:

```kotlin
data class ConversationSession(
    val conversationId: String,
    val profileId: String,
    val messages: MutableList<ConversationMessage>,  // USER/ASSISTANT pairs only
)
```

This is appropriate for the CLI testing scope. The session is created once per CLI run,
lives in process, and is never persisted.

The agent (`SchemaExplorationAgent.run(session, input, listener)`) replays the session's
`USER`/`ASSISTANT` pairs into the LangChain4j messages list each turn, then appends the
completed turn back. This is a `FullHistoryStrategy` inlined into the agent.

### 4.1 What this covers

- Multi-turn context in the CLI REPL
- Refine-style follow-ups (`remove email`, `show as pie chart`) have prior context
- `/clear` command resets the session

### 4.2 What this does not cover

- Persistence across process restarts
- Cross-request state in an HTTP service
- Memory management for long conversations
- Full tool-call audit in the record
- UX record vs LLM memory separation

---

## 5. Recommended Persistence Work Item Split

When persistence work begins, treat the two concerns as separate tracks:

**Track A — UX Conversation Record persistence**

- Define `ConversationRecord` and `ConversationTurn` in `mill-ai-v3-core`
- Add `ConversationRecordStore` interface (load, save, append)
- Implement an in-process / JDBC store
- HTTP layer: accept `conversationId`, load record, return in response

**Track B — LLM Memory management**

- Extract `LlmMemoryStrategy` interface to `mill-ai-v3-core`
- Implement `WindowStrategy` and `TokenBudgetStrategy`
- Make strategy injectable into the agent run
- Defer `SummaryBufferStrategy` — requires LLM call, plan separately

These tracks can be worked in parallel or sequenced. Track A delivers UX value
(conversation resume). Track B delivers model quality (no context overflow on long
sessions). Neither depends on the other.

---

## 6. Relation to Current Types

| Current type | Future role |
|---|---|
| `ConversationSession` | Transitional — merges into `ConversationRecord` (UX) + in-process `ConversationMemory` (LLM) |
| `ConversationMessage` | Stays in core; used in both tracks with `MessageRole` as-is |
| `MessageRole` | Stays; `TOOL_RESULT` and `SYSTEM` become relevant in Track B |
| `RunState.conversationId` | Foreign key linking a single run to its `ConversationRecord` |
