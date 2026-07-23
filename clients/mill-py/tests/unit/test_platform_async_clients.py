"""Async metadata and schema explorer clients (mocked)."""
from __future__ import annotations

import httpx
import pytest

from mill.metadata.aio.client import AsyncMetadataClient
from mill.schema_explorer.aio.client import AsyncSchemaExplorerClient


@pytest.mark.unit
async def test_async_metadata_list_scopes() -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        return httpx.Response(200, json=[])

    transport = httpx.MockTransport(handler)
    async with httpx.AsyncClient(transport=transport, base_url="http://t") as http:
        c = AsyncMetadataClient(http)
        assert await c.list_scopes() == []


@pytest.mark.unit
async def test_async_metadata_uses_urn_slug_entity_paths() -> None:
    entity_urn = "urn:mill/model/table:public.orders"
    facet_type_urn = "urn:mill/metadata/facet-type:descriptive"

    def handler(request: httpx.Request) -> httpx.Response:
        assert b"%2F" not in request.url.raw_path
        if request.url.path.endswith("/entities/mill-model-table:public.orders/facets"):
            return httpx.Response(200, json=[])
        if request.url.path.endswith("/facets/descriptive"):
            return httpx.Response(
                200,
                json={
                    "facetTypeUrn": facet_type_urn,
                    "title": "Description",
                    "description": "Description facet",
                    "contentSchema": {"type": "OBJECT", "title": "Description"},
                },
            )
        return httpx.Response(404, text=request.url.path)

    transport = httpx.MockTransport(handler)
    async with httpx.AsyncClient(transport=transport, base_url="http://t") as http:
        c = AsyncMetadataClient(http)
        assert await c.get_entity_facets(entity_urn) == []
        facet_type = await c.get_facet_type(facet_type_urn)
        assert facet_type.type_key == facet_type_urn


@pytest.mark.unit
async def test_async_schema_context() -> None:
    ctx = {"selectedContext": "g", "availableScopes": []}

    def handler(request: httpx.Request) -> httpx.Response:
        return httpx.Response(200, json=ctx)

    transport = httpx.MockTransport(handler)
    async with httpx.AsyncClient(transport=transport, base_url="http://t") as http:
        c = AsyncSchemaExplorerClient(http)
        out = await c.get_context()
        assert out.selected_context == "g"
