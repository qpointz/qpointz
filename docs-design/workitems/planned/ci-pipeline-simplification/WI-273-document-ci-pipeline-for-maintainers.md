# WI-273 — Document CI/CD pipeline for maintainers and agents

## Goal

After the pipeline restructuring is complete, update project documentation so that
maintainers and AI agents can understand the CI/CD architecture without reading every YAML
file. Two deliverables:

1. **Design doc** — `docs/design/build-system/ci-pipeline.md`
2. **RULES.md addendum** — CI/CD section in `docs/workitems/RULES.md`

## Deliverable 1: `docs/design/build-system/ci-pipeline.md`

A reference document covering the restructured pipeline. Contents:

### Pipeline architecture

- Stage model diagram (`build → test → test-integration → test-acceptance → package → publish → release`)
- Root orchestrator role: stage-activation variables, module includes, workflow rules
- Module-owned lifecycle: how each module CI file defines jobs for every stage it participates in

### File map

- `.gitlab-ci.yml` — what it does, how to read it
- `.gitlab/common.yml` — all shared templates and global variables
- `.gitlab/pipelines/acceptance.yml` — cross-module acceptance tests
- `.gitlab/pipelines/release.yml` — tag-triggered release jobs
- `<module>/.gitlab-ci.yml` — per-module lifecycle

### Stage-activation variables

Table of `RUN_TEST`, `RUN_INTEGRATION`, `RUN_ACCEPTANCE`, `RUN_PACKAGE` — default values,
what sets them, what they control.

### How to add a new module

Step-by-step: create `<module>/.gitlab-ci.yml` following the pattern, add include rule in
root `.gitlab-ci.yml`, add change paths.

### How to add a new stage to a module

Where to add the job, which variable gates it, how to test locally.

### Build tool version management

Where Gradle, JDK, Python, Node versions are controlled and how to update them.

### Templates reference

Brief description of each template in `common.yml`: `.gradle-job`, `.gradle-module-job`,
`.docker-build-job`, `.docker-build-job-release`, `.python-mill-py-unit-test`, `.node-job`,
`.init-dev-certs`, `.semantic-release-job`, `.apps-package-dist-template`,
`.clients-jdbc-shell-package-dist-template`.

### Acceptance test matrix

How mill-py and JDBC matrices work, how to add a new Python version or protocol.

### Feature-branch packaging

How to trigger Docker image builds on feature branches (`RUN_PACKAGE=true`), where to find
the built images.

### Release pipeline

What happens on a protected tag: Maven, Docker Hub, PyPI, docs.

## Deliverable 2: `docs/workitems/RULES.md` — CI/CD section

Add a section (e.g. `## CI/CD Pipeline`) to `RULES.md` after the existing content. This
provides concise, normative guidance for agents and maintainers working on CI:

- **File structure** — one-paragraph summary of where CI files live
- **Module CI contract** — when adding or modifying a module, its `.gitlab-ci.yml` must define
  jobs for all applicable stages (`build`, `test`, `test-integration`) following the pattern
  in `docs/design/build-system/ci-pipeline.md`
- **Root orchestrator** — do not add module-specific logic to the root `.gitlab-ci.yml`;
  module pipelines are self-contained
- **Template usage** — extend `.gradle-module-job` for Gradle modules; do not duplicate
  template content in module files
- **Variable hygiene** — dead variables must be removed; new variables need a `description`
  field
- **Cross-reference** — point to `docs/design/build-system/ci-pipeline.md` for full reference

## Execution note

This WI runs **last** — after WI-266 through WI-272 are complete and the pipeline is
functional. It documents the final state, not an intermediate one.

## Files affected

- `docs/design/build-system/ci-pipeline.md` — **create**
- `docs/workitems/RULES.md` — **append** CI/CD section
