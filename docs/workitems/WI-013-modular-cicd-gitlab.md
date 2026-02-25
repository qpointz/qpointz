# WI-013: Modular GitLab CI/CD Refactor

**Type:** refactoring / improvement  
**Priority:** high  
**Status:** planned  
**Rules:** See [RULES.md](RULES.md)  
**Branch name:** `refactor/wi-013-modular-gitlab-cicd`

---

## Goal

Make GitLab CI/CD manageable, modular, and safer by:

1. Reducing duplication across module pipelines.
2. Standardizing rules/stages/variables and job templates.
3. Centralizing shared build logic and cache behavior.
4. Improving security and maintainability of publish/release paths.

---

## Scope

- Refactor root `.gitlab-ci.yml` into a thin orchestrator.
- Introduce reusable CI templates under `.gitlab/templates/`.
- Normalize module pipelines to use shared templates and minimal wrappers.
- Replace mixed `only` usage with `rules` for consistency.
- Fix variable/path drift and remove conflicting/duplicate definitions.
- Remove or archive obsolete CI files that create confusion.

---

## Current Pain Points (Baseline)

- Duplicate job logic across `core`, `data`, `ai`, `ui`, `apps`, `metadata`, `clients`.
- Mixed control patterns (`rules` + `only`) and inconsistent stages.
- Variable collisions and drift in shared vars files.
- Inconsistent path conventions in nested client CI files.
- Risky token handling patterns in publish jobs.
- Legacy CI files remain in-repo and increase onboarding cost.

---

## Out of Scope

- Migrating from GitLab CI to another CI platform.
- Rewriting business build/test logic (Gradle/npm/pytest commands remain functionally equivalent).
- Introducing new release/versioning semantics beyond pipeline structure cleanup.

---

## Implementation Plan

1. **Create shared templates**
   - Add `.gitlab/templates/gradle.yml`, `python.yml`, `node.yml`, `docker.yml`, `release.yml`, `rules.yml`.
   - Move reusable definitions from `.gitlab/.jobs.yml` into focused templates.

2. **Refactor root orchestrator**
   - Keep root pipeline responsible for workflow, global variables, and includes only.
   - Move component registration into dedicated include files.

3. **Modularize component pipelines**
   - Convert module-level pipelines to thin wrappers using `extends`.
   - Ensure each component sets only module-specific paths/tasks.

4. **Standardize control flow**
   - Replace `only` with `rules` in publish/release jobs.
   - Align stages and gating patterns across all included files.

5. **Fix drift and hygiene issues**
   - Resolve duplicate/conflicting variable definitions.
   - Correct stale/legacy path references in clients pipeline files.
   - Remove/retire obsolete CI files and commented legacy blocks where safe.

6. **Security and reliability pass**
   - Remove token echoing and unsafe debug output.
   - Validate docs publish and release jobs still function with tightened handling.

---

## Deliverables

- Modular template-based GitLab CI structure under `.gitlab/templates/`.
- Simplified root pipeline orchestration with clear component includes.
- Updated module pipelines with reduced duplication.
- Consistent `rules`-based gating.
- Cleaned shared variable definitions and corrected path references.
- CI docs section update describing pipeline layout and extension points.

---

## Verification

1. All affected pipelines lint successfully in GitLab CI linter.
2. MR pipeline executes only impacted components via `changes` rules.
3. Protected branch pipeline executes full expected build/integration/publish flow.
4. No job relies on deprecated `only` where corresponding `rules` are required.
5. No duplicate/conflicting shared variable keys in CI config.
6. Release/docs jobs run without exposing sensitive tokens in logs.

---

## Estimated Effort

Medium to high. Structural CI refactors are broad but low-risk when done in phases
with parity checks and staged rollout.
