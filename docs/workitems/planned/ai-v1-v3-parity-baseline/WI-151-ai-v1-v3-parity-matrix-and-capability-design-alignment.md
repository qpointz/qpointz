# WI-151 — AI v1 / v3 Parity Matrix and Capability Design Alignment

Status: `planned`  
Type: `📝 docs` / `💡 improvement`  
Area: `ai`  
Milestone: `0.8.0`  
Backlog refs: `A-21`, `A-22`, `A-23` (alignment only)

## Problem Statement

**0.8.0** targets **AI v1 feature parity** on the **v3** stack, but “parity” is not yet a single
checklist. The legacy stack (`mill-ai-core`, `mill-ai-nlsql-chat-service`) and v3
(`mill-ai-v3`, chat service, UI) must be compared explicitly: chat lifecycle, NL→SQL flows,
memory, streaming, errors, and admin surfaces.

Separately, the gap versus `docs/design/ai/capabilities_design.md` (protocol streaming,
orchestration, reasoner descriptions) should be clear so work is not duplicated or mis-prioritized.

## Goal

1. Produce a **v1 ↔ v3 parity matrix** (user-visible and operator-relevant behaviours): **done**,
   **partial**, **missing**, **out of scope for 0.8.0** — with pointers to code or endpoints per row.
2. Produce a concise **inventory** of v3 capability-related code paths (registry, manifests,
   agents) and map each to design concepts: tools, prompts, protocols, streaming lifecycle, and
   follow-up backlog links.

## In Scope

1. **Parity matrix** — trace v1 features through controllers, chat memory, and NL-to-SQL paths in
   `mill-ai-nlsql-chat-service` / `mill-ai-core`; map to v3 equivalents or gaps.
2. Enumerate v3 providers, manifests, and YAML assets under `mill-ai-v3` (and related test fixtures).
3. Document **implemented today**, **partial**, and **not started** relative to
   `docs/design/ai/capabilities_design.md`.
4. Cross-link relevant backlog rows (**A-21**–**A-23**) and agentic design notes where they apply.

## Out of Scope

- Implementing new runtime features (covered by other **0.8.0** AI stories under `planned/`).
- MCP server work (**A-56**).

## Acceptance Criteria

- A short design note exists under `docs/design/agentic/` (or `docs/design/ai/`) that includes:
  - the **v1 ↔ v3 parity matrix** (normative for 0.8.0 AI scope);
  - the **capability inventory** and gap list versus `capabilities_design.md`.
- The note explicitly calls out dependencies between this deliverable, the **`planned/ai-v3`** story,
  and other **0.8.0** AI stories (`ai-sql-generate-capability`, `ai-value-mapping-capability`, etc.)
  without duplicating their deliverables.

## Deliverables

- This work item definition.
- The design note file created in **In Scope**.
