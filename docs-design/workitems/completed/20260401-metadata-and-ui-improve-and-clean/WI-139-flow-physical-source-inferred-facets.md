# WI-139 — Flow physical-source inferred facets (deferred)

**Story:** metadata-and-ui-improve-and-clean (follow-up story)  
**Status:** Deferred  
**Type:** feat  
**Area:** data

## Summary

**Not executed on this branch.** Future work: a **`MetadataSource`** for **physical** / **flow** (and similar) backends that surfaces non-secret descriptor facts (paths, storage type, reader types, etc.) with **per-source safety** rules co-authored when the source lands.

Normative intent: [`SPEC.md`](SPEC.md) §3g, §6, §7; this WI is a **placeholder** for planning the follow-up story.

## Scope (when picked up)

- Implement read-only inferred facets from flow/source descriptors.
- **Sanitization / redaction** decided **with** that backend — no global matrix.
- **Dependencies:** **WI-132**, **WI-137**, pattern from **WI-138**.

## Dependencies (this branch)

None — out of critical path.

## Note

Keep file for historical sketch; execution tracker lives in the **follow-up** story’s **`STORY.md`**.
