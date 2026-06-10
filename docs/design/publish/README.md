# Build, Release, and Documentation

Design documents for building, publishing, and generating documentation for Mill artifacts.

## Classification Criteria

A document belongs here if its **primary subject** is one of:

- Maven Central artifact publishing (signing, Sonatype, bundle upload)
- Documentation generation tooling (MkDocs, Dokka, API docs)
- Release processes, versioning strategies, CI/CD publishing pipelines
- Documentation site structure and preview workflows

## Does NOT Belong Here

- CI/CD pipeline configuration that isn't about publishing → `build-system/`
- General build tooling or Gradle conventions → `build-system/`
- User-facing documentation content (types, APIs) → respective domain folder

## Documents

| File | Description |
|------|-------------|
| `documentation-generation.md` | How to build the docs site (MkDocs + Dokka), preview locally, output locations |
| `dokka.md` | Kotlin Dokka documentation generator reference |
| `maven-publishing.md` | Maven Central artifact publishing: local testing, signing, Sonatype bundle |
