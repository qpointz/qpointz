# Agentic AI Design

## Purpose

This directory captures the design decisions for the `ai/v3` agentic runtime.

Some documents still describe strategic direction, while others now document the implemented
baseline in the current proof-of-concept runtime.

The long-term target remains:

- a Kotlin-first, Spring-free core runtime
- multiple agent profiles built from reusable capabilities
- a shared routed-event and persistence model
- durable chat transcript and artifact records
- downstream observer and indexing lanes

## Strategic Goal

The strategic end state for `ai/v3` is not one generic chat agent.

It is a shared runtime for a family of context-bound agents aligned to the user-facing contexts
in `ui/mill-ui`, especially:

- `model`
- `knowledge`
- `analysis`

These context families should compose reusable capabilities while remaining bounded to their own
workflow semantics.

## Current Documents

| File | Purpose |
|------|---------|
| `v3-foundation-decisions.md` | Strategic architecture decisions and long-range design direction for `ai/v3` |
| `v3-runtime-roles.md` | Runtime role split for capabilities, planner, observer, and workflow ownership |
| `v3-interactive-cli.md` | Current behavior of the `mill-ai-v3-cli` manual inspection tool, including raw event rendering and artifact-observer prints |
| `v3-validation-harness.md` | Deterministic validation strategy for scenarios, event traces, and `testIT` layering |
| `v3-capability-manifest.md` | `CapabilityManifest` YAML schema reference for prompts, tools, and protocols |
| `developer-manual/v3-developer-manual.md` | Entry point for the ai/v3 developer manual, treating `ai/v3` as a framework for building chat and agent runtimes |
| `developer-manual/v3-developer-capabilities-profiles-and-dependencies.md` | How to build capabilities, providers, manifests, dependencies, and agent profiles |
| `developer-manual/v3-developer-runtime-events-persistence.md` | How runtime execution, routed events, transcript, artifacts, telemetry, and observers work together |
| `developer-manual/v3-developer-recipes.md` | Practical implementation recipes for adding tools, capabilities, profiles, artifact types, and observers |
| `developer-manual/v3-developer-testing-and-debugging.md` | Testing strategy and debugging playbook for ai/v3 framework development |
| `v3-v2-learnings.md` | Gap analysis between `ai/v2` and `ai/v3` |
| `v3-v1-rationale-and-guardrails.md` | Why `ai/v3` remains the correct base despite `v1` workflow parity pressure |
| `v3-v1-prompt-budget-comparison.md` | Prompt/context size comparison between `ai/v1` and `ai/v3` |
| `v3-authoring-protocol.md` | Protocol design for schema metadata authoring |
| `v3-conversation-persistence.md` | Implemented baseline split between chat transcript, model memory, and artifact persistence in `ai/v3` |
| `v3-chat-service.md` | Implemented unified chat metadata, runtime rehydration, SSE stream contract, and HTTP service facade for `ai/v3` |
| `v3-persistence-lanes.md` | Implemented persistence-lane architecture for `ai/v3`: model memory, routed events, transcript, artifacts, telemetry, and artifact observers |
| `v3-langchain4j-simplification-notes.md` | Simplification map for `LangChain4jAgent` and adjacent runtime seams |
| `v3-mill-ai-v3-data-boundary.md` | **`0.8.0`**: **`mill-ai-v3`** ports vs **`mill-ai-v3-data`** adapters (schema + SQL validation); **`mill-ai-v3-autoconfigure`** canonical wiring; **`mill-ai-v3-service`** primary consumer |
| `v3-agent-ui-and-portal-agents.md` | Direction: agent-builder UI and portal-defined agent bundles applied to local Mill instances |
