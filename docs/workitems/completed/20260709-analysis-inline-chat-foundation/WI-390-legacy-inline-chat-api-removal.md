# WI-390 - Legacy inline-chat API removal

## Goal

Hard remove legacy inline-chat API references before building the new Analysis copilot behavior.

## Status

**Complete** — legacy `POST /api/v1/inline-chat/messages` removed from API requirements, backlog
(B-20), and architecture docs. Unified `/api/v1/ai/chats/**` documented as the only inline transport.
Code already used `chatService`; no code changes required.

## Requirements

- Remove documentation that presents `POST /api/v1/inline-chat/messages` as available, planned, or
  backward-compatible.
- Remove backlog rows that request implementation of the legacy inline-chat endpoint.
- Remove frontend service helpers, mocks, tests, or feature references that depend on a legacy
  inline-chat endpoint if any still exist.
- Keep the unified chat API as the only normative inline-chat transport:
  - create/session flow through `POST /api/v1/ai/chats`
  - message streaming through the existing unified chat/SSE path
  - per-turn host context through optional `context.values`
- Do not add a deprecation path, compatibility alias, or fallback adapter for the legacy endpoint.
- Update design/backlog docs before artifact-strip and host-action implementation WIs consume the
  API contract.

## Acceptance Criteria

- No story, design, public, backlog, or API documentation describes the legacy inline-chat endpoint
  as available or planned.
- No code path, mock path, or test in `ui/mill-ui` depends on `POST /api/v1/inline-chat/messages`.
- Inline chat documentation names the unified chat service as the only transport.
- Documentation states that legacy inline-chat API removal is intentional hard removal, not
  deprecation.

## Verification

```bash
rg -n "/api/v1/inline-chat|legacy inline-chat endpoint|inline-chat/messages" docs ui/mill-ui
cd ui/mill-ui && npm run test -- --run
```
