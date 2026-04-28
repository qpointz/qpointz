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
async def test_async_schema_context() -> None:
    ctx = {"selectedContext": "g", "availableScopes": []}

    def handler(request: httpx.Request) -> httpx.Response:
        return httpx.Response(200, json=ctx)

    transport = httpx.MockTransport(handler)
    async with httpx.AsyncClient(transport=transport, base_url="http://t") as http:
        c = AsyncSchemaExplorerClient(http)
        out = await c.get_context()
        assert out.selected_context == "g"
