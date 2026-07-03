# WI-380 — Facet Type JSON Schema REST API

Status: `planned`  
Type: `feature`  
Area: `metadata`, `services`

## Problem Statement

The facet type catalog API exposes Mill manifests, but external consumers need a direct JSON Schema
artifact for validation and model/tool ingestion. Consumers should not have to duplicate Mill's
schema conversion logic.

## Goal

Expose a generated JSON Schema projection for a single facet type through the metadata facet type
API.

## Scope

1. Add service method support on `FacetTypeManagementService`.
2. Add `GET /api/v1/metadata/facets/{typeKey}/schema`.
3. Reuse existing type-key normalization and not-found behavior.
4. Return a JSON object with `$schema`, `$id`, structural schema keywords, and `x-mill-*`
   annotations.

## Acceptance Criteria

- `GET /api/v1/metadata/facets/descriptive/schema` returns HTTP 200 with JSON Schema.
- Unknown facet types return the same 404 behavior as the manifest endpoint.
- The endpoint describes one payload instance; `MULTIPLE` is annotation-only.
- Existing create/update/delete/list manifest endpoints are unchanged.

## Test Plan

- Controller test for successful schema response.
- Controller/service test coverage for not-found behavior through existing exception handling.
- Focused metadata service test task.
