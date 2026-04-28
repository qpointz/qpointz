# WI-195 — mill-py: metadata client — entities and facets

Status: `planned`  
Type: `✨ feature`  
Area: `client`  
Story: [`STORY.md`](STORY.md)

## Goal

Extend **`MetadataClient`** to cover [`MetadataEntityController`](../../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataEntityController.kt):

- List/get/create/update/patch/delete entities (`{id}` encoding from **WI-192**).
- Facet operations: list facets, get by type, assign (POST), patch/delete by uid, unassign (DELETE by type).
- Read merge-trace and audit/history endpoints as exposed by the controller.

Respect query parameters documented on the controller: `scope`, `origin`, legacy `context` alias where applicable.

## Dependencies

- **WI-192**, **WI-193**; **WI-194** should exist so `MetadataClient` type is already introduced.

## Acceptance

- Methods map 1:1 to controller routes; unit tests with mocks for representative success and error responses (`MillStatusDetails`).
