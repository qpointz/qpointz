# Maintainer Recipes

High-signal operational commands for Gradle and CI maintainers.

## Core Gradle Commands

From repository root:

```bash
./gradlew clean test testITClasses
```

Useful focused runs:

```bash
./gradlew :core:mill-core:test
./gradlew :clients:mill-jdbc-driver:testIT
./gradlew :apps:mill-service:installBootDist -Pedition=integration
```

## Editions Quick Ops

Inspect edition configuration:

```bash
./gradlew :apps:mill-service:millListEditions -Pedition=integration
./gradlew :apps:mill-service:millEditionMatrix
```

Inspect edition-specific dependencies:

```bash
./gradlew :apps:mill-service:dependencies --configuration runtimeClasspath -Pedition=minimal
./gradlew :apps:mill-service:dependencies --configuration runtimeClasspath -Pedition=integration
```

Verify edition content merge behavior:

```bash
./gradlew :apps:mill-service:installBootDist -Pedition=integration --console plain
```

Expected log pattern:

- lineage printed in inheritance order (base -> selected)
- missing edition directories reported as skipped
- selected edition synced last (override-friendly)

## Add a New Java Module

1. Add include in `settings.gradle.kts`.
2. Create module build file and apply required convention plugin(s).
3. Use dependency catalog aliases from `libs.versions.toml`.
4. Add/verify `test` and optional `testIT`.
5. Update aggregate tasks if module belongs to an aggregator project.

## CI Change Checklist

- Keep root `.gitlab-ci.yml` orchestration-only.
- Prefer extending downstream pipelines:
  - `.gitlab/pipelines/packaging.yml`
  - `.gitlab/pipelines/integration.yml`
- Keep downstream trigger contract stable (`QP_VERSION`, `strategy: depend`) unless intentionally changed.
- Avoid secret/token output in scripts.

For full CI topology and ownership map, see `gitlab-ci-inventory.md`.

## CI Validation Commands

Syntax and structure validation:

```bash
python3 - <<'PY'
from pathlib import Path
import yaml

class L(yaml.SafeLoader):
    pass

def anytag(loader, tag_suffix, node):
    if isinstance(node, yaml.SequenceNode):
        return loader.construct_sequence(node)
    if isinstance(node, yaml.MappingNode):
        return loader.construct_mapping(node)
    return loader.construct_scalar(node)

L.add_multi_constructor('!', anytag)
targets = [
    Path(".gitlab-ci.yml"),
    Path(".gitlab/common/common.yml"),
    Path(".gitlab/common/jobs.yml"),
    Path(".gitlab/common/vars.yml"),
    Path(".gitlab/pipelines/integration.yml"),
    Path(".gitlab/pipelines/packaging.yml"),
    Path(".gitlab/pipelines/publish.yml"),
]
for p in targets:
    yaml.load(p.read_text(), Loader=L)
    print(f"OK YAML: {p}")
PY
```

Token exposure spot check:

```bash
rg "echo .*TOKEN|printenv|env\\b" .gitlab .gitlab-ci.yml
```
