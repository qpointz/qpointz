"""Integration tests for platform HTTP (metadata + schema explorer).

Requires ``MILL_IT_PLATFORM_ORIGIN`` (e.g. ``http://localhost:8080``). Reuses
``MILL_IT_*`` auth/TLS from the shared integration config.
"""
from __future__ import annotations

import httpx
import pytest

from mill.metadata import MetadataClient, parse_metadata_export_json
from mill.schema_explorer import SchemaExplorerClient


@pytest.mark.integration
def test_platform_metadata_list_scopes(platform_httpx: httpx.Client) -> None:
    client = MetadataClient(platform_httpx)
    scopes = client.list_scopes()
    assert isinstance(scopes, list)


@pytest.mark.integration
def test_platform_schema_context(platform_httpx: httpx.Client) -> None:
    client = SchemaExplorerClient(platform_httpx)
    ctx = client.get_context()
    assert ctx.selected_context


@pytest.mark.integration
def test_platform_schema_list_schemas(platform_httpx: httpx.Client) -> None:
    client = SchemaExplorerClient(platform_httpx)
    names = client.list_schemas()
    assert isinstance(names, list)


@pytest.mark.integration
def test_platform_metadata_export_json_round_trip(platform_httpx: httpx.Client) -> None:
    """JSON export is a top-level array of documents (WI-202); parse with stdlib-shaped helper."""
    client = MetadataClient(platform_httpx)
    body = client.export_metadata(format="json")
    docs = parse_metadata_export_json(body)
    assert isinstance(docs, list)
