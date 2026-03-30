# WI-117 — Metadata cleanup: tests, remove file repository, dead code

Status: `planned`  
Type: `test` / `refactor`  
Area: `metadata`, `mill-data-schema-core`, `mill-data-schema-service`, `ui/mill-ui` (as touched by this story)  
Story: [`STORY.md`](./STORY.md)

## Goal

Update **unit and integration tests** for URN-only identity and removed APIs. **Remove `FileMetadataRepository`** and **`mill.metadata.storage.type=file`** autoconfigure for this story ([`STORY.md`](./STORY.md) — file mode may return later when the model is stable). **`MetadataRepository`** = **JPA** (or **NoOp** when nothing configured); no file+JPA composite until a new file adapter exists.

**Delete all unused and leftover code** from this story’s surface — see plan Phase 5 / [`STORY.md`](./STORY.md) **Cleanup**.

## In scope

- Test rewrites; fixture YAML/JSON ids as URNs; remove tests that only covered file repository or deleted behaviour.
- **Remove:** [`FileMetadataRepository`](../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/repository/file/FileMetadataRepository.kt) (or reduce to unused stub only if build graph requires — prefer **full removal**), [`MetadataRepositoryAutoConfiguration`](../../../metadata/mill-metadata-autoconfigure/src/main/kotlin/io/qpointz/mill/metadata/configuration/MetadataRepositoryAutoConfiguration.kt) **file** `@Bean`, related properties docs.
- **Production code deletion:** unused methods after **WI-113–WI-116** (grep per plan Phase 5).
- **`mill-ui`:** remove dead helpers tied to old metadata id rules when **WI-116** lands.
- **Flyway:** confirm **WI-115** removed **V5–V8** duplicates.

## Out of scope

- New product features unrelated to metadata URN delivery.
- Reintroducing file-backed metadata (future story).

## Configuration properties (Java)

- For changed **`mill.metadata.*`** configuration, implement **`@ConfigurationProperties` in Java** (migrate from Kotlin if needed) so **`spring-boot-configuration-processor`** generates **`META-INF/spring-configuration-metadata.json`** automatically — per [`STORY.md`](./STORY.md) **#12**.

## Code documentation (this WI)

- **Production** Kotlin/Java **removed or edited** (e.g. autoconfigure, deleted classes’ call sites): any **remaining** public API must still have **KDoc/JavaDoc** through **parameter level** where the code is **new or substantively changed** in this WI. **Test** classes/methods remain exempt.

## Acceptance criteria

- `./gradlew` tests for touched modules pass; `ui/mill-ui` **`npm run test`** / **`npm run build`** as applicable.
- **`mill.metadata.storage.type=file`** no longer creates a repository bean; docs and sample configs updated.
- No intentional leftover pre-URN behaviour in touched packages.
- **KDoc/JavaDoc** updated for any **non-test** production edits (deletions do not require docs on removed symbols).
- **`mill.metadata.*`** properties: **Java** `@ConfigurationProperties` + generated **`spring-configuration-metadata.json`** where keys changed.

## Commit

One logical commit for this WI, prefix `[test]` or `[change]`, per `docs/workitems/RULES.md`.
