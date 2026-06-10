# ci-pipeline-simplification

## Goal

Restructure the GitLab CI/CD pipeline around a **module-centric** model where each module
owns its full CI lifecycle and the root pipeline acts as an orchestrator. Reduce duplication,
flatten unnecessary indirection, remove dead code, and centralize build tool version management.

## Audit findings

### Current file inventory (`.gitlab/`)

```
.gitlab-ci.yml                                      # root orchestrator (240 lines)
.gitlab/
  Makefile                                           # tool-image builder (parses vars.yml)
  common/
    common.yml                                       # 2-line aggregator → jobs.yml + vars.yml
    jobs.yml                                         # 6-line aggregator → 6 template files
    vars.yml                                         # global variables (12 lines)
  templates/
    gradle.yml                                       # .gradle-job (65 lines)
    docker.yml                                       # .docker-base + 2 build jobs (67 lines)
    python.yml                                       # .python-job + .python-mill-py-unit-test (31 lines)
    node.yml                                         # .node-job (3 lines)
    minica.yml                                       # .init-dev-certs (12 lines)
    release.yml                                      # .semantic-release-job (7 lines)
    apps-package-dist.yml                            # .apps-package-dist-template (56 lines)
    clients-jdbc-shell-package-dist.yml              # .clients-jdbc-shell-package-dist-template (45 lines)
  pipelines/
    test.yml                                         # child pipeline: monolithic test + UI + mill-py (75 lines)
    integration.yml                                  # child pipeline: packaging + 12 integration jobs (416 lines)
    release.yml                                      # child pipeline: maven, docker, pypi, docs (305 lines)
  docker/
    minica/Dockerfile
    deploy-tools/Dockerfile
    semantic-release/Dockerfile
  legacy/                                            # UNUSED — old CI configs (8 files)

Module CI files (9 files, 6-50 lines each):
  core/.gitlab-ci.yml
  metadata/.gitlab-ci.yml
  persistence/.gitlab-ci.yml
  ai/.gitlab-ci.yml
  data/.gitlab-ci.yml
  cloud/.gitlab-ci.yml
  clients/.gitlab-ci.yml
  services/.gitlab-ci.yml
  ui/.gitlab-ci.yml
```

### Template analysis

| Template | File | Lines | Status |
|----------|------|-------|--------|
| `.gradle-job` | gradle.yml | 65 | **Refactor.** `before_script` has verbose `BUILD_TAG_INPUTS` cat block (rarely useful). Rules section repeats CACHE variable sets 3 times with identical `CACHE_FALLBACK_KEY`. |
| `.docker-base` | docker.yml | 9 | **Keep** as shared base. |
| `.docker-build-job` | docker.yml | 20 | **Merge** with release variant — 80% identical (buildx create/inspect/build/rm). Only tagging differs. |
| `.docker-build-job-release` | docker.yml | 35 | **Merge** into single template with tagging override. Release adds Docker Hub login + version tags. |
| `.python-job` | python.yml | 2 | **Inline.** Just sets `PIP_DISABLE_PIP_VERSION_CHECK` and `PIP_ROOT_USER_ACTION`. Fold into `.python-mill-py-unit-test`. |
| `.python-mill-py-unit-test` | python.yml | 26 | **Keep, fix.** Hardcodes `stage: build` — should be `stage: test`. `TEST_TYPE` variable set but never read by script. |
| `.node-job` | node.yml | 3 | **Keep.** Trivially small (image + 1 variable) but referenced in 4 places. |
| `.init-dev-certs` | minica.yml | 12 | **Keep.** Hardcodes `stage: init` — needs to be overridable. Only used by acceptance pipeline. |
| `.semantic-release-job` | release.yml | 7 | **Keep as-is.** Clean and small. |
| `.apps-package-dist-template` | apps-package-dist.yml | 56 | **Keep.** Contains jdeps/jlink analysis. Used by feature-branch packaging and release editions. |
| `.clients-jdbc-shell-package-dist-template` | clients-jdbc-shell-package-dist.yml | 45 | **Keep.** Similar jdeps pattern. Release pipeline only. |

### Dead variables

| Variable | Defined in | Used by |
|----------|-----------|---------|
| `IMAGE_DEPLOY_TOOLS` | vars.yml | **Nothing.** Makefile builds the image but no CI job references it. |
| `TEST_TYPE` | python.yml, release.yml (pypi jobs) | **Never read.** Set to `"unit"` in multiple places but no script checks it. |

### Dead YAML anchors (root `.gitlab-ci.yml`)

| Anchor | Line | Referenced by |
|--------|------|---------------|
| `.if-protected` / `&if-protected` | 53 | **Nothing.** |
| `.if-mr-full` / `&if-mr-full` | 56 | **Nothing.** |
| `.if-run-integration` / `&if-run-integration` | 59 | **Nothing.** |
| `.build-changes` / `&build-changes` | 62 | **Nothing.** |

### Dead code blocks

| Location | Content | Lines |
|----------|---------|-------|
| test.yml:25-40 | Commented-out `ui:v1:test` | 15 |
| release.yml:77-105 | Commented-out `ui:ui-v1:build`, `ui:ui-v2:build` | 28 |
| release.yml:110-116, 145-151 | Commented-out `needs` on edition dist jobs | 12 |
| integration.yml:68-89 | Commented-out `ai:publish-regression` | 21 |
| integration.yml:200-203 (root) | Commented-out `needs` on `integration:downstream` | 4 |
| ui/.gitlab-ci.yml:1-16 | Commented-out `ui:grinder-ui-v1:build` | 16 |
| 7 module CI files | `echo $HOME` debug line | 7 |
| release.yml:165 | Debug `ls -la` in `apps:samples-edition-docker` `before_script` | 1 |

### Integration pipeline redundancies

**mill-py template variable waste** — every expanded job re-specifies defaults that match the
template. These variables **never vary** across any of the 9 jobs:

- `MILL_IT_HOST: "mill-it"` — always `mill-it`
- `MILL_IT_BASE_PATH: "/services/jet"` — always `/services/jet`
- `MILL_IT_TLS: "false"` — always `false`
- `MILL_IT_AUTH: "none"` — always `none`
- `MILL_IT_SCHEMA: "skymill"` — always `skymill`

**Unused template variables** (set in template, never consumed):

- `MILL_IT_TLS_CA`, `MILL_IT_TLS_CERT`, `MILL_IT_TLS_KEY` — always empty
- `MILL_IT_USERNAME`, `MILL_IT_PASSWORD` — set but `AUTH: "none"` means unused
- `MILL_IT_TOKEN` — always empty

**`MILL_IT_PLATFORM_ORIGIN`** set inconsistently — present in some jobs (3.12 grpc, 3.13
http-json, 3.14 http-json), missing from others. Should be a template default.

**Simple `testIT` jobs** (`core:integration`, `ai:integration`, `cloud:integration`) are plain
`./gradlew :module:testIT` — belong in module files under `test-integration` stage, not in the
global acceptance pipeline.

### Release pipeline redundancies

- `publish:pypi-test` and `publish:pypi-prod` share ~95% of script (poetry install, version,
  build, publish). Differ only in repository config (test vs prod). Could be a single template.
- `TEST_TYPE: "unit"` variable in both pypi jobs — dead, never read.

### `.gitlab/Makefile`

Builds tool Docker images (semantic-release, minica, deploy-tools). Developer/ops utility,
not CI. **Keep** but note it parses `common/vars.yml` with grep — if `vars.yml` is merged into
`common.yml`, the Makefile grep pattern must be updated or the version extracted differently.

### `.gitlab/docker/deploy-tools/`

Dockerfile exists, Makefile builds it, `IMAGE_DEPLOY_TOOLS` variable is defined — but **no CI
job references the image**. Candidate for removal or documentation of its intended use.

## Target architecture

### Stage model

```
build → test → test-integration → test-acceptance → package → publish → release
```

| Stage | Scope | Description |
|-------|-------|-------------|
| `build` | Module | Compile classes, assemble artifacts |
| `test` | Module | Unit tests, `testITClasses` compilation check |
| `test-integration` | Module | `testIT` — integration tests using testcontainers (self-contained, no external services) |
| `test-acceptance` | Global | Cross-module acceptance: spin up mill-service container, run mill-py and JDBC client tests across protocols/versions |
| `package` | Module + Global | Build distribution archives, Docker images. Feature branches: manual via `RUN_PACKAGE=true`. Protected branches: automatic. |
| `publish` | Global | Maven Central, PyPI, docs. Release pipeline only. |
| `release` | Global | Semantic release, tag management. |

### Root `.gitlab-ci.yml` — orchestrator

- Defines all stages
- Sets **stage-activation variables** based on context:
  - `RUN_TEST` (default `true`)
  - `RUN_INTEGRATION` (default `false`, `true` on protected branches)
  - `RUN_ACCEPTANCE` (default `false`, `true` on protected branches or manual)
  - `RUN_PACKAGE` (default `false`, `true` on protected branches or manual)
- Includes module pipelines with change-based rules
- Hosts global jobs: feature-branch packaging, semantic release trigger
- Includes global pipelines: acceptance, release

### Module `<module>/.gitlab-ci.yml` — self-contained lifecycle

Each module defines jobs for every stage it participates in. Jobs self-gate on stage
variables. Example:

```yaml
core:build:
  extends: .gradle-module-job
  stage: build
  script:
    - ./gradlew --no-daemon ${GRADLE_CONTINUE_PARAM} --console plain :core:classes

core:test:
  extends: .gradle-module-job
  stage: test
  rules:
    - if: '$RUN_TEST != "false"'
  script:
    - ./gradlew --no-daemon ${GRADLE_CONTINUE_PARAM} --console plain :core:test :core:testITClasses
  artifacts:
    when: always
    expire_in: 7 days
    paths:
      - core/**/build/reports/tests/test/
    reports:
      junit:
        - core/**/build/test-results/test/TEST-*.xml

core:test-integration:
  extends: .gradle-module-job
  stage: test-integration
  rules:
    - if: '$RUN_INTEGRATION == "true"'
  script:
    - ./gradlew --no-daemon ${GRADLE_CONTINUE_PARAM} --console plain :core:testIT
```

### Global pipelines

- **Acceptance** (`.gitlab/pipelines/acceptance.yml`) — packages integration edition, spins up
  service container, runs mill-py (matrix: 3 Python versions x 3 protocols) and JDBC (matrix:
  3 protocols) client tests. Gated by `RUN_ACCEPTANCE`.
- **Release** (`.gitlab/pipelines/release.yml`) — protected tags only. Maven publication, Docker
  images (minimal + samples editions, jdbc-shell), PyPI (test + prod), docs publishing.

### Shared templates (`.gitlab/common.yml` — single merged file)

- Global variables (from vars.yml + new `JDK_VERSION`)
- `.gradle-job` — cleaned up (remove verbose cat block, simplify rules)
- `.gradle-module-job` — extends `.gradle-job`
- `.docker-build-job` — consolidated from current two templates
- `.docker-build-job-release` — extends base, adds version tags + Docker Hub
- `.python-mill-py-unit-test` — `.python-job` inlined, `stage: test`, drop dead `TEST_TYPE`
- `.node-job` — kept (3 lines, referenced in 4 places)
- `.init-dev-certs` — kept, stage made overridable
- `.semantic-release-job` — kept as-is
- `.apps-package-dist-template` — kept
- `.clients-jdbc-shell-package-dist-template` — kept

### File structure (after)

```
.gitlab-ci.yml                          # orchestrator
.gitlab/
  common.yml                            # variables + all templates (merged)
  Makefile                              # tool-image builder (updated grep path)
  pipelines/
    acceptance.yml                      # cross-module acceptance tests (matrix)
    release.yml                         # tag-triggered release
  docker/
    minica/Dockerfile
    semantic-release/Dockerfile

core/.gitlab-ci.yml                     # build + test + test-integration
ai/.gitlab-ci.yml                       # build + test + test-integration
data/.gitlab-ci.yml                     # build + test
cloud/.gitlab-ci.yml                    # build + test + test-integration
metadata/.gitlab-ci.yml                 # build + test
persistence/.gitlab-ci.yml              # build + test
clients/.gitlab-ci.yml                  # gradle build/test + mill-py unit tests (matrix)
services/.gitlab-ci.yml                 # build + test
ui/.gitlab-ci.yml                       # gradle + node jobs
```

**Deleted:**

| Path | Reason |
|------|--------|
| `.gitlab/common/common.yml` | Merged into `common.yml` |
| `.gitlab/common/jobs.yml` | Merged into `common.yml` |
| `.gitlab/common/vars.yml` | Merged into `common.yml` |
| `.gitlab/templates/` (entire directory, 8 files) | Merged into `common.yml` |
| `.gitlab/pipelines/test.yml` | Replaced by module-level test jobs |
| `.gitlab/pipelines/integration.yml` | Renamed/refactored to `acceptance.yml` |
| `.gitlab/legacy/` (entire tree, 8 files) | Unused dead code |
| `.gitlab/docker/deploy-tools/Dockerfile` | Unused — no CI job references it |

### Orchestration logic

| Context | Modules included | Active stages |
|---------|-----------------|---------------|
| Feature branch, module changed | Changed modules only | build, test |
| Feature branch, `RUN_FULL_TEST=true` | All | build, test |
| Feature branch, `RUN_INTEGRATION=true` | All | build, test, test-integration |
| Feature branch, `RUN_ACCEPTANCE=true` | All | build, test, test-integration, test-acceptance |
| Feature branch, `RUN_PACKAGE=true` | All | build, test, package |
| MR to `rc`/`main` | All | build, test |
| Protected branch (`dev`, `rc`, `main`) | All | build, test, test-integration, test-acceptance, package |
| Tag on protected branch | — | release pipeline |

### Build tool version management

- **Gradle version**: `gradle/wrapper/gradle-wrapper.properties` (single source of truth).
  Cache key references this file.
- **JDK version**: `JDK_VERSION` global variable in `common.yml` (default `21`), referenced
  in `.gradle-job` image tag.
- **Python default version**: `DEFAULT_PY_VER` in `common.yml` (currently `3.13`).
- **Makefile dependency**: parses `common.yml` for `BUILD_TOOLS_VERSION` — grep pattern must
  match the merged file format.

## Work items

- [ ] WI-266 — Restructure module CI files to own all stages (`WI-266-consolidate-module-ci-files.md`)
- [ ] WI-267 — Flatten and clean templates (`WI-267-flatten-common-templates.md`)
- [ ] WI-268 — Acceptance pipeline with matrix (`WI-268-matrix-integration-tests.md`)
- [ ] WI-269 — Rewrite root orchestration logic (`WI-269-deduplicate-root-include-rules.md`)
- [ ] WI-270 — Remove dead code, legacy, and unused artifacts (`WI-270-cleanup-legacy-and-dead-code.md`)
- [ ] WI-272 — Centralize build tool versions (`WI-272-centralize-build-tool-versions.md`)
- [ ] WI-273 — Document CI/CD pipeline for maintainers and agents (`WI-273-document-ci-pipeline-for-maintainers.md`)
