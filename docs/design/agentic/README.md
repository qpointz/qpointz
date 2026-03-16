# Agentic AI Design

## Purpose

This directory captures the design decisions for the planned `ai/v3` agentic runtime.

The immediate goal is to define a clean, Kotlin-first, LangChain4j-based platform for
building a family of context-bound agents in Mill without coupling the core runtime to
Spring.

This directory primarily captures design decisions for the planned `ai/v3` agentic runtime,
with some documents now also describing the current proof-of-concept implementation shape.

## Strategic Goal

The strategic end state for `ai/v3` is not one generic chat agent.

It is a shared runtime for a family of context-bound agents aligned to the user-facing contexts
in `ui/mill-ui`, especially:

- `model`
- `knowledge`
- `analysis`

These context families should be able to compose reusable capabilities while remaining bounded to
their own workflow semantics.

`v3` should also support cross-cutting enrichment/authoring behavior across those contexts, such as:

- amending descriptions and relations
- proposing metadata enrichments
- introducing or refining concepts inferred from conversation
- surfacing structured proposals for review/promotion instead of only returning answers

This implies:

- one shared runtime and event model
- durable, persisted conversations and run records
- multiple agent profiles and likely multiple planner/observer families
- reusable capabilities that can be composed differently by context
- explicit separation between read/explain flows and enrichment/authoring flows

## Current Documents

| File | Purpose |
|------|---------|
| `v3-foundation-decisions.md` | Summary of current architecture and POC decisions for `ai/v3` |
| `v3-runtime-roles.md` | Runtime role split for capabilities, planner, observer, and Hello World example |
| `v3-interactive-cli.md` | Design and usage of the `mill-ai-v3-cli` interactive testing tool |
| `v3-validation-harness.md` | Deterministic validation strategy for scenarios, event traces, and `testIT` layering |
| `v3-capability-manifest.md` | `CapabilityManifest` YAML schema reference — one file per capability, tool and prompt declaration format |
| `v3-v2-learnings.md` | Gap analysis between `ai/v2` and `ai/v3` — what is worth porting, what is not, and recommended implementation order |
| `v3-authoring-protocol.md` | Deferred three-layer protocol design for schema metadata authoring (WI-068 §5.1 gap) — planner intent, authored-request, and capture result as explicit structured boundaries |
| `v3-conversation-persistence.md` | Two-track persistence model — LLM chat memory vs UX conversation record, why they must be separate, target architecture, and recommended work item split |
