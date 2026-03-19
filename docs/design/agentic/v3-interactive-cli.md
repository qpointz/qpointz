# Agentic Runtime v3 - Interactive CLI

**Status:** Implemented  
**Date:** March 2026  
**Scope:** `ai/mill-ai-v3-cli` as the manual inspection surface for the current `v3` runtime

## 1. Purpose

The CLI is a manual runtime inspection tool.

It is used to:

- run `v3` agents interactively against a real model
- inspect the raw `AgentEvent` stream
- exercise multi-turn chat memory and transcript behavior
- observe protocol outputs and downstream artifact-observer prints during development

It is not the production Chat API surface.

## 2. Current Behavior

### Agent support

The CLI currently supports:

- `hello`
- `schema`

The selected agent runs with a persistent `ConversationSession` for the life of the CLI process.

The `schema` surface is currently backed by the combined schema authoring runtime rather than a
read-only exploration-only profile. In other words, the CLI selector remains `schema`, but the
effective runtime identity is `schema-authoring`.

### Event display model

The CLI renders raw `AgentEvent` values, not routed events.

Current rendering split:

- `MessageDelta`
  - streamed inline as agent text
- `ProtocolTextDelta`
  - streamed inline as protocol text
- `ReasoningDelta`
  - streamed in a bordered reasoning block
- `ProtocolFinal`
  - printed as structured JSON payload
- `ProtocolStreamEvent`
  - printed as structured JSON payload
- `LlmCallCompleted`
  - printed as token counts
- all other events
  - rendered by generic JSON serialization

### Structured payloads

The CLI docs used to show stringified tool/protocol payloads. That is no longer accurate.

The current event model carries structured payloads for:

- `ToolCall.arguments`
- `ToolResult.result`
- `ProtocolFinal.payload`
- `ProtocolStreamEvent.payload`

So CLI output now shows structured JSON rather than nested JSON strings where the runtime
already normalized the payload.

## 3. Persistence and Observer Visibility

The CLI is still driven by raw `AgentEvent`, but the runtime beneath it now also wires:

- transcript persistence
- artifact persistence
- run-event persistence
- telemetry accumulation
- a default `NoOpArtifactObserver`

The no-op artifact observer currently prints normalized indexing requests after artifact
persistence succeeds.

This means CLI runs may now show two kinds of output:

- formatted `AgentEvent` output handled by `CliApp.kt`
- direct artifact-indexer print lines emitted by the default no-op observer

Those observer prints are currently a development aid, not a formal CLI rendering surface.

## 4. Commands

Current commands:

- `/help`
- `/exit`, `/quit`, `exit`, `quit`
- `/clear`

`/clear` resets the live `ConversationSession` and clears `ChatMemoryStore`.

It does not currently reset the separate in-memory persistence stores created inside a distinct
`AgentPersistenceContext` unless the caller wires a shared context and clears it explicitly.

## 5. Environment

Current relevant environment variables:

- `OPENAI_API_KEY`
- `OPENAI_MODEL`
- `OPENAI_BASE_URL`
- `AGENT`
- `SCHEMA_SOURCE`

## 6. Current Role in Development

The CLI is the easiest way to inspect:

- chat-stream behavior
- protocol output behavior
- token accounting events
- artifact creation
- no-op artifact indexing requests

It complements automated tests, especially for:

- manual inspection of event ordering
- real-model behavior
- schema/authoring experimentation

## 7. Current Limitations

The CLI does not yet:

- render routed events directly
- render persisted transcript state directly
- show telemetry accumulator totals as a separate view
- format artifact-observer prints as first-class CLI events
- expose relation indexing output beyond the current no-op observer print

Those are future ergonomics improvements, not missing core runtime functionality.
