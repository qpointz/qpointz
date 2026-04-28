"""Synchronous schema explorer HTTP client (``/api/v1/schema``)."""
from __future__ import annotations

from typing import Any, cast
from urllib.parse import quote

import httpx

from mill._http_common import build_platform_client, raise_for_status
from mill.auth import Credential
from mill.schema_explorer.dto import (
    ColumnDto,
    ModelRootDto,
    SchemaContextDto,
    SchemaDto,
    SchemaExplorerTreeDto,
    SchemaListItemDto,
    TableDto,
    column_dto_from_dict,
    model_root_from_dict,
    schema_context_from_dict,
    schema_dto_from_dict,
    schema_explorer_tree_from_dict,
    schema_list_item_from_dict,
    table_dto_from_dict,
)


def _path_seg(segment: str) -> str:
    """Percent-encode one URL path segment."""
    return quote(segment, safe="")


class SchemaExplorerClient:
    """Read-only schema + merged-facets explorer."""

    def __init__(self, http: httpx.Client, *, prefix: str = "/api/v1/schema") -> None:
        self._http = http
        self._prefix = prefix.rstrip("/")

    def close(self) -> None:
        self._http.close()

    def __enter__(self) -> SchemaExplorerClient:
        return self

    def __exit__(self, *exc: object) -> None:
        self.close()

    @staticmethod
    def _read_params(
        scope: str | None = None,
        context: str | None = None,
        origin: str | None = None,
        facet_mode: str | None = None,
    ) -> dict[str, str]:
        q: dict[str, str] = {}
        if scope is not None:
            q["scope"] = scope
        if context is not None:
            q["context"] = context
        if origin is not None:
            q["origin"] = origin
        if facet_mode is not None:
            q["facetMode"] = facet_mode
        return q

    def get_context(self) -> SchemaContextDto:
        """``GET …/context``."""
        r = self._http.get(
            f"{self._prefix}/context",
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("context response must be JSON object")
        return schema_context_from_dict(cast(dict[str, Any], raw))

    def list_schemas(
        self,
        *,
        scope: str | None = None,
        context: str | None = None,
        origin: str | None = None,
        facet_mode: str | None = None,
        legacy_path: bool = False,
    ) -> list[SchemaListItemDto]:
        """``GET …/schema`` or legacy ``GET …/schema/schemas``."""
        path = f"{self._prefix}/schemas" if legacy_path else self._prefix
        r = self._http.get(
            path,
            params=self._read_params(scope, context, origin, facet_mode) or None,
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        data = r.json()
        if not isinstance(data, list):
            raise ValueError("schema list must be JSON array")
        return [schema_list_item_from_dict(cast(dict[str, Any], x)) for x in data]

    def get_tree(
        self,
        *,
        scope: str | None = None,
        context: str | None = None,
        origin: str | None = None,
        facet_mode: str | None = None,
    ) -> SchemaExplorerTreeDto:
        """``GET …/tree``."""
        r = self._http.get(
            f"{self._prefix}/tree",
            params=self._read_params(scope, context, origin, facet_mode) or None,
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("tree response must be JSON object")
        return schema_explorer_tree_from_dict(cast(dict[str, Any], raw))

    def get_model_root(
        self,
        *,
        scope: str | None = None,
        context: str | None = None,
        origin: str | None = None,
        facet_mode: str | None = None,
    ) -> ModelRootDto:
        """``GET …/model``."""
        r = self._http.get(
            f"{self._prefix}/model",
            params=self._read_params(scope, context, origin, facet_mode) or None,
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("model response must be JSON object")
        return model_root_from_dict(cast(dict[str, Any], raw))

    def get_schema(
        self,
        schema_name: str,
        *,
        scope: str | None = None,
        context: str | None = None,
        origin: str | None = None,
        facet_mode: str | None = None,
        legacy_path: bool = False,
    ) -> SchemaDto:
        """``GET …/{schemaName}`` or legacy ``…/schemas/{schemaName}``."""
        sn = _path_seg(schema_name)
        path = f"{self._prefix}/schemas/{sn}" if legacy_path else f"{self._prefix}/{sn}"
        r = self._http.get(
            path,
            params=self._read_params(scope, context, origin, facet_mode) or None,
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("schema detail must be JSON object")
        return schema_dto_from_dict(cast(dict[str, Any], raw))

    def get_table(
        self,
        schema_name: str,
        table_name: str,
        *,
        scope: str | None = None,
        context: str | None = None,
        origin: str | None = None,
        facet_mode: str | None = None,
        legacy_path: bool = False,
    ) -> TableDto:
        """Table detail (modern or legacy path)."""
        sn, tn = _path_seg(schema_name), _path_seg(table_name)
        if legacy_path:
            path = f"{self._prefix}/schemas/{sn}/tables/{tn}"
        else:
            path = f"{self._prefix}/{sn}/tables/{tn}"
        r = self._http.get(
            path,
            params=self._read_params(scope, context, origin, facet_mode) or None,
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("table detail must be JSON object")
        return table_dto_from_dict(cast(dict[str, Any], raw))

    def get_column(
        self,
        schema_name: str,
        table_name: str,
        column_name: str,
        *,
        scope: str | None = None,
        context: str | None = None,
        origin: str | None = None,
        facet_mode: str | None = None,
        legacy_path: bool = False,
    ) -> ColumnDto:
        """Column detail (modern or legacy path)."""
        sn, tn, cn = _path_seg(schema_name), _path_seg(table_name), _path_seg(column_name)
        if legacy_path:
            path = f"{self._prefix}/schemas/{sn}/tables/{tn}/columns/{cn}"
        else:
            path = f"{self._prefix}/{sn}/tables/{tn}/columns/{cn}"
        r = self._http.get(
            path,
            params=self._read_params(scope, context, origin, facet_mode) or None,
            headers={"Accept": "application/json"},
        )
        raise_for_status(r, request_headers=dict(r.request.headers))
        raw = r.json()
        if not isinstance(raw, dict):
            raise ValueError("column detail must be JSON object")
        return column_dto_from_dict(cast(dict[str, Any], raw))


def connect(
    base_url: str,
    *,
    prefix: str = "/api/v1/schema",
    auth: Credential = None,
    tls_ca: str | bool | None = None,
    tls_cert: str | None = None,
    tls_key: str | None = None,
    timeout: float = 30.0,
) -> SchemaExplorerClient:
    """Factory: new :class:`httpx.Client` + :class:`SchemaExplorerClient`."""
    http = build_platform_client(
        base_url,
        auth=auth,
        tls_ca=tls_ca,
        tls_cert=tls_cert,
        tls_key=tls_key,
        timeout=timeout,
    )
    return SchemaExplorerClient(http, prefix=prefix)
