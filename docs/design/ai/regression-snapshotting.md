# Regression via Full Conversation Snapshot — Summary

## Goal
Use **regression testing** to control and observe **token growth, memory behavior, cost, and latency risks** in a chat-based LLM system.

Regression is the *right moment* to collect large, verbose artifacts and analyze cumulative effects that are invisible in unit or runtime testing.

---

## Core Idea

A **reference conversation** is executed as a regression test.

At each **user turn**, the system captures a **full snapshot** of what is sent to the LLM:
- static system context
- retained memory
- current action/system/user prompts

This enables deterministic analysis of:
- token totals
- token growth per turn
- memory accumulation
- regressions between versions

---

## What Is a Reference Conversation

A reference conversation is a **structured, attributed sequence**, not just text.

Each step has:
- role (system / user / assistant / tool)
- purpose (reasoning, action, result, explanation, etc.)
- retention policy (keep / drop / summarize)

Example (conceptual):

```yaml
- type: system_default
- type: user_request
- type: assistant_reasoning (retained: false)
- type: assistant_action (retained: false)
- type: tool_result (retained: summary)
- type: assistant_response (retained: true)
