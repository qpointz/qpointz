# WI-274 — Minimal backend resource contract and Spring adapter

## Goal

Introduce the smallest Spring-free backend resource loading contract needed for flow descriptor
reads, and provide Spring Boot wiring that backs it with the application `ResourceLoader` plus
registered custom protocol resolvers.

## Problem

The two immediate consumers use incompatible infrastructure:

- Flow descriptor loading uses `Path` / `File`, so it is effectively local filesystem only.
- Metadata seed loading uses Spring `ResourceLoader`, so adding cloud schemes such as `s3://`,
  `gs://`, and `azure-blob://` would either
  require Spring-specific protocol handlers or contaminate lower-level code with Spring concepts.

Mill needs a neutral backend contract that clean backend modules can depend on without importing
Spring or cloud SDKs. The implementation should stay close to Spring out of the box: Spring
`ResourceLoader` does the actual resolution; the backend contract is only an anti-contamination
adapter.

## Target Design

Create a small resource API in **`data/mill-data-backend-core`** (see **Decisions** in `STORY.md`).
`mill-data-backends` consumes the contract via its existing dependency on that module. Do not move
the contract into `core/mill-core`, and do not make `data/mill-data-source-calcite` depend on
`data/mill-data-backend-core` just to reuse it.

Preferred contract shape:

```java
public interface BackendResourceLoader {
    InputStream open(String location) throws IOException;
    String displayLocation(String location);
}
```

The exact names are implementation choices. Avoid adding provider registration, directory listing,
metadata probes, write support, or a parallel `Resource` hierarchy unless implementation proves it
necessary.

The Spring adapter must support:

- `classpath:` resources from a classloader;
- `file:` resources from the local filesystem;
- bare path compatibility by treating paths without a scheme as `file:`;
- clear errors for unknown schemes and missing resources;
- safe diagnostic display without exposing credentials.

Spring integration belongs in `data/mill-data-autoconfigure`:

- Define a `BackendResourceLoader` bean when none exists.
- Implement it as an adapter over Spring `ResourceLoader`.
- Use Spring `Resource` for `classpath:` and `file:` handling.
- Allow cloud modules to extend the same application `ResourceLoader` through
  `ProtocolResolver`s.
- Do not require clean backend modules to import `org.springframework.core.io.Resource`.

## Constraints

- No dependency on `org.springframework.*` in the backend contract module.
- No dependency on AWS, GCP, Azure, or other provider SDKs.
- Spring dependencies are allowed only in the autoconfigure adapter.
- Classpath and file lookup must work from tests and packaged applications through the Spring-backed
  adapter.
- Do not introduce a custom ServiceLoader/provider registry in this WI.
- Do not add a dependency from `data/mill-data-source-calcite` to `data/mill-data-backend-core`.

## Acceptance Criteria

- Clean backend modules compile against `BackendResourceLoader` only.
- The Spring adapter can open `classpath:` and `file:` resources as `InputStream`.
- Bare local paths resolve as local files for backwards compatibility.
- Unknown schemes fail with a precise error naming the unsupported scheme.
- The contract module has no Spring or cloud SDK dependency.
- Unit tests cover classpath, file, bare path, missing resource, and unknown scheme behaviour.

## Verification

- Run backend contract and data autoconfigure tests.
- Inspect dependency output or build files to confirm no Spring/cloud SDK dependency was introduced.

## Notes

This WI establishes the backend seam. It should not implement cloud providers; those belong to
`cloud/*` modules in WI-277.
