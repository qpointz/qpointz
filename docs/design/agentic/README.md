# Agentic AI Design

## Purpose

This directory captures the design decisions for the planned `ai/v3` agentic runtime.

The immediate goal is to define a clean, Kotlin-first, LangChain4j-based platform for
building a family of context-bound agents in Mill without coupling the core runtime to
Spring.

This directory primarily captures design decisions for the planned `ai/v3` agentic runtime,
with some documents now also describing the current proof-of-concept implementation shape.

## Current Documents

| File | Purpose |
|------|---------|
| `v3-foundation-decisions.md` | Summary of current architecture and POC decisions for `ai/v3` |
| `v3-runtime-roles.md` | Runtime role split for capabilities, planner, observer, and Hello World example |
| `v3-interactive-cli.md` | Design and usage of the `mill-ai-v3-cli` interactive testing tool |
| `v3-validation-harness.md` | Deterministic validation strategy for scenarios, event traces, and `testIT` layering |
