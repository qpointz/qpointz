"""Asynchronous metadata HTTP client (``/api/v1/metadata``)."""
from __future__ import annotations

import json
from typing import IO, Any, cast

import httpx

from mill._http_common import (
    build_platform_async_client,
    encode_metadata_entity_path_segment,
    raise_for_status,
)
from mill.auth import Credential
from mill.metadata.dto import (
    FacetInstanceDto,
    FacetMergeTraceResponseDto,
    FacetTypeManifestDto,
    ImportResultDto,
    MetadataAuditRecordDto,
    MetadataEntityDto,
    MetadataScopeDto,
    facet_instance_from_dict,
    facet_merge_trace_from_dict,
    facet_type_manifest_from_dict,
    import_result_from_dict,
    metadata_audit_record_from_dict,
    metadata_entity_from_dict,
    metadata_scope_from_dict,
)


class AsyncMetadataClient:
    """Metadata platform REST client.

    Args:
        http: An :class:`httpx.AsyncClient` configured with the Mill HTTP origin as
            ``base_url`` (no trailing slash). Default headers should include
            ``Authorization`` only; this client sets ``Accept`` / ``Content-Type``
            per call.
        prefix: URL path prefix for metadata APIs (default ``/api/v1/metadata``).
    """

    def __init__(self, http: httpx.AsyncClient, *, prefix: str = "/api/v1/metadata") -> None:
        self._http = http
        self._prefix = prefix.rstrip("/")
        self._entities = f"{self._prefix}/entities"

    @staticmethod
    def _read_context_params(
        scope: str | None = None,
        context: str | None = None,
        origin: str | None = None,
    ) -> dict[str, str]:
        q: dict[str, str] = {}
        if scope is not None:
            q["scope"] = scope
        if context is not None:
            q["context"] = context
        if origin is not None:
            q["origin"] = origin
        return q

    def _entity_segment(self, entity_id: str) -> str:
        return encode_metadata_entity_path_segment(entity_id)

    async def aclose(self) -> None:
        """Close the async HTTP client."""
        await self._http.aclose()

    async def __aenter__(self) -> AsyncMetadataClient:
        return self

    async def __aexit__(self, *exc: object) -> None:
        await self.aclose()

    async def list_scopes(self) -> list[MetadataScopeDto]:
        """``GET …/scopes`` — list all metadata scopes."""
        r = await self._http.get(
            f"{self._prefix}/scopes",
            headers={"Accept": "application/json"},
        )
        raise_for_status(
            r,
            request_headers=dict(r.request.headers),
        )
        data = r.json()
        if not isinstance(data, list):
            raise ValueError("scopes response must be a JSON array")
        return [metadata_scope_from_dict(cast(dict[str, Any], x)) for x in data]

    async def create_scope(
        self,
        *,
        scope_urn: str,
        display_name: str | None = None,
        owner_id: str | None = None,
    ) -> MetadataScopeDto:
        """``POST …/scopes`` — create a scope."""
        body: dict[str, Any] = {"scopeUrn": scope_urn}
        if display_name is not None:
            body["displayName"] = display_name
        if owner_id is not None:
            body["ownerId"] = owner_id
        r = await self._http.post(
            f"{self._prefix}/scopes",
            json=body,
            headers={"Accept": "application/json", "Content-Type": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("create scope response must be a JSON object")
        return metadata_scope_from_dict(cast(dict[str, Any], raw))

    async def delete_scope(self, scope_slug: str) -> None:
        """``DELETE …/scopes/{scopeSlug}`` — remove a scope (204)."""
        seg = encode_metadata_entity_path_segment(scope_slug)
        r = await self._http.delete(f"{self._prefix}/scopes/{seg}")
        raise_for_status(r, request_headers=dict(r.request.headers))

    async def import_metadata(
        self,
        file: bytes | IO[bytes],
        *,
        filename: str = "metadata.yaml",
        mode: str = "MERGE",
        actor: str = "system",
    ) -> ImportResultDto:
        """``POST …/import`` — multipart YAML upload."""
        files = {"file": (filename, file, "application/x-yaml")}
        data = {"mode": mode, "actor": actor}
        r = await self._http.post(
            f"{self._prefix}/import",
            data=data,
            files=files,
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("import response must be a JSON object")
        return import_result_from_dict(cast(dict[str, Any], raw))

    async def export_metadata(self, *, scope: str | None = None, format: str = "yaml") -> str:
        """``GET …/export`` — canonical metadata document stream (YAML or JSON per server)."""
        fmt = format.lower()
        if fmt not in ("yaml", "json"):
            raise ValueError("format must be 'yaml' or 'json'")
        params: dict[str, str] = {"format": fmt}
        if scope is not None:
            params["scope"] = scope
        accept = "application/json" if fmt == "json" else "text/yaml"
        r = await self._http.get(
            f"{self._prefix}/export",
            params=params,
            headers={"Accept": accept},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        return r.text

    # -- entities / facets (SPEC §10) --

    async def list_entities(self, *, kind: str | None = None) -> list[MetadataEntityDto]:
        """``GET …/entities`` — list entity identities (optional ``kind`` filter)."""
        params: dict[str, str] = {}
        if kind is not None:
            params["kind"] = kind
        r = await self._http.get(
            self._entities,
            params=params or None,
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        data = r.json()
        if not isinstance(data, list):
            raise ValueError("entities response must be a JSON array")
        return [metadata_entity_from_dict(cast(dict[str, Any], x)) for x in data]

    async def get_entity(self, entity_id: str) -> MetadataEntityDto:
        """``GET …/entities/{id}``."""
        r = await self._http.get(
            f"{self._entities}/{self._entity_segment(entity_id)}",
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("entity response must be a JSON object")
        return metadata_entity_from_dict(cast(dict[str, Any], raw))

    async def create_entity(
        self,
        *,
        entity_urn: str,
        kind: str | None = None,
    ) -> MetadataEntityDto:
        """``POST …/entities``."""
        body: dict[str, Any] = {"entityUrn": entity_urn}
        if kind is not None:
            body["kind"] = kind
        r = await self._http.post(
            self._entities,
            json=body,
            headers={"Accept": "application/json", "Content-Type": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("entity response must be a JSON object")
        return metadata_entity_from_dict(cast(dict[str, Any], raw))

    async def overwrite_entity(
        self,
        entity_id: str,
        *,
        entity_urn: str | None = None,
        kind: str | None = None,
    ) -> MetadataEntityDto:
        """``PUT …/entities/{id}`` — full replace."""
        body: dict[str, Any] = {}
        if entity_urn is not None:
            body["entityUrn"] = entity_urn
        if kind is not None:
            body["kind"] = kind
        r = await self._http.put(
            f"{self._entities}/{self._entity_segment(entity_id)}",
            json=body,
            headers={"Accept": "application/json", "Content-Type": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("entity response must be a JSON object")
        return metadata_entity_from_dict(cast(dict[str, Any], raw))

    async def patch_entity(
        self,
        entity_id: str,
        *,
        entity_urn: str | None = None,
        kind: str | None = None,
    ) -> MetadataEntityDto:
        """``PATCH …/entities/{id}`` (server maps to overwrite)."""
        body: dict[str, Any] = {}
        if entity_urn is not None:
            body["entityUrn"] = entity_urn
        if kind is not None:
            body["kind"] = kind
        r = await self._http.patch(
            f"{self._entities}/{self._entity_segment(entity_id)}",
            json=body,
            headers={"Accept": "application/json", "Content-Type": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("entity response must be a JSON object")
        return metadata_entity_from_dict(cast(dict[str, Any], raw))

    async def delete_entity(self, entity_id: str) -> None:
        """``DELETE …/entities/{id}`` (204)."""
        r = await self._http.delete(f"{self._entities}/{self._entity_segment(entity_id)}")
        raise_for_status(r, request_headers=dict(r.request.headers))

    async def get_entity_facets(
        self,
        entity_id: str,
        *,
        scope: str | None = None,
        context: str | None = None,
        origin: str | None = None,
    ) -> list[FacetInstanceDto]:
        """``GET …/entities/{id}/facets`` — merged facet rows."""
        r = await self._http.get(
            f"{self._entities}/{self._entity_segment(entity_id)}/facets",
            params=self._read_context_params(scope, context, origin) or None,
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        data = r.json()
        if not isinstance(data, list):
            raise ValueError("facets response must be a JSON array")
        return [facet_instance_from_dict(cast(dict[str, Any], x)) for x in data]

    async def get_entity_facets_by_type(
        self,
        entity_id: str,
        type_key: str,
        *,
        scope: str | None = None,
        context: str | None = None,
        origin: str | None = None,
    ) -> list[FacetInstanceDto]:
        """``GET …/entities/{id}/facets/{typeKey}``."""
        tk = self._entity_segment(type_key)
        r = await self._http.get(
            f"{self._entities}/{self._entity_segment(entity_id)}/facets/{tk}",
            params=self._read_context_params(scope, context, origin) or None,
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        data = r.json()
        if not isinstance(data, list):
            raise ValueError("facets response must be a JSON array")
        return [facet_instance_from_dict(cast(dict[str, Any], x)) for x in data]

    async def get_facet_merge_trace(
        self,
        entity_id: str,
        *,
        scope: str | None = None,
        context: str | None = None,
        origin: str | None = None,
    ) -> FacetMergeTraceResponseDto:
        """``GET …/entities/{id}/facets/merge-trace``."""
        r = await self._http.get(
            f"{self._entities}/{self._entity_segment(entity_id)}/facets/merge-trace",
            params=self._read_context_params(scope, context, origin) or None,
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("merge-trace response must be a JSON object")
        return facet_merge_trace_from_dict(cast(dict[str, Any], raw))

    async def assign_facet(
        self,
        entity_id: str,
        type_key: str,
        payload: Any,
        *,
        scope: str | None = None,
    ) -> FacetInstanceDto:
        """``POST …/entities/{id}/facets/{typeKey}``."""
        params: dict[str, str] = {}
        if scope is not None:
            params["scope"] = scope
        tk = self._entity_segment(type_key)
        r = await self._http.post(
            f"{self._entities}/{self._entity_segment(entity_id)}/facets/{tk}",
            params=params or None,
            json=payload,
            headers={"Accept": "application/json", "Content-Type": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("facet response must be a JSON object")
        return facet_instance_from_dict(cast(dict[str, Any], raw))

    async def patch_facet_payload(
        self,
        entity_id: str,
        type_key: str,
        facet_uid: str,
        payload: Any,
    ) -> FacetInstanceDto:
        """``PATCH …/entities/{id}/facets/{typeKey}/{facetUid}``."""
        tk = self._entity_segment(type_key)
        fu = self._entity_segment(facet_uid)
        r = await self._http.patch(
            f"{self._entities}/{self._entity_segment(entity_id)}/facets/{tk}/{fu}",
            json=payload,
            headers={"Accept": "application/json", "Content-Type": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("facet response must be a JSON object")
        return facet_instance_from_dict(cast(dict[str, Any], raw))

    async def delete_facet_by_uid(
        self,
        entity_id: str,
        type_key: str,
        facet_uid: str,
    ) -> None:
        """``DELETE …/entities/{id}/facets/{typeKey}/{facetUid}`` (204)."""
        tk = self._entity_segment(type_key)
        fu = self._entity_segment(facet_uid)
        r = await self._http.delete(
            f"{self._entities}/{self._entity_segment(entity_id)}/facets/{tk}/{fu}",
        )
        raise_for_status(r, request_headers=dict(r.request.headers))

    async def delete_facets_at_scope(
        self,
        entity_id: str,
        type_key: str,
        *,
        scope: str,
    ) -> None:
        """``DELETE …/entities/{id}/facets/{typeKey}?scope=…`` (204)."""
        tk = self._entity_segment(type_key)
        r = await self._http.delete(
            f"{self._entities}/{self._entity_segment(entity_id)}/facets/{tk}",
            params={"scope": scope},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))

    async def get_entity_history(self, entity_id: str) -> list[MetadataAuditRecordDto]:
        """``GET …/entities/{id}/history``."""
        r = await self._http.get(
            f"{self._entities}/{self._entity_segment(entity_id)}/history",
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        data = r.json()
        if not isinstance(data, list):
            raise ValueError("history response must be a JSON array")
        return [metadata_audit_record_from_dict(cast(dict[str, Any], x)) for x in data]

    # -- facet type catalog --

    async def list_facet_types(
        self,
        *,
        target_type: str | None = None,
        enabled_only: bool = False,
    ) -> list[FacetTypeManifestDto]:
        """``GET …/facets`` — facet catalog list."""
        params: dict[str, str] = {"enabledOnly": "true" if enabled_only else "false"}
        if target_type is not None:
            params["targetType"] = target_type
        r = await self._http.get(
            f"{self._prefix}/facets",
            params=params,
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        data = r.json()
        if not isinstance(data, list):
            raise ValueError("facet types response must be a JSON array")
        return [facet_type_manifest_from_dict(cast(dict[str, Any], x)) for x in data]

    async def get_facet_type(self, type_key: str) -> FacetTypeManifestDto:
        """``GET …/facets/{typeKey}``."""
        seg = self._entity_segment(type_key)
        r = await self._http.get(
            f"{self._prefix}/facets/{seg}",
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("facet type response must be a JSON object")
        return facet_type_manifest_from_dict(cast(dict[str, Any], raw))

    async def register_facet_type(self, manifest: dict[str, Any]) -> FacetTypeManifestDto:
        """``POST …/facets`` — register custom facet type (JSON body)."""
        body = json.dumps(manifest)
        r = await self._http.post(
            f"{self._prefix}/facets",
            content=body.encode("utf-8"),
            headers={"Accept": "application/json", "Content-Type": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("facet type response must be a JSON object")
        return facet_type_manifest_from_dict(cast(dict[str, Any], raw))

    async def update_facet_type(self, type_key: str, manifest: dict[str, Any]) -> FacetTypeManifestDto:
        """``PUT …/facets/{typeKey}``."""
        seg = self._entity_segment(type_key)
        body = json.dumps(manifest)
        r = await self._http.put(
            f"{self._prefix}/facets/{seg}",
            content=body.encode("utf-8"),
            headers={"Accept": "application/json", "Content-Type": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("facet type response must be a JSON object")
        return facet_type_manifest_from_dict(cast(dict[str, Any], raw))

    async def delete_facet_type(self, type_key: str) -> None:
        """``DELETE …/facets/{typeKey}`` (204)."""
        seg = self._entity_segment(type_key)
        r = await self._http.delete(f"{self._prefix}/facets/{seg}")
        raise_for_status(r, request_headers=dict(r.request.headers))


async def connect(
    base_url: str,
    *,
    prefix: str = "/api/v1/metadata",
    auth: Credential = None,
    tls_ca: str | bool | None = None,
    tls_cert: str | None = None,
    tls_key: str | None = None,
    timeout: float = 30.0,
) -> AsyncMetadataClient:
    """Build an :class:`AsyncMetadataClient` with a new :class:`httpx.AsyncClient`."""
    http = build_platform_async_client(
        base_url,
        auth=auth,
        tls_ca=tls_ca,
        tls_cert=tls_cert,
        tls_key=tls_key,
        timeout=timeout,
    )
    return AsyncMetadataClient(http, prefix=prefix)
