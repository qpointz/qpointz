"""Schema explorer client (mocked)."""
from __future__ import annotations

import httpx

from mill.schema_explorer.client import SchemaExplorerClient


def test_schema_explorer_context_and_list() -> None:
    ctx = {"selectedContext": "g", "availableScopes": [{"id": "1", "slug": "global", "displayName": "G"}]}

    def handler(request: httpx.Request) -> httpx.Response:
        p = request.url.path
        if p.endswith("/context"):
            return httpx.Response(200, json=ctx)
        if p == "/api/v1/schema":
            return httpx.Response(200, json=[])
        if p == "/api/v1/schema/schemas":
            return httpx.Response(200, json=[{"id": "m", "entityType": "MODEL", "schemaName": ""}])
        return httpx.Response(404, text=p)

    transport = httpx.MockTransport(handler)
    with httpx.Client(transport=transport, base_url="http://test") as http:
        c = SchemaExplorerClient(http)
        assert c.get_context().selected_context == "g"
        assert c.list_schemas() == []
        assert len(c.list_schemas(legacy_path=True)) == 1
