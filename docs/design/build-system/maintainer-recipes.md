# Maintainer Recipes

## 1) Run Core Build Locally

From repository root:

```bash
./gradlew clean test compileTestIT
```

Useful focused commands:

```bash
./gradlew :core:mill-core:test
./gradlew :clients:mill-jdbc-driver:testIT
./gradlew :apps:mill-service:installBootDist :apps:mill-service:assembleSamples
```

## 2) Validate Pipeline Routing Variables

Root pipeline control variables:

- `RUN_INTEGRATION`
- `RUN_PACKAGING`
- `RUN_PUBLISH`

Typical combinations:

- Integration path: `RUN_INTEGRATION=true`
- Packaging-only path: `RUN_PACKAGING=true`
- Force publish include: `RUN_PUBLISH=true`
- Disable publish include: `RUN_PUBLISH=false`

## 3) Downstream Version Propagation

Parent computes version in `init-version` and passes it downstream as `QP_VERSION`.

Current downstream bridges in `.gitlab-ci.yml`:

- `packaging:downstream` passes `QP_VERSION: $BUILD_VERSION`
- `integration:downstream` passes `QP_VERSION: $BUILD_VERSION`

Downstream jobs rely on this variable to rehydrate `VERSION` when needed.

## 4) Add a New Java Module

Checklist:

1. Register module in `settings.gradle.kts`.
2. Add module build file and apply required convention plugin(s).
3. Use catalog dependencies from `libs.versions.toml`.
4. Add/verify test suites (`test`, optional `testIT`).
5. Update root or parent aggregate tasks if required.

## 5) Add a New CI Component Wrapper

For a new top-level module area:

1. Create wrapper in `.gitlab/components/<module>.yml`.
2. Add include + rules (`if` and `changes`).
3. Add wrapper include in `.gitlab-ci.yml`.
4. Keep job logic in module `.gitlab-ci.yml` or downstream pipeline file, not in root.

## 6) Add a New Packaging/Integration Matrix Axis

Prefer extending downstream pipelines, not root:

- Packaging changes in `.gitlab/pipelines/packaging.yml`
- Integration matrix changes in `.gitlab/pipelines/integration.yml`

Keep parent bridge contract stable (`QP_VERSION`, `strategy: depend`).

## 7) Migrating Docker Build Jobs

Current active template uses Docker Buildx in `.gitlab/common/jobs.yml`.

When changing build behavior:

1. Update shared template first.
2. Validate packaging downstream jobs.
3. Verify integration still pulls expected image tags (`mill-service-samples:${CI_COMMIT_REF_SLUG}`).

## 8) CI Safety Checklist Before Merge

- Root `.gitlab-ci.yml` remains orchestration-only.
- No secret/token logging in scripts.
- No reintroduction of deprecated Kaniko paths in active CI templates.
- Downstream triggers retain `strategy: depend` unless deliberately relaxed.

## 9) CI Lint and Structure Validation

Use this local syntax/structure check for root and active CI fragments:

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

## 10) Check Duplicate YAML Keys (Variables and Beyond)

Use a duplicate-key aware loader:

```bash
python3 - <<'PY'
from pathlib import Path
import yaml

class DupCheckLoader(yaml.SafeLoader):
    pass

def construct_mapping(loader, node, deep=False):
    mapping = {}
    for key_node, value_node in node.value:
        key = loader.construct_object(key_node, deep=deep)
        if key in mapping:
            raise ValueError(f"Duplicate key '{key}' at line {key_node.start_mark.line+1}")
        mapping[key] = loader.construct_object(value_node, deep=deep)
    return mapping

def anytag(loader, tag_suffix, node):
    if isinstance(node, yaml.SequenceNode):
        return loader.construct_sequence(node)
    if isinstance(node, yaml.MappingNode):
        return construct_mapping(loader, node)
    return loader.construct_scalar(node)

DupCheckLoader.add_constructor(
    yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG, construct_mapping
)
DupCheckLoader.add_multi_constructor('!', anytag)

targets = [Path(".gitlab-ci.yml")] + sorted(Path(".gitlab").rglob("*.yml"))
for p in targets:
    yaml.load(p.read_text(), Loader=DupCheckLoader)
print(f"No duplicate keys across {len(targets)} CI YAML files")
PY
```

## 11) Token Exposure Spot Check

Quick scan for obvious token echo/print patterns in active CI files:

```bash
rg "echo .*TOKEN|printenv|env\\b" .gitlab .gitlab-ci.yml
```

Expected result for active files: no direct token-value printing.
