# WI-276 — Metadata seed resource locations

## Goal

Make `mill.metadata.seed.resources` compatible with cloud protocol-addressed locations by continuing
to use Spring `ResourceLoader`, backed by cloud `ProtocolResolver`s in Spring autoconfigure.

## Problem

`MetadataSeedStartup` currently injects Spring `ResourceLoader`:

```kotlin
val seedKey = MetadataSeedKey.stableKey(resourceLoader, loc)
val bytes = resourceLoader.getResource(loc).inputStream.use { it.readBytes() }
```

This works for `classpath:` and `file:` but does not compose with Mill cloud storage support until
cloud modules register Spring protocol resolvers. It also needs stable-key logic that works for both
standard Spring resources and provider-specific cloud resources.

## Target Design

Update metadata seed loading to resolve configured locations through the Spring `ResourceLoader`
after cloud protocol resolver registration:

```yaml
mill:
  metadata:
    seed:
      resources:
        - classpath:metadata/platform-bootstrap.yaml
        - file:/etc/mill/metadata/platform-flow-facet-types.yaml
        - s3://mill-config-prod/metadata/skymill-canonical.yaml
        - gs://mill-config-prod/metadata/skymill-extras.yaml
        - azure-blob://config-container/metadata/finance-extras.yaml
```

Expected behaviour:

- Resource order remains exactly the configured list order.
- `on-failure` behaviour remains `fail-fast` / `continue`.
- Ledger skip behaviour remains based on **`metadata_seed.seed_key`** +
  **`metadata_seed.fingerprint`**, where the fingerprint is the existing MD5 over resolved resource
  bytes.
- Stable keys remain a deterministic metadata helper over configured locations / Spring resources.
  For cloud schemes, use the provider-defined canonical key from WI-277 when available. The key must
  be credential-free and include enough non-secret namespace identity to avoid collisions.
- Existing `classpath:` and `file:` seed locations continue to work.
- Bare local paths remain compatible as `file:`.
- Cloud provider metadata such as S3 ETag, GCS generation, Azure ETag, last-modified, or object
  version IDs is not used for skip/reapply decisions in this story.

## Implementation Shape

- Keep `ResourceLoader` usage in `MetadataSeedStartup`.
- Ensure provider protocol resolvers are registered before seed execution (ordering rules: **`STORY.md` → Decisions**).
- Replace or extend `MetadataSeedKey.stableKey(ResourceLoader, String)` so cloud resources get
  deterministic, credential-free keys. If provider canonicalization is ambiguous, fall back to a
  normalized configured location with sensitive parts stripped.
- Keep this work inside `metadata/mill-metadata-autoconfigure`; do not move Spring resource
  semantics into clean metadata core modules.
- Preserve current logging shape while using resource display locations.
- Read each seed resource at most once during a seed run where practical, reusing those bytes for
  both MD5 fingerprinting and import.

## Constraints

- No AWS/GCP/Azure imports in `metadata/mill-metadata-autoconfigure` or lower metadata modules.
- Do not change canonical metadata import semantics.
- Do not change the seed ledger schema.
- Avoid loading the same resource twice solely to compute the key.
- Do not introduce `BackendResourceLoader` into metadata unless Spring `ResourceLoader` proves
  insufficient.
- Do not add provider-metadata skip optimizations such as ETag/generation/version comparisons.

## Acceptance Criteria

- Metadata seeds load from `classpath:`, `file:`, and bare local paths through the Mill loader.
- Seed ledger keys are stable across process restarts and list reordering.
- Cloud seed ledger keys exclude credentials, SAS tokens, signed URL query parameters, and other
  temporary auth material.
- Cloud seed ledger keys include provider scheme plus non-secret namespace identity, such as
  bucket/key plus endpoint or account/container/blob where relevant.
- Changed seed content still re-applies; unchanged content still skips.
- Skip/reapply decisions are based on content MD5, not provider object metadata.
- Missing resources obey existing `on-failure` semantics.
- Tests cover stable-key behaviour for file/classpath and startup runner behaviour with the new
  loader.
- No cloud SDK dependency is introduced into metadata modules.

## Verification

- Run `metadata/mill-metadata-autoconfigure` tests.
- Run an integration test that seeds platform metadata from classpath plus a file location.

## Notes

Cloud seed coverage can be completed after WI-277 supplies provider implementations. This WI should
still avoid provider-specific assumptions.
