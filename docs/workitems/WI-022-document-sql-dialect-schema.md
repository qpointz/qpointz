# WI-022 — Fully Document SQL Dialect Schema (Design Docs)

Status: `completed`  
Type: `📝 docs`  
Area: `core`, `client`, `ai`, `platform`  
Backlog refs: `C-6`, `C-7`, `D-8` (documentation support)

## Problem Statement

Even with the new YAML schema and migrated dialect files, teams cannot use the model effectively
without one complete, implementation-oriented design reference. Current docs describe direction,
but need a canonical “how to describe any dialect” guide aligned with the new `core/mill-sql`
program and section 3/4 representability goals.

## Goal

Produce full design documentation for the SQL dialect schema so engineers can author, review, and
consume dialect definitions consistently across Kotlin runtime, server contracts, AI, and Python.

## In Scope

1. Expand `docs/design/client/sql-dialect-yaml-schema.md` into the canonical specification.
2. Add a normative section for required vs optional fields and defaulting behavior.
3. Add representability mapping tables:
   - section 3 requirement -> schema fields
   - section 4 gap id -> schema fields
4. Add complete examples for migrated dialects:
   - `POSTGRES`, `H2`, `CALCITE`, `MYSQL`
5. Add authoring rules:
   - naming conventions,
   - allowed enum/value sets,
   - unknown/unverified value policy,
   - deprecation policy for legacy fields.
6. Add consumer mapping notes:
   - Kotlin typed model,
   - gRPC/HTTP contracts,
   - AI dialect consumer,
   - Python consumer.
7. Add change-management section for future schema evolution.

## Out of Scope

- Implementing Kotlin runtime/model code.
- Implementing server endpoints or client behavior changes.
- Migrating additional dialects beyond documented examples.

## Implementation Plan

1. **Schema spec hardening**
   - Rewrite sections to clearly separate normative rules from examples.
2. **Coverage documentation**
   - Add explicit tables for section 3 and section 4 representability mapping.
3. **Examples**
   - Provide complete and validated example fragments for four reference dialects.
4. **Consumer guidance**
   - Document how each consumer reads/interprets schema sections.
5. **Governance**
   - Document versioning, deprecation, and review checklist for schema changes.

## Acceptance Criteria

- `sql-dialect-yaml-schema.md` is the single canonical dialect schema reference.
- Section 3 and section 4 representability mappings are fully documented.
- Four reference dialect examples are documented and aligned with migrated files.
- Authoring/deprecation/versioning rules are explicit and actionable.
- Consumers have clear mapping guidance from schema to runtime contracts.

## Test Plan (documentation quality)

- Cross-check every section 3 and section 4 item appears in mapping tables.
- Validate all YAML examples are parseable and conform to documented rules.
- Run documentation peer review checklist for ambiguity and completeness.

## Risks and Mitigations

- **Risk:** Docs diverge from runtime implementation details over time.  
  **Mitigation:** Add versioned change log and “update docs in same PR” policy.

- **Risk:** Examples become stale as migrations continue.  
  **Mitigation:** Treat examples as generated/validated artifacts where possible.

## Deliverables

- This work item definition (`docs/workitems/WI-022-document-sql-dialect-schema.md`).
- Fully updated `docs/design/client/sql-dialect-yaml-schema.md`.
- Coverage mapping tables and reference examples for four migrated dialects.
