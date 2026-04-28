"""Unit tests for MetadataClient entity and facet routes (mocked)."""
from __future__ import annotations

import json

import httpx

from mill.metadata.client import MetadataClient


def _facet_row(uid: str = "u1") -> dict:
    return {
        "uid": uid,
        "facetTypeUrn": "urn:mill/facet:t",
        "scopeUrn": "urn:mill/metadata/scope:global",
        "origin": "CAPTURED",
        "originId": "repo",
        "assignmentUid": "a1",
        "payload": {},
        "createdAt": "2025-01-01T00:00:00Z",
        "lastModifiedAt": "2025-01-01T00:00:00Z",
    }


def test_entity_crud_and_facets_mock() -> None:
    entity_urn = "urn:mill/model/table:public.orders"
    base_entity = f"/api/v1/metadata/entities/{entity_urn}"

    def handler(request: httpx.Request) -> httpx.Response:
        p = request.url.path
        if request.method == "GET" and p.endswith("/entities"):
            return httpx.Response(200, json=[{"entityUrn": entity_urn, "kind": "table"}])
        if request.method == "GET" and p == base_entity:
            return httpx.Response(200, json={"entityUrn": entity_urn})
        if request.method == "POST" and p.endswith("/entities"):
            b = json.loads(request.content.decode())
            return httpx.Response(201, json=b)
        if request.method == "DELETE" and p == base_entity:
            return httpx.Response(204)
        if request.method == "GET" and p == f"{base_entity}/facets":
            return httpx.Response(200, json=[_facet_row()])
        if request.method == "GET" and p == f"{base_entity}/facets/merge-trace":
            return httpx.Response(
                200,
                json={"scopes": ["urn:mill/metadata/scope:global"], "entries": []},
            )
        if request.method == "GET" and p == f"{base_entity}/history":
            return httpx.Response(200, json=[])
        if request.method == "POST" and "/facets/" in p:
            return httpx.Response(200, json=_facet_row("new"))
        return httpx.Response(404, text="unhandled " + p)

    transport = httpx.MockTransport(handler)
    with httpx.Client(transport=transport, base_url="http://test") as http:
        c = MetadataClient(http)
        assert len(c.list_entities()) == 1
        assert c.get_entity(entity_urn).entity_urn == entity_urn
        c.create_entity(entity_urn=entity_urn, kind="table")
        c.delete_entity(entity_urn)
        assert len(c.get_entity_facets(entity_urn)) == 1
        assert c.get_facet_merge_trace(entity_urn).scopes
        assert c.get_entity_history(entity_urn) == []
        c.assign_facet(entity_urn, "descriptive", {"title": "x"})
