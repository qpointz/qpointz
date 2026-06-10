# WI-268 — Acceptance pipeline with matrix

## Goal

Refactor the current `integration.yml` into a focused **acceptance pipeline**
(`.gitlab/pipelines/acceptance.yml`) that handles only cross-module acceptance testing:
service spin-up, mill-py client tests, and JDBC client tests. Use `parallel:matrix` to
collapse copy-pasted job definitions. Clean up template variables.

## Current state

`integration.yml` (416 lines) contains three concerns mixed together:

1. **Packaging** (3 jobs): `package:dev-certs`, `apps:integration-edition-package`,
   `apps:integration-edition-docker`
2. **Simple `testIT` jobs** (3 jobs): `core:integration`, `ai:integration`,
   `cloud:integration` — plain `./gradlew :module:testIT`
3. **Client acceptance tests** (12 jobs): 9 mill-py + 3 JDBC, all copy-pasted from templates

Concern 2 moves to module files (WI-266). This WI handles concerns 1 and 3.

## Target: `.gitlab/pipelines/acceptance.yml`

### Stages

```yaml
stages:
  - package
  - test-acceptance
```

### Packaging (integration edition)

Stays in acceptance pipeline — builds the service container that mill-py/JDBC tests connect to:

```yaml
package:dev-certs:
  extends: .init-dev-certs
  stage: package

acceptance:service-package:
  extends: .apps-package-dist-template
  stage: package
  variables:
    APP_EDITION: "integration"
  needs:
    - job: package:dev-certs
      artifacts: true

acceptance:service-docker:
  extends: .docker-build-job
  stage: package
  needs:
    - job: acceptance:service-package
      artifacts: true
  variables:
    APP_EDITION: "integration"
    DOCKER_BUILD_CTX: ${CI_PROJECT_DIR}/apps/mill-service
    DOCKER_BUILD_FILE: ${CI_PROJECT_DIR}/apps/mill-service/src/main/docker/base/Dockerfile
    DOCKER_BUILD_CONTAINER_NAME: mill-service-${APP_EDITION}
    DOCKER_BUILD_ARGS: --build-arg APP_EDITION=${APP_EDITION}
```

### mill-py acceptance (9 jobs → 1 definition)

```yaml
acceptance:mill-py:
  extends: .mill-py-acceptance-template
  stage: test-acceptance
  needs:
    - job: acceptance:service-docker
      artifacts: false
  parallel:
    matrix:
      - PY_VER: ["3.12", "3.13", "3.14"]
        MILL_IT_PROTOCOL: grpc
        MILL_IT_PORT: "9090"
      - PY_VER: ["3.12", "3.13", "3.14"]
        MILL_IT_PROTOCOL: [http-json, http-protobuf]
        MILL_IT_PORT: "8080"
```

### JDBC acceptance (3 jobs → 1 definition)

```yaml
acceptance:jdbc:
  extends: .mill-jdbc-acceptance-template
  stage: test-acceptance
  needs:
    - job: acceptance:service-docker
      artifacts: false
  parallel:
    matrix:
      - MILL_IT_PROTOCOL: grpc
        MILL_IT_PORT: "9090"
      - MILL_IT_PROTOCOL: [http-json, http-protobuf]
        MILL_IT_PORT: "8080"
```

### Template cleanup

Rename templates to match the new naming:
- `.mill-py-integration-template` → `.mill-py-acceptance-template`
- `.mill-jdbc-integration-template` → `.mill-jdbc-acceptance-template`

**Remove variables that never vary** from expanded jobs (keep only as template defaults):

| Variable | Template default | Varies? |
|----------|-----------------|---------|
| `MILL_IT_HOST` | `mill-it` (py) / `mill` (jdbc) | No |
| `MILL_IT_BASE_PATH` | `/services/jet` | No |
| `MILL_IT_TLS` | `false` | No |
| `MILL_IT_AUTH` | `none` | No |
| `MILL_IT_SCHEMA` | `skymill` | No |
| `MILL_IT_PLATFORM_ORIGIN` | `http://mill-it:8080` | No — normalize to default |

**Remove unused template variables** (defined but never consumed):

| Variable | Reason |
|----------|--------|
| `MILL_IT_TLS_CA` | Always empty, TLS disabled |
| `MILL_IT_TLS_CERT` | Always empty |
| `MILL_IT_TLS_KEY` | Always empty |
| `MILL_IT_USERNAME` | Set to `reader` but auth is `none` |
| `MILL_IT_PASSWORD` | Set to `reader` but auth is `none` |
| `MILL_IT_TOKEN` | Always empty |
| `CI_DEBUG_SERVICES` | Set to `false` (already the default) |
| `MILL_IT_PYTEST_PATH` | Always `tests/integration/` (already the script default path) |

Keep these variables **documented in comments** for future use (TLS/auth testing) but do not
set them in the template.

**Remove artificial sequential `needs:` chains** — current jobs chain grpc → http-json →
http-protobuf within each Python version. With `parallel:matrix`, each cell runs independently
with its own service container.

### Release pipeline PyPI consolidation (sub-task)

`publish:pypi-test` and `publish:pypi-prod` in `release.yml` share ~95% of script. Create
`.pypi-publish-template` in `common.yml`:

```yaml
.pypi-publish-template:
  stage: publish
  image: ${CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX}/python:${DEFAULT_PY_VER}-slim
  variables:
    PIP_DISABLE_PIP_VERSION_CHECK: "1"
    PIP_ROOT_USER_ACTION: "ignore"
  script:
    - cd ${CI_PROJECT_DIR}/clients/mill-py
    - pip install poetry
    - poetry install --all-extras
    - poetry version "${QP_VERSION_PEP440}"
    - poetry build
    - !reference [.pypi-publish-template, .publish-step]
  rules:
    - when: manual
```

Test and prod variants override only the registry config and verification command.

## Files affected

- `.gitlab/pipelines/integration.yml` → **rename** to `.gitlab/pipelines/acceptance.yml`
- `.gitlab/pipelines/acceptance.yml` — major refactor (416 → ~100 lines)
- `.gitlab/common.yml` — add `.mill-py-acceptance-template`, `.mill-jdbc-acceptance-template`,
  `.pypi-publish-template`
- `.gitlab/pipelines/release.yml` — use `.pypi-publish-template` for pypi jobs, remove dead
  `TEST_TYPE` variables, remove commented-out UI blocks and needs
- `.gitlab-ci.yml` — reference `acceptance.yml` instead of `integration.yml` (see WI-269)
