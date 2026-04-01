# WI-140 — Facet class demotion

**Story:** metadata-and-ui-improve-and-clean  
**Status:** Done  
**Type:** feat  
**Area:** metadata, data, ai

## Summary

Execute **SPEC §2**: remove **`MetadataFacet`** lifecycle (`merge` / `validate` / `setOwner`), drop **`FacetClassResolver`** / **`FacetConverter`**, demote concrete facet types to **plain data**, and update **all §2c consumers**. Optionally introduce **`FacetPayloadUtils`** or use inline conversion — decide and document in this WI.

Normative: [`SPEC.md`](SPEC.md) §0, §2, §5 (remaining choices).

## Scope

- **§2a–§2b** in **SPEC**.
- **§2c** table — all rows (schema core/service, ai v3 / v1, metadata autoconfigure).
- **Module split:** default keep types in **`mill-metadata-core`** unless technical need forces **`mill-metadata-types`** (**SPEC §5**).
- **JavaDoc / KDoc** on touched public APIs.

## Decisions (complete before closing WI — shrinks **SPEC §5**)

Record the outcome here (1–3 sentences + optional link to PR). Implementer **must** fill in:

| Decision | Choice (FacetPayloadUtils shared helper vs inline Jackson / other) | Date / PR |
|----------|---------------------------------------------------------------------|-----------|
| **FacetPayloadUtils** vs inline conversion in **§2c** consumers | **`FacetPayloadUtils`** in **`mill-metadata-core`** (`object` + `@JvmStatic convert`), backed by `JsonUtils.defaultJsonMapper().convertValue`. **`SchemaFacets.fromResolved`** and **`NlsqlMetadataFacets`** use it; **`SchemaExplorerService`** calls it for descriptive facet hydration. | 2026-04-01 |
| **`mill-metadata-core`** vs new **`mill-metadata-types`** module | **Keep** all demoted types and **`FacetPayloadUtils`** in **`mill-metadata-core`**; **`StructuralFacet`** / **`RelationFacet`** remain in **`mill-data-schema-core`**. | 2026-04-01 |

## Out of scope

- New **`MetadataSource`** features beyond what consumers need for compilation/tests.

## Dependencies

- **WI-132** through **WI-136** feature set stable enough to avoid repeated conflict (negotiate with branch owner if parallel work).

## Acceptance criteria

- No production reference to removed bridge types; facet payloads remain schema-driven.
- Tests for listed modules pass (run full relevant **`./gradlew`** and AI Gradle if **ai/** touched per repo norms).

## Testing

**§2c** always touches **`ai/`** modules — run **both** root Gradle and **`ai`** build (not optional):

```bash
./gradlew build
cd ai && ./gradlew test
```

**Note:** This repository’s **`ai/`** subtree has no standalone `gradlew`; AI modules are included in the **root** build. Verified with root **`.\gradlew.bat build`** and **`.\gradlew.bat :ai:mill-ai-v3:test :ai:mill-ai-v3-cli:test :ai:mill-ai-v1-core:test :ai:mill-ai-v1-nlsql-chat-service:test`**.

## §2c — ChatProcessor

No code changes: **`ChatProcessor`** does not reference **`FacetConverter`**; NL-SQL facet reads go through **`NlsqlMetadataFacets`** (updated).

## Commit

One logical `[feat]` commit (or split per **SPEC §5** only if this WI explicitly documents a split); update [`STORY.md`](STORY.md); clean tree.
