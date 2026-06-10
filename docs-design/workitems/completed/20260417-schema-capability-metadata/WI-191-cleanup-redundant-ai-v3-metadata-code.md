# WI-191 — Remove redundant metadata/schema code from `mill-ai-v3*`

Status: `planned`  
Type: `🔧 change` / `🧹 cleanup`  
Area: `ai`  
Milestone: `0.8.0`

## Problem Statement

After **WI-188**–**WI-190**, some **`ai/mill-ai-v3`** and **`ai/mill-ai-v3-*`** code may be **superseded**: duplicate facet-type URN strings, dead branches, unused helpers, or leftover mapping that now lives under **`data/`** / **`metadata/`**.

## Goal

1. **Audit** `ai/mill-ai-v3`, `ai/mill-ai-v3-data`, `ai/mill-ai-v3-autoconfigure` (and other `mill-ai-v3-*` as needed) for **redundant** schema/metadata implementation.
2. **Delete** superseded code paths; **replace** imports with shared constants/APIs (`MetadataUrns`, `mill-data-metadata`, shared mappers from **WI-188**).
3. Ensure **no behavioral regression**: full relevant **`./gradlew`** test set for touched AI modules + any affected **`data`** tests.

## In Scope

- Deletions and rewrites that **reduce duplication** only — no unrelated refactors.

## Out of Scope

- Changing **WI-188** domain decisions (this WI assumes they are done).
- Documentation story closure steps (**`MILESTONE.md`**, **`BACKLOG.md`**) — handle at **story** closure per [`RULES.md`](../../RULES.md).

## Acceptance Criteria

- Grep/architecture review shows **no duplicate** facet merge or URN registries left in **`ai/*`** for concerns owned by **`data/`** / **`metadata/`**.
- CI green for affected modules.

## Deliverables

- Net-negative or neutral LOC in **`ai/mill-ai-v3*`** where redundancy existed; short note in **WI-191** body listing **removed** items.

## Depends On

- **WI-189** minimum; **WI-190** recommended so tests guard the cleanup.
