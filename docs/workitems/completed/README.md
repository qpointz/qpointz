# Completed stories (archive)

Closed stories are **moved** here from `docs/workitems/<story-slug>/` — they are **not** deleted.

## Naming

Each archived story is a folder:

```text
docs/workitems/completed/YYYYMMDD-<story-slug>/
```

- **`YYYYMMDD`** — closure date (UTC unless your team standard says otherwise): the day the story is merge-ready or merged.
- **`<story-slug>`** — same lowercase hyphen-separated name the folder had while active.

## Finding the latest closures

Lexical **ascending** sort (A→Z) on folder names orders **older** dates before **newer** ones. To list **most recent first**, sort folder names **descending** (Z→A) in your file browser or IDE.

Optionally, keep a **newest-first** bullet list below as a human-maintained index (most recent at the top).

---

## Index (optional, newest first)

- [2026-04-14 — ai-v3-chat-capability-dependencies (`CapabilityDependencyAssembler`, profile HTTP, IT/docs, CLI HTTP bench; WI-160, WI-167–WI-169)](20260414-ai-v3-chat-capability-dependencies/STORY.md)
- [2026-04-14 — ai-v3-schema-exploration-port (`SchemaCatalogPort`, `mill-ai-v3-data`, `SqlValidator`; WI-161–WI-166)](20260414-ai-v3-schema-exploration-port/STORY.md)
- [2026-04-13 — ai-sql-generate-capability (`sql-query` generate-only, SqlValidator, CLI; WI-156–WI-159)](20260413-ai-sql-generate-capability/STORY.md)
- [2026-04-02 — flow-source-ui-facets (flow inferred facets, mill-ui; WI-146–WI-150)](20260402-flow-source-ui-facets/STORY.md)
- [2026-04-02 — typed-entity-urns (typed model URNs; WI-144 → eliminate-entity-kind)](20260402-typed-entity-urns/STORY.md)
- [2026-04-01 — metadata-and-ui-improve-and-clean (multi-origin facets, UI cleanup)](20260401-metadata-and-ui-improve-and-clean/STORY.md)
- [2026-03-30 — metadata-rework (greenfield URN)](20260330-metadata-rework/STORY.md)

---

Normative rules: [`../RULES.md`](../RULES.md) (Story closure).
