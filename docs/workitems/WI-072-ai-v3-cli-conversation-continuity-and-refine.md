# WI-072 - AI v3 CLI Conversation Continuity and Refine Support

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: `A-63`

## Problem Statement

The current `ai/v3` CLI behaves like a sequence of isolated single-turn runs.

This means:

- each user input is sent as a fresh turn
- previous user messages are not preserved
- previous assistant/tool outputs are not preserved
- refine-style requests cannot work reliably

Observed symptom:

- a follow-up question like `show it as a pie chart`
- or `remove country`
- or `filter only active clients`

loses the context of the previous turn and is interpreted as a standalone request.

## Known Current Cause

The reason is visible in the current code.

In [CliApp.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-cli/src/main/kotlin/io/qpointz/mill/ai/cli/CliApp.kt):

- each line is handled by `runTurn(runTurnFn, input)`
- `runTurnFn` delegates to `agent.run(input, listener)`
- there is no conversation/session object shared across turns

In [SchemaExplorationAgent.kt](/home/vm/wip/qpointz/qpointz/ai/mill-ai-v3-langchain4j/src/main/kotlin/io/qpointz/mill/ai/langchain4j/SchemaExplorationAgent.kt):

- `run()` creates a new `messages` list on every call
- that list starts from:
  - system prompt
  - current user message
- prior conversation history is not injected

So refine failure is not primarily an LLM problem. It is a runtime/CLI state problem.

## Goal

Define the first `ai/v3` conversation continuity model for the interactive CLI so that:

- multiple turns in one CLI session share conversation context
- refine-style follow-up questions can work
- the design aligns with planned durable conversation support later

This work item is about:

- in-memory session continuity first

not yet:

- durable persistence
- multi-user server conversation storage

## Architectural Direction

The CLI should stop treating each line as a brand-new run.

Instead it should maintain a session object that preserves:

- conversation id
- prior user messages
- prior assistant messages
- tool results / artifact references needed for refine
- profile / context identity

This state should be passed back into the agent on each turn.

## Recommended Runtime Shape

Introduce an in-memory conversation/session abstraction for manual CLI use, for example:

```kotlin
data class AgentConversationSession(
    val conversationId: String,
    val history: MutableList<ConversationTurn>,
    val profileId: String,
)
```

The exact type may differ, but the agent boundary should support:

- prior messages/history as input
- new events/messages appended after each turn

## Agent Integration

The agent API should evolve so a turn can run against an existing session or conversation state.

Example direction:

```kotlin
fun run(
    session: AgentConversationSession,
    input: String,
    listener: (AgentEvent) -> Unit = {},
): String
```

or equivalent stateful wrapper around the existing agent implementation.

The important point is:

- `run()` must no longer reconstruct message history from scratch on every turn

## Scope

In scope:

- in-memory session continuity for CLI
- preserve enough history for refine-like follow-ups
- explicit conversation id in the CLI session
- keep current streaming/event behavior

Out of scope:

- durable persistence storage
- replaying historical conversations from database/files
- cross-process session resume
- production chat-memory infrastructure

## Why This Matters

Without this change, the current CLI under-represents the real `ai/v3` target runtime.

It makes the model look weaker than it is because refine and continuation prompts depend on
history that the CLI throws away before each turn.

So this is both:

- a developer experience issue
- a POC-validation issue

## Relation to `ai/v1`

`ai/v1` refine flows were explicitly designed around conversation history and previous intent
context. The `ai/v1` reasoning prompts repeatedly instructed the model to inspect prior messages
when determining whether the user was refining a previous query.

`ai/v3` needs the runtime/session layer to make that possible before a true refine capability
can be evaluated fairly.

## Implementation Direction

Recommended stages:

1. add an in-memory conversation/session object for CLI
2. extend agent invocation to accept prior history
3. append each completed turn back into session state
4. ensure tool results/artifact refs needed for refine are preserved in memory
5. validate follow-up/refine prompts manually in CLI

## Validation Examples

Manual CLI sequences that should start working after this item:

1. `show top 10 clients by country`
2. `show it as a pie chart`

1. `describe orders table`
2. `add also relations`

1. `show clients`
2. `remove email and phone`

These do not require full production refine logic; they require preserved conversational context.

## Acceptance Criteria

- CLI supports multi-turn in-memory conversation continuity.
- Agent turns in one CLI session share history instead of starting from scratch.
- Refine-style follow-up prompts become possible in principle because prior context is preserved.
- The design aligns with planned durable conversation support but does not require persistence yet.

## Deliverables

- This work item definition (`docs/workitems/WI-072-ai-v3-cli-conversation-continuity-and-refine.md`).
- CLI/runtime design update for in-memory session continuity.
- Follow-up implementation in `ai/mill-ai-v3-cli` and relevant `ai/v3` agent boundaries.
