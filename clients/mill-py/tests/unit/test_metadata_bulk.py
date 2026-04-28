"""Unit tests for mill.metadata.bulk — concat import and JSON export parse."""
from __future__ import annotations

import json
from pathlib import Path

import httpx
import pytest

from mill.metadata.aio.client import AsyncMetadataClient
from mill.metadata.bulk import (
    concat_metadata_yaml_documents,
    export_canonical,
    export_canonical_async,
    import_metadata_bundle,
    import_metadata_bundle_async,
    parse_metadata_export_json,
)
from mill.metadata.client import MetadataClient


def test_concat_metadata_yaml_documents_joins_in_order() -> None:
    a = "kind: MetadataScope\nscopeUrn: urn:mill/metadata/scope:global\n"
    b = "kind: MetadataEntity\nentityUrn: urn:mill/test:e1\nfacets: []\n"
    merged = concat_metadata_yaml_documents([a.strip(), b.strip()])
    assert merged.index("MetadataScope") < merged.index("---")
    assert merged.index("---") < merged.index("MetadataEntity")


def test_concat_skips_blank_parts() -> None:
    assert (
        concat_metadata_yaml_documents(["  \n", "kind: MetadataScope\nscopeUrn: urn:x\n"])
        == "kind: MetadataScope\nscopeUrn: urn:x"
    )


def test_import_metadata_bundle_single_multipart_with_separator(tmp_path: Path) -> None:
    f1 = tmp_path / "one.yaml"
    f2 = tmp_path / "two.yaml"
    f1.write_text("kind: MetadataScope\nscopeUrn: urn:mill/metadata/scope:global\n", encoding="utf-8")
    f2.write_text("kind: MetadataEntity\nentityUrn: urn:mill/model/schema:x\nfacets: []\n", encoding="utf-8")

    seen: dict[str, bytes] = {}

    def handler(request: httpx.Request) -> httpx.Response:
        if request.method == "POST" and request.url.path.endswith("/import"):
            seen["body"] = request.content
            return httpx.Response(
                200,
                json={"entitiesImported": 2, "facetTypesImported": 0, "errors": []},
            )
        return httpx.Response(404)

    transport = httpx.MockTransport(handler)
    with httpx.Client(transport=transport, base_url="http://test") as http:
        c = MetadataClient(http)
        r = import_metadata_bundle(c, [f1, f2], mode="MERGE", actor="itest")
    assert r.entities_imported == 2
    body = seen["body"]
    assert b"urn:mill/metadata/scope:global" in body
    assert b"urn:mill/model/schema:x" in body
    assert b"\n---\n" in body


def test_parse_metadata_export_json_round_trip() -> None:
    docs = [
        {"kind": "MetadataScope", "scopeUrn": "urn:mill/metadata/scope:global"},
        {"kind": "MetadataEntity", "entityUrn": "urn:mill/x:y", "facets": []},
    ]
    raw = json.dumps(docs)
    parsed = parse_metadata_export_json(raw)
    assert parsed == docs


def test_parse_metadata_export_json_rejects_non_array() -> None:
    with pytest.raises(ValueError, match="top-level"):
        parse_metadata_export_json('{"kind":"x"}')


def test_export_canonical_delegates_to_client() -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        if request.method == "GET" and request.url.path.endswith("/export"):
            q = str(request.url.query)
            assert "format=json" in q
            assert "scope=all" in q
            return httpx.Response(200, text="[]", headers={"content-type": "application/json"})
        return httpx.Response(404)

    transport = httpx.MockTransport(handler)
    with httpx.Client(transport=transport, base_url="http://test") as http:
        c = MetadataClient(http)
        text = export_canonical(c, scope="all", format="json")
    assert text == "[]"


@pytest.mark.asyncio
async def test_import_metadata_bundle_async() -> None:
    captured: dict[str, bytes] = {}

    def handler(request: httpx.Request) -> httpx.Response:
        if request.method == "POST" and request.url.path.endswith("/import"):
            captured["body"] = request.content
            return httpx.Response(
                200,
                json={"entitiesImported": 1, "facetTypesImported": 0, "errors": []},
            )
        return httpx.Response(404)

    transport = httpx.MockTransport(handler)
    async with httpx.AsyncClient(transport=transport, base_url="http://test") as http:
        c = AsyncMetadataClient(http)
        r = await import_metadata_bundle_async(
            c,
            ["kind: MetadataEntity\nentityUrn: urn:mill/a:b\nfacets: []\n"],
        )
    assert r.entities_imported == 1
    assert b"urn:mill/a:b" in captured["body"]


@pytest.mark.asyncio
async def test_export_canonical_async() -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        if request.method == "GET" and request.url.path.endswith("/export"):
            return httpx.Response(200, text="[]")
        return httpx.Response(404)

    transport = httpx.MockTransport(handler)
    async with httpx.AsyncClient(transport=transport, base_url="http://test") as http:
        c = AsyncMetadataClient(http)
        out = await export_canonical_async(c, format="json")
    assert out == "[]"
