# WI-097 — gRPC catalog and reference-tree hygiene

Status: `planned`  
Type: `refactoring`  
Area: `build`, `platform`  
Backlog refs: `P-5`  
Depends on: none

## Problem Statement

After moving production gRPC to raw **grpc-java**, the version catalog and reference copies may still
mention `net.devh` / `bootGRPC`. A stray `bootGRPC` version without library aliases, or a broken
`misc/spring-3/**/build.gradle.kts`, causes confusion and can break standalone builds of reference
trees.

## Goal

- Eliminate **dead or inconsistent** gRPC starter entries from `libs.versions.toml` once no
  in-repo production module needs them.
- Ensure **reference** layout under `misc/spring-3/` either builds with explicit coordinates
  documented as legacy-only, or is clearly excluded / documented as non-participating in the root
  composite build.

## Scope

1. Grep the repo for `net.devh`, `bootGRPC`, `grpc-spring-boot-starter`.
2. `libs.versions.toml`: remove `bootGRPC` version (and any `bootGRPC-*` library entries if still
   present) when nothing in the **product** Gradle graph resolves them; if `misc/spring-3` must keep
   net.devh for historical builds, either add **local** version declarations in that subtree only or
   pin coordinates there without polluting the root catalog.
3. Document in **one** sentence in `misc/spring-3/README` (or parent README) that the tree is
   reference-only and not shipped, if not already stated.

## Out of Scope

- Changing production gRPC implementation (already raw grpc-java in `services/mill-data-grpc-service`).

## Acceptance Criteria

- `./gradlew build` (or agreed CI target) succeeds from repo root.
- No production module depends on `net.devh` gRPC starters unless explicitly scoped to `misc/spring-3` only.
- Catalog has no orphan `bootGRPC` version key unless a real `libraries` entry uses `version.ref`.

## References

- [`docs/design/platform/spring4-migration-plan.md`](../../design/platform/spring4-migration-plan.md) — Critical #1, Appendix A, build table
