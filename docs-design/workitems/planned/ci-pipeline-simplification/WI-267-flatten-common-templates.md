# WI-267 — Flatten and clean templates

## Goal

Merge the two-level include indirection into a single `.gitlab/common.yml`. Clean up each
template: remove dead variables, inline trivial helpers, consolidate Docker build templates.

## Current include chain

```
Pipeline file
  └── includes: /.gitlab/common/common.yml         (2 lines — pure aggregator)
        ├── includes: /.gitlab/common/vars.yml      (12 lines — variables only)
        └── includes: /.gitlab/common/jobs.yml      (6 lines — pure aggregator)
              ├── /.gitlab/templates/release.yml     (7 lines)
              ├── /.gitlab/templates/gradle.yml      (65 lines)
              ├── /.gitlab/templates/python.yml      (31 lines)
              ├── /.gitlab/templates/node.yml        (3 lines)
              ├── /.gitlab/templates/docker.yml      (67 lines)
              └── /.gitlab/templates/minica.yml      (12 lines)
```

Additionally, `apps-package-dist.yml` and `clients-jdbc-shell-package-dist.yml` are included
separately by pipelines that need them.

## Target: single `.gitlab/common.yml`

All content merged into one file. Every pipeline includes `/.gitlab/common.yml` and gets
everything.

### Section 1: Global variables

From current `vars.yml` plus new additions:

```yaml
variables:
  JDK_VERSION: "21"
  BUILD_TOOLS_VERSION: 1.0.3
  BUILD_TOOLS_REGISTRY: registry.qpointz.io/qpointz/qpointz
  DEFAULT_PY_VER: "3.13"
  IMAGE_SEM_RELEASE: ${BUILD_TOOLS_REGISTRY}/semantic-release:${BUILD_TOOLS_VERSION}
  IMAGE_MINICA: ${BUILD_TOOLS_REGISTRY}/minica:${BUILD_TOOLS_VERSION}
  GIT_STRATEGY: fetch
  GIT_DEPTH: "1"
  GIT_CLEAN_FLAGS: none
  GIT_SUBMODULE_STRATEGY: none
  GIT_LFS_SKIP_SMUDGE: "1"
```

**Removed:** `IMAGE_DEPLOY_TOOLS` — defined but never referenced by any CI job.

### Section 2: `.gradle-job` — cleaned up

Changes from current:
- Use `${JDK_VERSION}` in image tag instead of hardcoded `21`
- Remove verbose `BUILD_TAG_INPUTS` cat block from `before_script` (rarely useful, adds noise)
- Simplify rules: the three rules branches all set `CACHE_FALLBACK_KEY: "gradle-dev"` — make
  it a variable default and remove from each rule

```yaml
.gradle-job:
  image:
    name: ${CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX}/azul/zulu-openjdk:${JDK_VERSION}-latest
  variables:
    GRADLE_USER_HOME: ${CI_PROJECT_DIR}/.gradle-home
    GRADLE_DO_CLEAN: "false"
    CACHE_POLICY: "pull"
    CACHE_PREFIX: "gradle-dev"
    CACHE_FALLBACK_KEY: "gradle-dev"
  before_script:
    - |
      if [ "$GRADLE_CONTINUE" = "true" ]; then
        export GRADLE_CONTINUE_PARAM=" --continue "
      else
        export GRADLE_CONTINUE_PARAM=" "
      fi
    - |
      if [ "$GRADLE_FORCE_CLEAN" = "true" ]; then
        export GRADLE_DO_CLEAN="true"
      fi
      if [ "$GRADLE_DO_CLEAN" = "true" ]; then
        ./gradlew --no-daemon --console plain clean
      fi
  rules:
    - if: '$CI_COMMIT_REF_NAME == "main" || $CI_COMMIT_REF_NAME == "rc"'
      variables:
        GRADLE_DO_CLEAN: "true"
        CACHE_POLICY: "pull-push"
        CACHE_PREFIX: "gradle-release-${CI_COMMIT_REF_NAME}"
    - if: '$CI_COMMIT_REF_NAME == "dev"'
      variables:
        CACHE_POLICY: "pull-push"
    - when: on_success
  cache:
    key:
      prefix: $CACHE_PREFIX
      files:
        - gradle/wrapper/gradle-wrapper.properties
    fallback_keys:
      - $CACHE_FALLBACK_KEY
    policy: $CACHE_POLICY
    paths:
      - .gradle-home/wrapper/
      - .gradle-home/caches/modules-2/
      - .gradle-home/caches/jars-9/
```

### Section 3: `.gradle-module-job`

```yaml
.gradle-module-job:
  extends: .gradle-job
```

Thin alias for now. May diverge later if module jobs need different defaults.

### Section 4: Docker templates — consolidated

Current state: `.docker-base` (9 lines) + `.docker-build-job` (20 lines) +
`.docker-build-job-release` (35 lines). The two build jobs share ~80% of logic
(buildx create, inspect, cache, build, push, cleanup).

Target: single buildx flow, tagging as the override point.

```yaml
.docker-base:
  image:
    name: docker:27-cli
    entrypoint: [""]
  variables:
    DOCKER_BUILDKIT: "1"
    DOCKER_DRIVER: overlay2
  before_script:
    - docker login -u "${CI_REGISTRY_USER}" -p "${CI_REGISTRY_PASSWORD}" "${CI_REGISTRY}"
  after_script:
    - docker buildx rm "ci-builder-${CI_JOB_ID}" || true

.docker-build-job:
  extends: .docker-base
  script:
    - export DOCKER_TAG_ARGS="--tag ${CI_REGISTRY_IMAGE}/${DOCKER_BUILD_CONTAINER_NAME}:${CI_COMMIT_REF_SLUG}"
    - export DOCKER_CACHE_REF="${CI_REGISTRY_IMAGE}/cache/${DOCKER_BUILD_CONTAINER_NAME}:${CI_COMMIT_REF_SLUG}"
    - docker buildx create --name "ci-builder-${CI_JOB_ID}" --driver docker-container --use
    - docker buildx inspect --bootstrap
    - >
      docker buildx build
      --file "${DOCKER_BUILD_FILE}"
      --cache-from "type=registry,ref=${DOCKER_CACHE_REF}"
      --cache-to "type=registry,ref=${DOCKER_CACHE_REF},mode=max"
      ${DOCKER_TAG_ARGS}
      ${DOCKER_BUILD_ARGS}
      --push
      "${DOCKER_BUILD_CTX}"

.docker-build-job-release:
  extends: .docker-build-job
  before_script:
    - docker login -u "${CI_REGISTRY_USER}" -p "${CI_REGISTRY_PASSWORD}" "${CI_REGISTRY}"
    - |
      if [ -z "${DOCKER_HUB_USER}" ] || [ -z "${DOCKER_HUB_PASSWORD}" ]; then
        echo "ERROR: DOCKER_HUB_USER and DOCKER_HUB_PASSWORD required for release."
        exit 1
      fi
    - docker login -u "${DOCKER_HUB_USER}" -p "${DOCKER_HUB_PASSWORD}" docker.io
  script:
    - |
      export DOCKER_TAG_ARGS="--tag ${CI_REGISTRY_IMAGE}/${DOCKER_BUILD_CONTAINER_NAME}:${CI_COMMIT_REF_SLUG} \
        --tag ${CI_REGISTRY_IMAGE}/${DOCKER_BUILD_CONTAINER_NAME}:${QP_VERSION_PEP440} \
        --tag qpointz/${DOCKER_BUILD_CONTAINER_NAME}:${QP_VERSION_PEP440}"
    - export DOCKER_CACHE_REF="${CI_REGISTRY_IMAGE}/cache/${DOCKER_BUILD_CONTAINER_NAME}:${CI_COMMIT_REF_SLUG}"
    - docker buildx create --name "ci-builder-${CI_JOB_ID}" --driver docker-container --use
    - docker buildx inspect --bootstrap
    - >
      docker buildx build
      --file "${DOCKER_BUILD_FILE}"
      --cache-from "type=registry,ref=${DOCKER_CACHE_REF}"
      --cache-to "type=registry,ref=${DOCKER_CACHE_REF},mode=max"
      ${DOCKER_TAG_ARGS}
      ${DOCKER_BUILD_ARGS}
      --push
      "${DOCKER_BUILD_CTX}"
```

### Section 5: Python templates — cleaned

`.python-job` (2 lines: just pip variables) **inlined** into `.python-mill-py-unit-test`.

Changes:
- `stage: build` → `stage: test` (correct stage in new model)
- Remove dead `TEST_TYPE` variable (set but never read by script)
- Default `PY_VER` changed to `${DEFAULT_PY_VER}` (references global variable)

```yaml
.python-mill-py-unit-test:
  image: ${CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX}/python:${PY_VER}-slim
  variables:
    PY_VER: "${DEFAULT_PY_VER}"
    PIP_DISABLE_PIP_VERSION_CHECK: "1"
    PIP_ROOT_USER_ACTION: "ignore"
  script:
    - cd ${CI_PROJECT_DIR}/clients/mill-py
    - pip install poetry
    - poetry install --all-extras
    - poetry run pytest --cov=mill --cov-report=xml:build/coverage-results/coverage.xml --junitxml=build/test-results/results.xml tests/unit/
  artifacts:
    expire_in: "5 days"
    reports:
      junit: ${CI_PROJECT_DIR}/clients/mill-py/build/test-results/*.xml
      coverage_report:
        coverage_format: cobertura
        path: ${CI_PROJECT_DIR}/clients/mill-py/build/coverage-results/*.xml
    paths:
      - ${CI_PROJECT_DIR}/clients/mill-py/build/test-results/*.xml
      - ${CI_PROJECT_DIR}/clients/mill-py/build/coverage-results/*.xml
```

### Section 6: Remaining templates (unchanged or minimal edits)

- `.node-job` — kept as-is (3 lines, referenced by `ui/.gitlab-ci.yml`, acceptance, release)
- `.init-dev-certs` — kept; remove hardcoded `stage: init` so callers set their own stage
- `.semantic-release-job` — kept as-is (7 lines)
- `.apps-package-dist-template` — kept as-is (56 lines, used by packaging and release)
- `.clients-jdbc-shell-package-dist-template` — kept as-is (45 lines, release only)

## Files deleted

| File | Lines | Reason |
|------|-------|--------|
| `.gitlab/common/common.yml` | 2 | Merged |
| `.gitlab/common/jobs.yml` | 6 | Merged |
| `.gitlab/common/vars.yml` | 12 | Merged |
| `.gitlab/templates/gradle.yml` | 65 | Merged |
| `.gitlab/templates/docker.yml` | 67 | Merged |
| `.gitlab/templates/python.yml` | 31 | Merged |
| `.gitlab/templates/node.yml` | 3 | Merged |
| `.gitlab/templates/minica.yml` | 12 | Merged |
| `.gitlab/templates/release.yml` | 7 | Merged |
| `.gitlab/templates/apps-package-dist.yml` | 56 | Merged |
| `.gitlab/templates/clients-jdbc-shell-package-dist.yml` | 45 | Merged |

## Files created

| File | Content |
|------|---------|
| `.gitlab/common.yml` | All variables + all templates |

## Files updated

- `.gitlab-ci.yml` — include path `/.gitlab/common.yml`
- `.gitlab/pipelines/acceptance.yml` — include path
- `.gitlab/pipelines/release.yml` — include path, remove separate template includes
- `.gitlab/Makefile` — update grep pattern for `BUILD_TOOLS_VERSION` (see WI-272)
