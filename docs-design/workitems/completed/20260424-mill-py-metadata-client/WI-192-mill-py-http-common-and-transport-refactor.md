# WI-192 — mill-py: shared HTTP common module + Jet transport refactor

Status: `planned`  
Type: `✨ feature`  
Area: `client`  
Story: [`STORY.md`](STORY.md)

## Goal

Centralise **httpx client construction** (scheme, `base_url`, path normalisation, TLS `verify`/`cert`, `Authorization` from [`Credential`](../../../../clients/mill-py/mill/auth.py)) and **HTTP error mapping** (`httpx.TransportError` → [`MillConnectionError`](../../../../clients/mill-py/mill/exceptions.py); 4xx/5xx → [`_from_http_status`](../../../../clients/mill-py/mill/exceptions.py)) so **platform REST** and **Jet HTTP** share one implementation.

Extend JSON error parsing as needed for [`MillStatusDetails`](../../../../core/mill-core/src/main/java/io/qpointz/mill/excepions/statuses/MillStatusDetails.java) (`status`, `message`, `timestamp`, `details`) returned by metadata and schema explorer exception handlers.

Refactor [`HttpTransport`](../../../../clients/mill-py/mill/_transport/_http.py) and [`AsyncHttpTransport`](../../../../clients/mill-py/mill/aio/_transport/_http.py) to use the new module **without behaviour change**.

Add **metadata entity path** helper: encode `{id}` segments for [`MetadataEntityController`](../../../../metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataEntityController.kt) (percent-encoding rules per controller KDoc).

## Acceptance

- New internal module (e.g. `mill/_http_common.py`) holds shared helpers; Jet transports call them.
- Existing unit tests for HTTP/async HTTP transport pass (`pytest -m unit` under [`clients/mill-py`](../../../../clients/mill-py)).
- Platform code can build an `httpx.Client` with **only** auth (and optional defaults), setting `Accept` / `Content-Type` **per request** (used by follow-on WIs).

## Out of scope

- Metadata or schema explorer endpoint wrappers (WI-193+).
- Changing Jet proto request/response semantics.
