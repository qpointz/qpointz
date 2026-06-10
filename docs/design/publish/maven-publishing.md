# Maven Central Artifact Publishing

**Status:** Active  
**Last Updated:** February 2026

---

## Quick Start -- Local Testing

Build all publishable modules locally without signing:

```bash
make maven-local-publish
```

This runs `./gradlew clean publish publishSonatypeBundle` and produces:

- `build/repo/` -- local Maven repository with all module artifacts
- `build/sonatype-bundle/sonatype-bundle.zip` -- bundle ready for Maven Central upload

Inspect the output:

```bash
# List published modules
ls build/repo/io/qpointz/mill/

# Check a specific module's artifacts
ls build/repo/io/qpointz/mill/mill-core/0.1.0/
# mill-core-0.1.0.jar
# mill-core-0.1.0-sources.jar
# mill-core-0.1.0-javadoc.jar
# mill-core-0.1.0.pom
```

---

## What Gets Published

For each publishable module, four artifacts are produced:

| Artifact | Description |
|----------|-------------|
| `{module}-{version}.jar` | Compiled classes |
| `{module}-{version}-sources.jar` | Source code (Java + Kotlin) |
| `{module}-{version}-javadoc.jar` | Dokka-generated Javadoc (includes both Java and Kotlin classes) |
| `{module}-{version}.pom` | Maven POM with dependencies, license (Apache 2.0), SCM links |

When signing is enabled (CI), each artifact also gets a `.asc` signature and checksum files (`.md5`, `.sha1`, `.sha256`, `.sha512`).

---

## Enabling Publishing for a Module

1. Add the publish plugin to the module's `plugins {}` block:

   ```kotlin
   plugins {
       `java-library`
       id("io.qpointz.plugins.mill")
       id("io.qpointz.plugins.mill-publish")
       id("org.jetbrains.dokka")
       id("org.jetbrains.dokka-javadoc")
   }
   ```

2. Configure the module's `mill {}` extension:

   ```kotlin
   mill {
       publishArtifacts = true
       description = "Brief description of the module"
   }
   ```

3. Verify locally:

   ```bash
   make maven-local-publish
   ls build/repo/io/qpointz/mill/{module-name}/
   ```

---

## How It Works

### MillPublishPlugin

The plugin at `build-logic/src/main/kotlin/io/qpointz/mill/plugins/MillPublishPlugin.kt` (registered as `io.qpointz.plugins.mill-publish`) does the following when applied:

1. Waits for the `mill` plugin to be applied (via `withPlugin("mill")`)
2. After project evaluation, applies `maven-publish` and `signing` plugins
3. Configures `withSourcesJar()` on the Java extension
4. Registers a `javadocJar` task that packages `build/dokka/javadoc/` (Dokka-generated, includes both Java and Kotlin classes)
5. Configures PGP signing (skipped if `signing.keyId` property is absent)
6. Creates a `mavenJava` publication from the `java` component, attaching the Javadoc JAR
7. Publishes to a local file repository at `build/repo/`

### Sonatype Bundle

The root `build.gradle.kts` registers a `publishSonatypeBundle` task that zips `build/repo/` into `build/sonatype-bundle/sonatype-bundle.zip`. This zip is the artifact uploaded to Maven Central.

### POM Contents

Each module's POM includes:
- Group: `io.qpointz.mill`
- License: Apache License 2.0
- SCM: `https://github.com/qpointz/qpointz`
- Developer: qpointz

---

## CI Pipeline

Pipeline file: `.gitlab/pipelines/.maven.yml`

> **Not yet wired into the main `.gitlab-ci.yml`.** To enable, add an `include` directive for this file with appropriate rules.

### Job 1: `maven-publication-create`

Builds, signs, and bundles all publishable artifacts.

- **Image**: Azul Zulu JDK 21 (via `.gradle-job`)
- **Stage**: `publish`
- **Depends on**: `init-version` (for VERSION file)
- **Steps**:
  1. Decode PGP keyring from `SIGN_PGP_RING` CI variable
  2. Run `./gradlew clean publish publishSonatypeBundle` with signing properties
- **Artifacts**: `build/sonatype-bundle/` (expires in 5 days)

### Job 2: `maven-publish`

Uploads the bundle to Maven Central via the Sonatype API.

- **Stage**: `publish`
- **Depends on**: `maven-publication-create`, `init-version`
- **Steps**:
  1. Generate Bearer token from `SONATYPE_USERNAME`/`SONATYPE_PASSWORD`
  2. Determine publishing type: `AUTOMATIC` on `main`, `USER_MANAGED` on other branches
  3. Upload `sonatype-bundle.zip` to `https://central.sonatype.com/api/v1/publisher/upload`
- **Rules**: Only runs on protected refs, excluding `dev`

### Enabling the Pipeline

Add to `.gitlab-ci.yml`:

```yaml
  - local: ${CI_PROJECT_DIR}/.gitlab/pipelines/.maven.yml
    rules:
      - *if-protected
      - when: never
```

---

## CI Variables Required

Configure these in GitLab CI/CD Settings > Variables:

| Variable | Description | How to obtain |
|----------|-------------|---------------|
| `SIGN_PGP_RING` | Base64-encoded PGP secret keyring | `base64 < keyring.kbx` (or `base64 -w0 keyring.kbx` on Linux) |
| `SIGN_PGP_KEY_ID` | 8-character PGP key ID | `gpg --list-keys --keyid-format short` |
| `SIGN_PGP_PASS` | Passphrase for the PGP key | Set during key generation |
| `SONATYPE_USERNAME` | Sonatype Central username | Account at [central.sonatype.com](https://central.sonatype.com) |
| `SONATYPE_PASSWORD` | Sonatype Central password | Account at [central.sonatype.com](https://central.sonatype.com) |

Mark `SIGN_PGP_RING`, `SIGN_PGP_PASS`, and `SONATYPE_PASSWORD` as **masked** and **protected**.

---

## Signing Locally

To test a fully signed build locally (same as CI):

```bash
./gradlew clean publish publishSonatypeBundle \
  -Psigning.keyId=XXXXXXXX \
  -Psigning.password=your-passphrase \
  -Psigning.secretKeyRingFile=/path/to/keyring.kbx
```

Output will include `.asc` signature files for each artifact.

---

## History

This publishing setup is adapted from the v0.4.1 tag (`mill/` subfolder in the monorepo). Key differences from v0.4.1:

| Aspect | v0.4.1 | Current |
|--------|--------|---------|
| Project root | `mill/` subfolder | Repository root |
| Plugin ID | `mill-publish` (buildSrc) | `io.qpointz.plugins.mill-publish` (build-logic) |
| JDK version | 17 | 21 |
| Javadoc JAR source | Standard `javadoc` task (Java classes only) | Dokka javadoc (Java + Kotlin classes) |
| CI paths | `${CI_PROJECT_DIR}/mill/build/...` | `${CI_PROJECT_DIR}/build/...` |
