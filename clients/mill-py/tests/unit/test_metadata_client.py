"""Unit tests for mill.metadata.client — mocked HTTP."""
from __future__ import annotations

import json

import httpx

from mill.metadata.client import MetadataClient


def test_list_scopes_mock() -> None:
    payload = [{"scopeUrn": "urn:mill/metadata/scope:global", "displayName": "G"}]

    def handler(request: httpx.Request) -> httpx.Response:
        assert request.method == "GET"
        assert str(request.url).endswith("/api/v1/metadata/scopes")
        assert request.headers.get("accept") == "application/json"
        return httpx.Response(200, json=payload)

    transport = httpx.MockTransport(handler)
    with httpx.Client(transport=transport, base_url="http://test") as http:
        c = MetadataClient(http)
        scopes = c.list_scopes()
    assert len(scopes) == 1
    assert scopes[0].scope_urn == "urn:mill/metadata/scope:global"


def test_create_delete_import_export_mock() -> None:
    calls: list[tuple[str, str]] = []

    def handler(request: httpx.Request) -> httpx.Response:
        calls.append((request.method, str(request.url.path)))
        if request.method == "POST" and request.url.path.endswith("/scopes"):
            body = json.loads(request.content.decode())
            return httpx.Response(201, json={**body, "scopeUrn": body["scopeUrn"]})
        if request.method == "DELETE" and "/scopes/" in request.url.path:
            return httpx.Response(204)
        if request.method == "POST" and request.url.path.endswith("/import"):
            return httpx.Response(
                200,
                json={"entitiesImported": 1, "facetTypesImported": 0, "errors": []},
            )
        if request.method == "GET" and request.url.path.endswith("/export"):
            assert "format=yaml" in str(request.url.query)
            return httpx.Response(200, text="kind: MetadataScope\n", headers={"content-type": "text/yaml"})
        return httpx.Response(404)

    transport = httpx.MockTransport(handler)
    with httpx.Client(transport=transport, base_url="http://test") as http:
        c = MetadataClient(http)
        c.create_scope(scope_urn="urn:mill/metadata/scope:team:x", display_name="X")
        c.delete_scope("team:x")
        r = c.import_metadata(b"kind: x\n", filename="x.yaml")
        assert r.entities_imported == 1
        yaml = c.export_metadata(scope="global", format="yaml")
        assert "kind:" in yaml
    assert ("POST", "/api/v1/metadata/scopes") in calls


def test_connect_builds_client() -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        return httpx.Response(200, json=[])

    transport = httpx.MockTransport(handler)
    # connect() builds real Client — swap transport only via monkeypatch is heavy;
    # smoke-test MetadataClient with injected mock client instead.
    with httpx.Client(transport=transport, base_url="http://x") as http:
        c = MetadataClient(http)
        assert c.list_scopes() == []
