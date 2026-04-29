# Miscellaneous (`misc/`)

Content here is **not** part of the root Gradle composite in `settings.gradle.kts` and is **not** built in CI unless explicitly called out.

| Path | Role |
|------|------|
| [`spring-3/`](spring-3/README.md) | Reference-only snapshots (e.g. historical **net.devh** gRPC layout). |
| [`cloud/mill-azure-service-function/`](cloud/README.md) | Legacy Azure Functions sample; Gradle `project(...)` names do not match the current product graph. |
| `local/`, `infra/` | Tooling, sandboxes, or infra helpers. |

For platform migration context see [`docs/design/platform/spring4-migration-plan.md`](../docs/design/platform/spring4-migration-plan.md).
