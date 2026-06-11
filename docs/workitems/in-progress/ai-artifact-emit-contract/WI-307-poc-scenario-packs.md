# WI-307 — POC scenario packs (primary acceptance)

Status: `planned`  
Type: `🧪 test` / `✨ feature`  
Area: `ai`  
Story: [`STORY.md`](STORY.md)

## Depends on

- **WI-302** (harness runner).
- **WI-304–306** (emit contract + profiles).

## Goal

**Primary story acceptance:** YAML scenario packs prove SQL and inferred-facet emit with full turn snapshots and baseline comparison.

## Deliverables

**Scenario packs (`ai/mill-ai-test/src/test/resources/scenarios/artifact-emit/`):**

- [ ] `data-analysis-sql-emit.yml` — POC A: events, artefacts (`sql.generated`), SSE `partType: sql`, shape checks
- [ ] `schema-authoring-facet-emit.yml` — POC B: `inferred-facet` / `facet-proposal`
- [ ] `data-analysis-no-facet.yml` — negative: no facet artefact on `data-analysis`
- [ ] `schema-authoring-sql-and-facet.yml` — optional multi-turn both kinds

**Baselines (`src/test/resources/scenarios/baselines/`):**

- [ ] Committed `*.record.normalized.json` for each POC pack (not raw records)
- [ ] Normalization per WI-301: scrub ids, timestamps, token stats; preserve structure
- [ ] Document `UPDATE_BASELINES=1` refresh flow in design doc
- [ ] Comparator test: two identical scripted runs produce identical normalized JSON

**Checks (register if missing in WI-301):**

- [ ] `artifacts.shape` — JSON path matchers
- [ ] `artifacts.has-kind` / count by `persistKind`
- [ ] `artifacts.json-schema-ref` — optional protocol `finalSchema` validation

**Integration:**

- [ ] `ArtifactEmitScenariosIT extends ScenarioPackTestBase` in `ai/mill-ai-test/src/testIT/`

## Acceptance criteria

- [ ] `./gradlew :ai:mill-ai-test:testIT` — all POC packs green.
- [ ] Comparator passes against committed **normalized** baselines.
- [ ] POC SQL pack asserts **both** `sql.validation` (count 1) and `sql.generated` (count 1) — not duplicate generated.
- [ ] Each run writes full `*.record.json` under `build/reports/scenarios/`.
- [ ] Records contain complete `outcome` slices (events, artefacts, SSE, transcript) per design doc.

## Notes

This WI is the **definition of done** for artefact emit behaviour — not optional extras.
