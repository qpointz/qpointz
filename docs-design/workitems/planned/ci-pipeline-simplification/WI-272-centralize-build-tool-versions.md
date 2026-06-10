# WI-272 — Centralize build tool versions

## Goal

Ensure build tool versions (JDK, Gradle, Python, Node, Docker CLI, build-tools images) are
managed from a single source of truth so upgrades require minimal changes.

## Gradle version

**Already centralized** via `gradle/wrapper/gradle-wrapper.properties` (currently 9.4.0).
All CI jobs use `./gradlew` which downloads the wrapper-specified version.

`.gradle-job` cache key references this file for invalidation:

```yaml
cache:
  key:
    files:
      - gradle/wrapper/gradle-wrapper.properties
```

**Action:** verify this cache key is consistently inherited by `.gradle-module-job` and all
Gradle job variants. No change needed if `extends` propagates correctly.

## JDK version

Currently hardcoded as `21` in `.gradle-job` image tag.

**Action:** extract to global variable in `.gitlab/common.yml`:

```yaml
variables:
  JDK_VERSION: "21"

.gradle-job:
  image:
    name: ${CI_DEPENDENCY_PROXY_GROUP_IMAGE_PREFIX}/azul/zulu-openjdk:${JDK_VERSION}-latest
```

Also update `jdeps --multi-release 21` in `.apps-package-dist-template` and
`.clients-jdbc-shell-package-dist-template` to use `${JDK_VERSION}` if feasible (jdeps
requires a numeric argument — may need shell interpolation).

## Python default version

**Already centralized** via `DEFAULT_PY_VER: "3.13"` in `vars.yml`.

**Action:** ensure `.python-mill-py-unit-test` uses `${DEFAULT_PY_VER}` as its `PY_VER`
default instead of hardcoded `"3.12"` (see WI-267).

## Node.js version

Currently set via image tag in `.node-job`:

```yaml
.node-job:
  image: node:24-alpine
```

**Action (low priority):** optionally extract to `NODE_VERSION: "24"` global variable for
consistency. Not critical — Node version changes rarely.

## Build-tools image version

`BUILD_TOOLS_VERSION: 1.0.3` in `vars.yml` controls minica and semantic-release image tags.
Already centralized.

## `.gitlab/Makefile` dependency

The Makefile parses `common/vars.yml` to extract `BUILD_TOOLS_VERSION` and
`BUILD_TOOLS_REGISTRY`:

```makefile
BUILD_TOOLS_VERSION := $(shell grep "BUILD_TOOLS_VERSION:" $(CURDIR)/common/vars.yml | awk '{print $$2}')
BUILD_TOOLS_REGISTRY := $(shell grep "BUILD_TOOLS_REGISTRY:" $(CURDIR)/common/vars.yml | awk '{print $$2}')
```

When `vars.yml` is merged into `common.yml` (WI-267), the file path changes. The grep
pattern itself will still work (it matches on `BUILD_TOOLS_VERSION:` anywhere in the file).

**Action:** update the Makefile `$(CURDIR)/common/vars.yml` path to `$(CURDIR)/common.yml`.

## Version summary (after)

| Tool | Source of truth | Location |
|------|----------------|----------|
| Gradle | `distributionUrl` in properties file | `gradle/wrapper/gradle-wrapper.properties` |
| JDK | `JDK_VERSION` CI variable | `.gitlab/common.yml` |
| Python (default) | `DEFAULT_PY_VER` CI variable | `.gitlab/common.yml` |
| Node.js | Image tag in `.node-job` | `.gitlab/common.yml` |
| Build tools (minica, semantic-release) | `BUILD_TOOLS_VERSION` CI variable | `.gitlab/common.yml` |

## Files affected

- `.gitlab/common.yml` — add `JDK_VERSION`, update `.gradle-job` image tag, update
  `.python-mill-py-unit-test` default `PY_VER`
- `.gitlab/Makefile` — update `vars.yml` path to `common.yml`
- `.gitlab/templates/apps-package-dist.yml` — optionally use `${JDK_VERSION}` in jdeps flag
- `.gitlab/templates/clients-jdbc-shell-package-dist.yml` — same
