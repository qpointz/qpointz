# WI-196 — mill-py: metadata client — facet type catalog

Status: `planned`  
Type: `✨ feature`  
Area: `client`  
Story: [`STORY.md`](STORY.md)

## Goal

Implement **`MetadataClient`** methods for [`MetadataFacetController`](../../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataFacetController.kt): list facet types (filters `targetType`, `enabledOnly`), get by key, create, update, delete.

Request/response bodies align with [`FacetTypeManifest`](../../../../metadata/mill-metadata-core/src/main/kotlin/io/qpointz/mill/metadata/domain/facet/FacetTypeManifest.kt) JSON field names and aliases (already reflected in **WI-193** models).

## Dependencies

- **WI-192**, **WI-193**, **WI-194** (client shell), **WI-195** optional ordering if facet catalog is folded into same class file.

## Acceptance

- Unit tests for list/get/create/update/delete with mocked HTTP; normalise `typeKey` path segments per server rules.
