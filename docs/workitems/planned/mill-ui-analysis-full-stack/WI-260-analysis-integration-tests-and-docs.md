# WI-260 — Analysis integration tests + docs sync

Status: `planned`  
Type: `test`, `docs`  
Area: `services`, `ui`  
Backlog refs: **U-13**

## Goal

Add **testIT** (or full-stack slice) covering **queries REST** + persistence, and sync **[`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md)** / mill-ui docs if behaviour or fields diverged during implementation. Include **Analysis Monaco editor** notes in mill-ui **ARCHITECTURE** / inventory if **WI-266** changed filenames or integration points.

## Scope

1. Gradle test suites per module conventions ([`CLAUDE.md`](../../../../CLAUDE.md)).
2. Update **MILESTONE** / story closure checklist when story completes ([`RULES.md`](../../RULES.md)).

## Acceptance

- CI green for new integration tests.
- No undocumented breaking changes vs published UI API contract.

## Depends on

**WI-256**, **WI-257**, **WI-258**, **WI-259**, **WI-266**
