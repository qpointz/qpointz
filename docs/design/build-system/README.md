# Build System Documentation

Maintainer-oriented reference for how this repository is built and tested locally and in GitLab CI.

## Scope

This section covers:

- Gradle multi-module build architecture and conventions.
- Active GitLab CI file inventory and include graph.
- Practical maintenance recipes for common build and CI tasks.

## Documents

| File | Description |
|------|-------------|
| `gradle-architecture.md` | Root Gradle structure, version management, convention plugins, and module layout |
| `gitlab-ci-inventory.md` | Active CI topology, file ownership, and downstream pipeline model |
| `maintainer-recipes.md` | Day-2 operations: adding modules, changing pipelines, validating CI behavior |

## Out of Scope

- Product/domain design (AI, metadata, client protocol details).
- End-user documentation content.
- Release notes and changelog policy.

For publishing-specific implementation details, see `docs/design/publish/`.
