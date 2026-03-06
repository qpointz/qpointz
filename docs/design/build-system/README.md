# Build System Documentation

Maintainer reference for Gradle build logic and GitLab CI structure.

## Recommended Reading Order

1. `gradle-architecture.md` - repository-level Gradle structure and conventions
2. `gradle-plugins.md` - all `Mill*` plugins and extension components from `build-logic`
3. `gradle-editions.md` - edition DSL, inheritance, and edition-aware packaging behavior
4. `maintainer-recipes.md` - practical commands and checklists
5. `gitlab-ci-inventory.md` - CI include graph and ownership map

## Document Map

| File | Focus |
|------|-------|
| `gradle-architecture.md` | Build topology, module grouping, version/catalog conventions |
| `gradle-plugins.md` | Plugin catalog (`mill`, `mill-publish`, `mill-aggregate`) and internals |
| `gradle-editions.md` | Edition model, feature resolution, inheritance, packaging output behavior |
| `maintainer-recipes.md` | High-signal operational commands for maintainers |
| `gitlab-ci-inventory.md` | CI orchestration, shared templates, downstream pipelines |

## Scope

- Build logic under `build-logic/`
- Root Gradle architecture and module wiring
- Edition-aware packaging model
- Active GitLab CI orchestration topology

## Out of Scope

- Product/domain behavior docs
- End-user docs and tutorials
- Release notes/changelog policies

For publishing-domain design details, see `docs/design/publish/`.
