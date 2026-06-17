# WI-309 — Story closure checklist (reference only)

Status: `done` — **not part of agent execution order**  
Type: `📐 docs`  
Area: `ai`, `docs`  
Story: [`STORY.md`](STORY.md)

## Important

Per [`RULES.md`](../../RULES.md) → **Explicit closure only (agents)**:

**Do not perform any step in this WI** — archive, MILESTONE, BACKLOG, squash — until the **user explicitly asks** to close the story (e.g. “close the story”, “story is merge-ready”, “archive the story”).

This file is a **checklist for humans** at merge-ready time. Agents completing WI-300–308 must **not** treat WI-309 as the next implementation task.

## Preconditions

- **WI-300–308** complete and verified (WI-310 optional — not required for closure unless user expands scope).
- User has **explicitly requested** story closure.

## Checklist (user-triggered)

- [x] Finalize design docs (no stale WI-300 sketches):
  - [`ai-v3-conversation-scenarios.md`](../../../design/agentic/ai-v3-conversation-scenarios.md)
  - [`artifact-emit-contract.md`](../../../design/agentic/artifact-emit-contract.md)
- [x] Update [`MILESTONE.md`](../../MILESTONE.md) — WI-300–308
- [x] Update [`BACKLOG.md`](../../BACKLOG.md) — mark related rows `done`; add deferred follow-ups:
  - sql-result-view visualization respin (**U-15** `done` via `ai-sql-view-restart`)
  - HTTP scenario runner (**A-93** `backlog`)
  - `ai:v3-integration` CI job (**A-94** `backlog`)
  - live-LLM YAML packs (**A-95** `backlog`)
- [x] Note in [`ai-v1-integration/README.md`](../../../design/ai/ai-v1-integration/README.md) reimplementation checklist progress (scenario matrix → `mill-ai-test`)
- [x] Mark all **execution-order** boxes `[x]` in [`STORY.md`](STORY.md) (WI-300–308)
- [x] Archive: `docs/workitems/completed/20260616-ai-artifact-emit-contract/`
- [x] MR-ready squash: 12 commits above merge base on story branch (within ~10 guideline; optional further squash before merge)
- [x] Clean working tree before push (`git status`)

## Verification commands (record in closure notes)

```bash
./gradlew :ai:mill-ai-test:test :ai:mill-ai-test:testIT
./gradlew :ai:mill-ai:test :ai:mill-ai-service:testIT
```
