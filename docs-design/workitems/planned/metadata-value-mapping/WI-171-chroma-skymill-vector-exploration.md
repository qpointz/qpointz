# WI-171 — Chroma + Skymill vector exploration (mill-ai-v3-data)

Status: `in-progress` (core IT implemented; polish / follow-ups below)  
Type: `🔬 spike` / `✨ feature` (test-only deliverable)  
Area: `ai`, `data`  
Backlog refs: _(optional — link when story is tracked in BACKLOG)_

## Current implementation status (handoff — 2026-04-15)

**Done**

- **LangChain4j only** (no Spring AI): `testIT` dependency on `langchain4j-chroma` at BOM-aligned beta
  [libs.versions.toml](libs.versions.toml): `langchain4j-beta` = `1.11.0-beta19` → library
  `langchain4j-chroma`.
- **Integration test** [ChromaSkymillDistinctVectorIT.kt](ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/chroma/it/ChromaSkymillDistinctVectorIT.kt):
  - `@SpringBootTest` + profile `chroma-explore-skymill`.
  - Executes `SELECT DISTINCT \`state\` FROM \`skymill\`.\`cities\`` via [DataOperationDispatcher](data/mill-data-backend-core).
  - Ingests each value into [ChromaEmbeddingStore](https://github.com/langchain4j/langchain4j) with a
    **deterministic** in-test [HashExplorationEmbeddingModel](ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/chroma/it/ChromaSkymillDistinctVectorIT.kt)
    (384-d, SHA-256–based, L2-normalized) — **no OpenAI / Ollama** required.
  - **Chroma HTTP v2** required for `chromadb/chroma:latest`:
    `.apiVersion(ChromaApiVersion.V2)`; default tenant/database
    `.tenantName("default_tenant").databaseName("default_database")` (matches local single-node Chroma).
  - Unique collection per run: `mill-wi171-{uuid}`.
  - Asserts similarity search returns the probe state in the hit list.
- **Dedicated Spring slice** (does not touch SQL validator IT):
  [ChromaSkymillExploreItApplication.kt](ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/chroma/it/ChromaSkymillExploreItApplication.kt)
  — imports metadata + flow autoconfig + [DefaultServiceConfiguration](data/mill-data-autoconfigure/src/main/java/io/qpointz/mill/data/backend/configuration/DefaultServiceConfiguration.java)
  (no duplicate `SubstraitDispatcher` `@Bean` here).
- **Profile YAML** (isolated from default testIT `application.yml`):
  [application-chroma-explore-skymill.yml](ai/mill-ai-v3-data/src/testIT/resources/application-chroma-explore-skymill.yml)
  — same Skymill flow + metadata seed pattern as `sql-validator-skymill`.

**Run locally**

1. Start Chroma, e.g. from repo root:  
   `docker compose -f deploy/local-dev/docker-compose.yml up -d chromadb`  
   (default HTTP **localhost:8000**).
2. Enable the IT and run (PowerShell):

   ```powershell
   $env:MILL_CHROMA_IT_ENABLED = "true"
   .\gradlew :ai:mill-ai-v3-data:testIT --tests "io.qpointz.mill.ai.data.chroma.it.ChromaSkymillDistinctVectorIT"
   ```

3. Optional: `MILL_CHROMA_BASE_URL` (no trailing path junk) if Chroma is not on `http://localhost:8000`.

**CI / default `./gradlew testIT`**

- Intended gate: `@EnabledIfEnvironmentVariable(named = "MILL_CHROMA_IT_ENABLED", matches = "true")` so the
  class is **skipped** when unset.
- **Note:** that annotation is currently **commented out** in
  [ChromaSkymillDistinctVectorIT.kt](ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/chroma/it/ChromaSkymillDistinctVectorIT.kt)
  (line ~39) for frictionless local runs — **uncomment before merge** so CI does not require Chroma.

**Known / follow-up when you continue**

- **Metadata on ingest:** optional — use `TextSegment.from(text, Metadata)` + assert
  `match.embedded().metadata()` after search; add `import dev.langchain4j.data.document.Metadata`.
- **Semantic embeddings:** swap `HashExplorationEmbeddingModel` for e.g. LangChain4j OpenAI or
  in-process ONNX embedding module (extra `testIT` dep) if you want realistic similarity beyond exact
  probe text.
- **Other columns:** try `` `skymill`.`cities`.`city` `` or `airport_iata` per original WI list.
- **Metadata-filtered search:** `EmbeddingSearchRequest` + LangChain4j filter DSL (Chroma `where`
  mapping) — not exercised yet.
- **Story hygiene:** when this WI is finished, mark `[x]` in [STORY.md](docs/workitems/planned/metadata-value-mapping/STORY.md)
  and move folder per [RULES.md](docs/workitems/RULES.md) if not already in `in-progress/`.

## Problem Statement

Before committing to the WI-172 value-resolution bridge and vector-backed behaviour, we need a
small, repeatable integration path that: runs SQL against the Skymill flow dataset, turns distinct
column values into embeddable text, ingests them into ChromaDB, and validates similarity search.
Today `mill-ai-v3-data` testIT proves Skymill SQL parsing only; it does not execute queries or
touch Chroma.

## Goal

Add one env-gated `testIT` in `ai/mill-ai-v3-data` that spins with the existing Skymill flow
fixture, loads distinct values from a chosen column, creates embeddings, writes to a Chroma vector
store, and asserts that similarity search returns sensible neighbours — using **local-dev**
infrastructure (Docker Compose) rather than Testcontainers in the first iteration.

## Isolation principles (exploratory — minimal blast radius)

Touch **only** what is required for a disposable spike; everything else stays frozen.

1. **No production / library surface changes** — do not add or change code under
   `ai/mill-ai-v3-data/src/main/kotlin` for this WI. Prefer zero changes outside
   `ai/mill-ai-v3-data` except the unavoidable `testIT` dependency block in that module’s
   `build.gradle.kts`.
2. **Do not modify existing ITs or their Spring config** — leave
   `SqlValidatorSkymillFlowItApplication`, `BackendSqlValidatorSkyMillFlowIT`, and the current
   `src/testIT/resources/application.yml` as-is. Add a **separate** `@SpringBootTest` configuration
   class used only by the new exploration test (copy the import list / profile idea; do not refactor
   shared test fixtures).
3. **Test-local configuration** — prefer `@SpringBootTest(properties = […])` and/or a **new**
   `src/testIT/resources/application-*.yml` loaded only via `@ActiveProfiles` on the exploration
   class, so the original `sql-validator-skymill` profile behaviour is unchanged for other tests.
4. **Single entry point** — one IT class (plus optional `@TestConfiguration` inner class or
   package-private config in the same file) for Chroma + embedding beans; no new autoconfigure
   modules and no edits under `data/mill-data-autoconfigure`, `metadata/`, `ai/mill-ai-v3`, etc.
5. **Gradle** — add **LangChain4j** Chroma (and any embedding module you choose) **only** to the
   `testIT` suite dependencies in `ai/mill-ai-v3-data/build.gradle.kts` (not `api` /
   `implementation` for the module’s published graph). The `langchain4j-chroma` artifact follows
   LangChain4j’s **beta** version line that matches the `1.11.0` BOM (e.g. `1.11.0-beta19` in
   [libs.versions.toml](libs.versions.toml) via `langchain4j-beta`); core `langchain4j` / OpenAI
   stay on `1.11.0`. **Do not** add Spring AI vector-store / Chroma starters for this WI.
6. **Deploy / compose** — document how to use `deploy/local-dev` in this WI or test KDoc; do not
   change compose files unless there is a hard blocker (then call it out explicitly as an
   exception).

## In Scope

1. **Spring test slice** in `ai/mill-ai-v3-data/src/testIT/` (new classes only):
   - **Copy** the Skymill profile idea (`flow.facet.it.root`, `flow-skymill.yaml`, dialect) into a
     dedicated exploration `@SpringBootConfiguration` — do not alter the existing validator IT
     application class.
   - Import backend wiring needed to **execute** SQL and read rows (not only `SqlProvider.parseSql`),
     following the same bean shape as other Skymill ITs (e.g. `DefaultServiceConfiguration` as used
     from flow + gRPC Skymill query tests), **only** on the exploration test’s `@Import` list.
2. **SQL:** `SELECT DISTINCT …` on a Skymill column with stable, interesting strings. Good
   candidates:
   - `` `skymill`.`cities`.`city` `` — semantic similarity between place names.
   - `` `skymill`.`cities`.`state` `` — smaller cardinality, smoke-friendly.
   - `` `skymill`.`cities`.`airport_iata` `` — short codes; typo / alias experiments.
   - `` `skymill`.`segments`.`origin` `` / `` `destination` `` — if row volume is acceptable.
   Use backtick-quoted identifiers consistent with existing Skymill IT comments (Calcite / flow).
3. **Embeddings + Chroma (LangChain4j):** testIT-only Gradle dependencies for
   `dev.langchain4j:langchain4j-chroma` plus a LangChain4j `EmbeddingModel` implementation (e.g.
   `langchain4j-open-ai` / `OpenAiEmbeddingModel` behind env, or an in-process quantized embedding
   module if you want zero external LLM). Use `ChromaEmbeddingStore` with `EmbeddingStore<TextSegment>`
   — **not** Spring AI `VectorStore`.
4. **Similarity search:** after adding embeddings + segments to the store, run the LangChain4j
   embedding-store similarity API for your LangChain4j release (query embedding from the same
   `EmbeddingModel`, `maxResults` / score threshold as appropriate) and assert on returned segments
   or scores (document thresholds in the test).
5. **Documentation:** in this WI or a short comment in the test class, document:
   - How to start Chroma (and embedding provider, e.g. Ollama) via `deploy/local-dev` compose.
   - Required environment variables (e.g. Chroma base URL, API key if any, embedding base URL).
   - That CI **skips** the test unless those variables are set (`@EnabledIfEnvironmentVariable` /
     JUnit assumptions).

## Out of Scope

- Spring AI (`org.springframework.ai:*`) vector stores, embedding clients, or Chroma starters for
  this spike — use **LangChain4j** only.
- WI-172 bridge, facet resolver modes, or NL2SQL wiring changes.
- WI-173 metadata REST / mill-ui changes.
- Production autoconfigure or shipping Chroma as a default Mill dependency.
- Refactors of existing Skymill SQL validator tests, shared test YAML, or cross-module extraction
  “for reuse” — copy-paste in testIT is acceptable for this spike.
- Testcontainers Chroma (optional follow-up if CI-in-the-box is desired later).

## Dependencies

- Skymill CSV dataset reachable via `data/mill-data-backends/config/test/flow-skymill.yaml` and
  `flow.facet.it.root` (Gradle system property) as today.
- Operator runs `deploy/local-dev` Docker Compose (or compatible Chroma + embedding endpoints)
  before `./gradlew :ai:mill-ai-v3-data:testIT`.

## Implementation Plan

1. **Done** — `testIT` dependency [langchain4j-chroma](ai/mill-ai-v3-data/build.gradle.kts) +
   [libs.versions.toml](libs.versions.toml) `langchain4j-beta` / `langchain4j-chroma`. No Spring AI
   on this path; no extra LangChain4j embedding JAR (in-test `EmbeddingModel` only).
2. **Done** — [ChromaSkymillExploreItApplication.kt](ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/chroma/it/ChromaSkymillExploreItApplication.kt)
   + [application-chroma-explore-skymill.yml](ai/mill-ai-v3-data/src/testIT/resources/application-chroma-explore-skymill.yml).
3. **Done** — [ChromaSkymillDistinctVectorIT.kt](ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/chroma/it/ChromaSkymillDistinctVectorIT.kt)
   (env gate: **restore** `@EnabledIfEnvironmentVariable` before merge; see handoff above).
4. **Done** — validated with local Chroma + `MILL_CHROMA_IT_ENABLED=true`. After the env gate is
   restored, `./gradlew :ai:mill-ai-v3-data:testIT` skips this class when `MILL_CHROMA_IT_ENABLED` is
   unset (CI-safe).
5. **Optional next** — metadata on `TextSegment`, filtered search, real embedding model, other
   Skymill columns (see **Current implementation status**).

## Acceptance Criteria

- With compose + env vars documented, `./gradlew :ai:mill-ai-v3-data:testIT --tests "*…*"` runs
  the new test successfully.
- Without env vars, the test is skipped (or clearly no-ops) so default CI does not require Chroma.
- Distinct values are sourced from live Skymill flow SQL execution, not hard-coded duplicates of
  production CSV content in the test body (fixture path may still point at repo test data).

## Test Plan

- Manual: start `deploy/local-dev` stack, export env, run targeted `testIT`.
- Automated: same command in CI only when pipeline sets secrets / service links (optional).

## Risks and Mitigations

- **Flaky embeddings** — use clear assertion bands or a test-dedicated collection name + cleanup;
  consider a small test double of LangChain4j `EmbeddingModel` only if live APIs prove too unstable
  (document trade-off).
- **Heavy context** — keep `@SpringBootTest` classes minimal; avoid pulling full mill-service graph.

## Deliverables

- This file (updated with handoff): `docs/workitems/planned/metadata-value-mapping/WI-171-chroma-skymill-vector-exploration.md`.
- **Delivered:** Kotlin under `ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/chroma/it/`
  and `src/testIT/resources/application-chroma-explore-skymill.yml`.
- **Delivered:** [libs.versions.toml](libs.versions.toml) (`langchain4j-beta`, `langchain4j-chroma`) +
  [ai/mill-ai-v3-data/build.gradle.kts](ai/mill-ai-v3-data/build.gradle.kts) `testIT` deps.
