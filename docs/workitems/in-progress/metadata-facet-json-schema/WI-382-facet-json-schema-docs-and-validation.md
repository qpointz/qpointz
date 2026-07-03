# WI-382 — Facet JSON Schema Docs and Verification

Status: `planned`  
Type: `docs`  
Area: `metadata`, `ui`, `docs`

## Problem Statement

Generated JSON Schema changes the external facet type contract. Future consumers need to know what
the schema validates, what is annotation-only, and where Mill semantics still live outside JSON
Schema.

## Goal

Document the JSON Schema projection and run the focused verification suite for metadata and UI.

## Scope

1. Update metadata design documentation to describe JSON Schema generation.
2. Update public/admin-facing metadata docs if needed to mention the read-only UI view.
3. Record the intended limitation: shape validation only.
4. Run focused Gradle and UI test/build commands.

## Acceptance Criteria

- Design docs explain `FacetPayloadSchema` as source of truth and JSON Schema as projection.
- Docs list supported schema keywords and `x-mill-*` annotations.
- Docs explicitly exclude Mill policy validation from JSON Schema.
- Local verification commands and results are recorded in the final handoff.

## Test Plan

- `./gradlew.bat --no-daemon :metadata:mill-metadata-core:test :metadata:mill-metadata-service:test`
- `cd ui/mill-ui && npm run test`
- `cd ui/mill-ui && npm run build`
