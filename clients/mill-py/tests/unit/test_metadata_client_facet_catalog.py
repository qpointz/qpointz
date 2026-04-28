"""MetadataClient facet catalog routes (mocked)."""
from __future__ import annotations

import json

import httpx

from mill.metadata.client import MetadataClient


def _manifest() -> dict:
    return {
        "facetTypeUrn": "urn:mill/metadata/facet-type:custom",
        "title": "Custom",
        "description": "d",
        "contentSchema": {"type": "OBJECT", "title": "t", "description": "x"},
    }


def test_facet_catalog_mock() -> None:
    m = _manifest()

    def handler(request: httpx.Request) -> httpx.Response:
        p = request.url.path
        if request.method == "GET" and p.endswith("/facets"):
            assert "enabledOnly=false" in str(request.url.query)
            return httpx.Response(200, json=[m])
        if request.method == "GET" and p.endswith("/facets/descriptive"):
            return httpx.Response(200, json=m)
        if request.method == "POST" and p.endswith("/facets"):
            json.loads(request.content.decode())
            return httpx.Response(201, json=m)
        if request.method == "PUT" and "/facets/" in p:
            return httpx.Response(200, json=m)
        if request.method == "DELETE" and p.endswith("/facets/custom"):
            return httpx.Response(204)
        return httpx.Response(404, text=p)

    transport = httpx.MockTransport(handler)
    with httpx.Client(transport=transport, base_url="http://test") as http:
        c = MetadataClient(http)
        types = c.list_facet_types()
        assert len(types) == 1
        assert c.get_facet_type("descriptive").type_key == m["facetTypeUrn"]
        c.register_facet_type(dict(m))
        c.update_facet_type("custom", dict(m))
        c.delete_facet_type("custom")
