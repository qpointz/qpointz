# WI-269 — Rewrite root orchestration logic

## Goal

Replace the current verbose root `.gitlab-ci.yml` (240 lines, 9 repeated include blocks,
child pipeline triggers, unused anchors) with a clean orchestrator that defines stages, sets
stage-activation variables, includes module pipelines, and hosts global jobs.

## Current state (problems)

1. **Nine repeated include blocks** (lines 71-141): identical 6-line rule condition, only
   module path differs. Any policy change requires editing nine places.
2. **Child pipeline triggers** (`test:downstream`, `integration:downstream`): add complexity
   and separate pipeline views. `test:downstream` is eliminated (WI-266); `integration` becomes
   a direct include (WI-268).
3. **Four unused YAML anchors**: `.if-protected`, `.if-mr-full`, `.if-run-integration`,
   `.build-changes` — defined but never referenced.
4. **Duplicate `$CI_COMMIT_TAG` guard** in `test:downstream` rules (lines 188-189 and 192-193).
5. **`RUN_PUBLISH` variable** defined but only used in `publish:semantic-release` rules via
   branch name check, not the variable itself.

## Target state

### Stages

```yaml
stages:
  - build
  - test
  - test-integration
  - test-acceptance
  - package
  - publish
  - release
```

### Stage-activation variables

```yaml
variables:
  RUN_TEST:
    value: "true"
    description: "Run test-stage jobs (default: true)"
    options: ["true", "false"]
  RUN_INTEGRATION:
    value: "false"
    description: "Run test-integration stage (module-level testIT)"
    options: ["true", "false"]
  RUN_ACCEPTANCE:
    value: "false"
    description: "Run test-acceptance stage (cross-module client tests)"
    options: ["true", "false"]
  RUN_PACKAGE:
    value: "false"
    description: "Build distribution packages and Docker images"
    options: ["true", "false"]
  GRADLE_CONTINUE:
    value: "false"
    description: "Continue Gradle build after failure"
    options: ["true", "false"]
  GRADLE_FORCE_CLEAN:
    value: "false"
    description: "Force Gradle clean"
    options: ["true", "false"]
```

**Removed variables:**
- `RUN_FULL_TEST` — replaced by `RUN_TEST` (always on by default) + `RUN_INTEGRATION`
- `RUN_PUBLISH` — unused (semantic release gated by branch name)

### Workflow rules

Set defaults based on pipeline context:

```yaml
workflow:
  rules:
    - if: '$CI_COMMIT_TAG && $CI_COMMIT_REF_PROTECTED == "true"'
      when: always
    - if: '$CI_COMMIT_TAG && $CI_COMMIT_REF_PROTECTED != "true"'
      when: never
    - if: '$CI_COMMIT_REF_PROTECTED == "true"'
      variables:
        RUN_TEST: "true"
        RUN_INTEGRATION: "true"
        RUN_ACCEPTANCE: "true"
        RUN_PACKAGE: "true"
      when: always
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event" && ($CI_MERGE_REQUEST_TARGET_BRANCH_NAME == "rc" || $CI_MERGE_REQUEST_TARGET_BRANCH_NAME == "main")'
      variables:
        RUN_TEST: "true"
      when: always
    - when: always
```

### Module includes

All nine module includes follow the same pattern. On feature branches, include only when the
module directory has changes **or** build infrastructure changed. On protected branches, MRs,
or when any `RUN_*` variable forces full testing, include all:

```yaml
include:
  - local: /.gitlab/common.yml

  - local: /core/.gitlab-ci.yml
    rules:
      - if: '$CI_COMMIT_REF_PROTECTED == "true" || $CI_PIPELINE_SOURCE == "merge_request_event" || $RUN_INTEGRATION == "true" || $RUN_ACCEPTANCE == "true"'
      - changes: [core/**/*]
      - changes: [.gitlab-ci.yml, .gitlab/**/*,  build-logic/**/*]

  # same pattern for: ai, data, cloud, metadata, persistence, clients, services, ui

  - local: /.gitlab/pipelines/acceptance.yml
    rules:
      - if: '$RUN_ACCEPTANCE == "true"'

  - local: /.gitlab/pipelines/release.yml
    rules:
      - if: '$CI_COMMIT_TAG && $CI_COMMIT_REF_PROTECTED == "true"'
```

### Global jobs (in root file)

**Feature-branch packaging** — available on any branch via `RUN_PACKAGE=true`:

```yaml
package:complete-samples-dist:
  extends: .apps-package-dist-template
  stage: package
  variables:
    APP_EDITION: "complete-samples"
  rules:
    - if: '$CI_COMMIT_TAG'
      when: never
    - if: '$RUN_PACKAGE == "true" || $CI_COMMIT_REF_PROTECTED == "true"'
      when: always
    - when: never

package:complete-samples-docker:
  extends: .docker-build-job
  stage: package
  needs:
    - job: package:complete-samples-dist
      artifacts: true
  variables:
    APP_EDITION: "complete-samples"
    DOCKER_BUILD_CTX: ${CI_PROJECT_DIR}/apps/mill-service
    DOCKER_BUILD_FILE: ${CI_PROJECT_DIR}/apps/mill-service/src/main/docker/base/Dockerfile
    DOCKER_BUILD_CONTAINER_NAME: mill-service-${APP_EDITION}
    DOCKER_BUILD_ARGS: --build-arg APP_EDITION=${APP_EDITION}
  rules:
    - if: '$CI_COMMIT_TAG'
      when: never
    - if: '$RUN_PACKAGE == "true" || $CI_COMMIT_REF_PROTECTED == "true"'
      when: always
    - when: never
```

**Semantic release** — manual on `main`/`rc`:

```yaml
publish:semantic-release:
  extends: .semantic-release-job
  stage: publish
  rules:
    - if: '$CI_COMMIT_REF_NAME == "main" || $CI_COMMIT_REF_NAME == "rc"'
      when: manual
    - when: never
  script:
    - export GITLAB_TOKEN=${CI_PUSH_TOKEN}
    - npx semantic-release --ci
```

### Removed from root

| Item | Replacement |
|------|-------------|
| `test:downstream` trigger | Module-level test jobs (WI-266) |
| `integration:downstream` trigger | Direct include of `acceptance.yml` |
| 9 repeated include blocks | Simplified includes (above) |
| `.if-protected` anchor | Deleted (unused) |
| `.if-mr-full` anchor | Deleted (unused) |
| `.if-run-integration` anchor | Deleted (unused) |
| `.build-changes` anchor | Deleted (unused) |
| `RUN_FULL_TEST` variable | Replaced by `RUN_INTEGRATION` |
| `RUN_PUBLISH` variable | Deleted (unused) |

## Files affected

- `.gitlab-ci.yml` — major rewrite (~240 → ~100 lines)
