# WI-270 — Remove dead code, legacy, and unused artifacts

## Goal

Delete all identified dead code, unused files, stale comments, and unreferenced artifacts
in a single cleanup pass. Purely mechanical — no design decisions.

## Inventory

### Legacy folder (8 files — delete entire tree)

| File | Lines |
|------|-------|
| `.gitlab/legacy/clients/.gitlab/.gitlab-ci.yml` | — |
| `.gitlab/legacy/clients/.gitlab/pipelines/integration.yml` | — |
| `.gitlab/legacy/misc/.gitlab/vars.yml` | — |
| `.gitlab/legacy/misc/.gitlab/pipelines/ci-init.yml` | — |
| `.gitlab/legacy/misc/.gitlab/jobs.yml` | — |
| `.gitlab/legacy/misc/.gitlab-ci.yml` | — |
| `.gitlab/legacy/misc/.gitlab-ci-mill--old.yml` | — |
| `.gitlab/legacy/README.md` | — |

### Commented-out job blocks

| File | Block | Lines |
|------|-------|-------|
| `.gitlab/pipelines/test.yml` | `ui:v1:test` (retired mill-grinder-ui) | 25-40 |
| `.gitlab/pipelines/release.yml` | `ui:ui-v1:build` | 77-90 |
| `.gitlab/pipelines/release.yml` | `ui:ui-v2:build` | 92-105 |
| `.gitlab/pipelines/release.yml` | `needs` on `apps:minimal-edition-dist` | 110-116 |
| `.gitlab/pipelines/release.yml` | `needs` on `apps:samples-edition-dist` | 145-151 |
| `.gitlab/pipelines/integration.yml` | `ai:publish-regression` | 68-89 |
| `ui/.gitlab-ci.yml` | `ui:grinder-ui-v1:build` | 1-16 |

### Dead YAML anchors (root `.gitlab-ci.yml`)

| Anchor | Line |
|--------|------|
| `.if-protected` / `&if-protected` | 53 |
| `.if-mr-full` / `&if-mr-full` | 56 |
| `.if-run-integration` / `&if-run-integration` | 59 |
| `.build-changes` / `&build-changes` | 62 |

### Dead variables

| Variable | Location | Reason |
|----------|----------|--------|
| `IMAGE_DEPLOY_TOOLS` | `.gitlab/common/vars.yml` | Defined but never referenced by any CI job |
| `TEST_TYPE` | `.gitlab/templates/python.yml` | Set in template and pypi publish jobs but never read by any script |
| `RUN_PUBLISH` | `.gitlab-ci.yml` | Defined but never effectively used (semantic release gated by branch name) |

### Stale debug lines

| File | Line content |
|------|-------------|
| `core/.gitlab-ci.yml` | `echo $HOME` |
| `metadata/.gitlab-ci.yml` | `echo $HOME` |
| `persistence/.gitlab-ci.yml` | `echo $HOME` |
| `ai/.gitlab-ci.yml` | `echo $HOME` |
| `data/.gitlab-ci.yml` | `echo $HOME` |
| `cloud/.gitlab-ci.yml` | `echo $HOME` |
| `services/.gitlab-ci.yml` | `echo $HOME` |
| `ui/.gitlab-ci.yml` | `echo $HOME` |
| `.gitlab/pipelines/release.yml` | `ls -la` in `apps:samples-edition-docker` `before_script` |

### Stale comment in `integration.yml`

Line 267: `#   mill-py 3.13` — comment says 3.13 but the jobs below are for 3.14 (copy-paste
error).

### Unused Docker image

| File | Reason |
|------|--------|
| `.gitlab/docker/deploy-tools/Dockerfile` | Image built by Makefile, `IMAGE_DEPLOY_TOOLS` defined in vars, but **no CI job** references either. |

### Duplicate `$CI_COMMIT_TAG` guard

Root `.gitlab-ci.yml` `test:downstream` rules contain the same `$CI_COMMIT_TAG` check at
lines 188-189 and again at 192-193.

### Unused mill-py integration template variables

These are defined in `.mill-py-integration-template` and `.mill-jdbc-integration-template`
but never consumed (auth is `none`, TLS is `false`):

- `MILL_IT_TLS_CA`, `MILL_IT_TLS_CERT`, `MILL_IT_TLS_KEY`
- `MILL_IT_USERNAME`, `MILL_IT_PASSWORD`, `MILL_IT_TOKEN`
- `CI_DEBUG_SERVICES` (set to `false`, already the GitLab default)
- `MILL_IT_PYTEST_PATH` (set to `tests/integration/`, already the pytest path in script)

## Execution ordering

This WI can be executed **first** (before other WIs) as preparatory cleanup. It simplifies
the codebase that WI-266/267/268/269 will restructure, resulting in cleaner diffs. If the
module CI files will be fully rewritten by WI-266, the `echo $HOME` removals are
implicitly handled; but explicitly listing them here ensures nothing is missed if WI-270 runs
independently.

## Files affected

- `.gitlab/legacy/` — delete entire directory tree
- `.gitlab/docker/deploy-tools/` — delete (evaluate; if used outside CI, document instead)
- `.gitlab/pipelines/test.yml` — remove commented-out `ui:v1:test`
- `.gitlab/pipelines/integration.yml` — remove commented-out `ai:publish-regression`; fix
  `# mill-py 3.13` → `3.14` comment
- `.gitlab/pipelines/release.yml` — remove commented-out UI v1/v2 blocks, stale `needs`,
  debug `ls -la`
- `ui/.gitlab-ci.yml` — remove commented-out `ui:grinder-ui-v1:build`
- `.gitlab-ci.yml` — remove unused anchors, duplicate tag guard, `RUN_PUBLISH` variable
- `.gitlab/common/vars.yml` — remove `IMAGE_DEPLOY_TOOLS`
- `.gitlab/templates/python.yml` — remove `TEST_TYPE` variable
- 8 module CI files — remove `echo $HOME` lines
