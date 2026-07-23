"""Tests for mill._http_common — platform helpers and path encoding."""
from __future__ import annotations

import httpx
import pytest

from mill._http_common import (
    encode_metadata_entity_path_segment,
    encode_metadata_prefixed_path_segment,
    jet_base_url,
    normalize_base_path,
    platform_client_headers,
    raise_for_status,
)
from mill.auth import BearerToken
from mill.exceptions import MillQueryError


def test_normalize_base_path() -> None:
    assert normalize_base_path("/api/v1/") == "/api/v1"
    assert normalize_base_path("api/v1") == "/api/v1"
    assert normalize_base_path("") == ""


def test_jet_base_url() -> None:
    assert jet_base_url("h", 80, ssl=False, base_path="/jet/") == "http://h:80/jet"


def test_platform_headers_only_authorization() -> None:
    h = platform_client_headers(BearerToken("x"))
    assert h == {"Authorization": "Bearer x"}
    assert platform_client_headers(None) == {}


def test_encode_metadata_entity_path_segment() -> None:
    assert encode_metadata_entity_path_segment("urn:mill/x:y:a/b") == "mill-x%3Ay%3Aa-b"


def test_encode_metadata_entity_path_segment_escapes_literal_hyphens() -> None:
    assert encode_metadata_entity_path_segment("urn:mill/x-y:a/b") == "mill-x--y%3Aa-b"


def test_encode_metadata_prefixed_path_segment() -> None:
    prefix = "urn:mill/metadata/facet-type:"
    assert encode_metadata_prefixed_path_segment(f"{prefix}examples-demo", prefix) == "examples-demo"
    assert encode_metadata_prefixed_path_segment("descriptive", prefix) == "descriptive"


def test_encode_metadata_prefixed_path_segment_rejects_foreign_urn() -> None:
    with pytest.raises(ValueError, match="does not belong"):
        encode_metadata_prefixed_path_segment(
            "urn:mill/model/table:public.orders",
            "urn:mill/metadata/facet-type:",
        )


def test_raise_for_status_ok() -> None:
    req = httpx.Request("GET", "http://example/x")
    resp = httpx.Response(200, request=req)
    raise_for_status(resp)  # no throw


def test_raise_for_status_mill_body() -> None:
    req = httpx.Request("GET", "http://example/metadata/x")
    body = '{"status":"NOT_FOUND","message":"missing","timestamp":1}'
    resp = httpx.Response(404, request=req, text=body)
    with pytest.raises(MillQueryError) as ei:
        raise_for_status(resp)
    err = ei.value
    assert err.mill_status == "NOT_FOUND"
    assert "missing" in str(err)
