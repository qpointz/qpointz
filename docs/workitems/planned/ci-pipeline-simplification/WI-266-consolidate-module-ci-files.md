# WI-266 — Restructure module CI files to own all stages

## Goal

Transform per-module `.gitlab-ci.yml` files from build-only job definitions into
**full-lifecycle pipelines** covering `build`, `test`, `test-integration`, and optionally
`package`. Jobs self-gate on stage-activation variables set by the root orchestrator.

## Current state

Each module file defines a single `<module>:build` job in `stage: build` that typically runs
`./gradlew :<module>:test :<module>:testITClasses`. Full-repo test runs are handled separately
by the `test:downstream` child pipeline (`test.yml`), and `testIT` execution lives in the
global `integration.yml`. Module files are only included on feature branches with changes.

Anomalies:
- `metadata/` and `persistence/` run `:build` instead of `:test :testITClasses`
- `clients/` includes a Python unit test job alongside the Gradle job
- `ui/` has an additional Node.js build/test job
- 7 files contain stale `echo $HOME` debug lines

## Target state

### Shared template (defined in `.gitlab/common.yml`, see WI-267)

```yaml
.gradle-module-job:
  extends: .gradle-job
```

### Module file pattern — Gradle modules

```yaml
<module>:build:
  extends: .gradle-module-job
  stage: build
  script:
    - ./gradlew --no-daemon ${GRADLE_CONTINUE_PARAM} --console plain :<module>:classes

<module>:test:
  extends: .gradle-module-job
  stage: test
  rules:
    - if: '$RUN_TEST != "false"'
  script:
    - ./gradlew --no-daemon ${GRADLE_CONTINUE_PARAM} --console plain :<module>:test :<module>:testITClasses
  artifacts:
    when: always
    expire_in: 7 days
    paths:
      - <module>/**/build/reports/tests/test/
      - <module>/**/build/reports/jacoco/test/
    reports:
      junit:
        - <module>/**/build/test-results/test/TEST-*.xml

<module>:test-integration:
  extends: .gradle-module-job
  stage: test-integration
  rules:
    - if: '$RUN_INTEGRATION == "true"'
  script:
    - ./gradlew --no-daemon ${GRADLE_CONTINUE_PARAM} --console plain :<module>:testIT
```

### Per-module variations

| Module | build | test | test-integration | package | Extra |
|--------|-------|------|------------------|---------|-------|
| `core` | `:core:classes` | `:core:test :core:testITClasses` | `:core:testIT` | — | — |
| `ai` | `:ai:classes` | `:ai:test :ai:testITClasses` | `:ai:testIT` (`allow_failure: true`) | — | AI regression artifacts in integration job |
| `data` | `:data:classes` | `:data:test :data:testITClasses` | — | — | No module-level testIT (cross-module in acceptance) |
| `cloud` | `:cloud:classes` | `:cloud:test :cloud:testITClasses` | `:cloud:testIT` | — | Testcontainer-based emulator tests |
| `metadata` | `:metadata:classes` | `:metadata:build` | — | — | Currently runs `:build` not `:test`; verify if tests exist |
| `persistence` | `:persistence:classes` | `:persistence:build` | — | — | Same as metadata |
| `clients` | `:clients:classes` | `:clients:test :clients:testITClasses` | — | — | Additional mill-py unit test job (matrix: 3.12/3.13/3.14) |
| `services` | `:services:classes` | `:services:test :services:testITClasses` | — | — | — |
| `ui` | `:ui:classes` | `:ui:test :ui:testITClasses` | — | — | Additional `ui:npm-build` job (Node.js test + build) |

### `clients/.gitlab-ci.yml` — mill-py unit tests

Mill-py unit tests move from deleted `test.yml` into `clients/.gitlab-ci.yml` as a module-owned
test-stage job with `parallel:matrix`:

```yaml
clients:mill-py:test:
  extends: .python-mill-py-unit-test
  stage: test
  rules:
    - if: '$RUN_TEST != "false"'
  parallel:
    matrix:
      - PY_VER: ["3.12", "3.13", "3.14"]
```

### `ui/.gitlab-ci.yml` — Node.js job

The existing `ui:npm-build` job (Node.js test + Vite build) stays alongside the Gradle jobs:

```yaml
ui:npm-build:
  extends: .node-job
  stage: test
  rules:
    - if: '$RUN_TEST != "false"'
  script:
    - cd ${CI_PROJECT_DIR}/ui/mill-ui
    - npm install
    - npm run test:ci
    - npm run build
  artifacts:
    expire_in: "5 days"
    paths:
      - ${CI_PROJECT_DIR}/services/mill-ui-service/src/main/resources/static/app/v2
    reports:
      junit:
        - ui/**/.test/*.xml
```

### Elimination of `test.yml` child pipeline

`.gitlab/pipelines/test.yml` is **deleted**. Its contents are absorbed:
- `components:test` (monolithic `./gradlew test testITClasses`) → replaced by per-module test jobs
- `ui:test` → stays in `ui/.gitlab-ci.yml`
- `mill-py-3.1x:test` (3 jobs) → `clients/.gitlab-ci.yml` with matrix
- `test:downstream` trigger in root `.gitlab-ci.yml` → removed

### Migration of `testIT` jobs from `integration.yml`

Three simple `testIT` jobs currently in `integration.yml` move to module files:
- `core:integration` → `core/.gitlab-ci.yml` as `core:test-integration`
- `ai:integration` → `ai/.gitlab-ci.yml` as `ai:test-integration`
- `cloud:integration` → `cloud/.gitlab-ci.yml` as `cloud:test-integration`

These are self-contained (testcontainers only, no external service management).

## Files affected

- `core/.gitlab-ci.yml` — rewrite (build + test + test-integration)
- `metadata/.gitlab-ci.yml` — rewrite (build + test)
- `persistence/.gitlab-ci.yml` — rewrite (build + test)
- `ai/.gitlab-ci.yml` — rewrite (build + test + test-integration with allow_failure)
- `ui/.gitlab-ci.yml` — rewrite (gradle + node across stages)
- `data/.gitlab-ci.yml` — rewrite (build + test)
- `cloud/.gitlab-ci.yml` — rewrite (build + test + test-integration)
- `clients/.gitlab-ci.yml` — rewrite (gradle + mill-py matrix)
- `services/.gitlab-ci.yml` — rewrite (build + test)
- `.gitlab/pipelines/test.yml` — **delete**
- `.gitlab/pipelines/integration.yml` — remove `core:integration`, `ai:integration`, `cloud:integration` (see WI-268)
- `.gitlab-ci.yml` — remove `test:downstream` trigger (see WI-269)
