"""Canonical multi-document metadata workflows (client-side YAML concat, JSON export parse)."""
from __future__ import annotations

import json
from pathlib import Path
from typing import Any, BinaryIO, Iterable, Union, cast

from mill.metadata.aio.client import AsyncMetadataClient
from mill.metadata.client import MetadataClient
from mill.metadata.dto import ImportResultDto

MetadataBundleSource = Union[Path, str, bytes, bytearray, memoryview, BinaryIO]


def _read_source_as_text(src: MetadataBundleSource) -> str:
    if isinstance(src, Path):
        return src.read_text(encoding="utf-8")
    if isinstance(src, str):
        return src
    if isinstance(src, (bytes, bytearray, memoryview)):
        return bytes(src).decode("utf-8")
    raw = src.read()
    if isinstance(raw, str):
        return raw
    return raw.decode("utf-8")


def concat_metadata_yaml_documents(sources: Iterable[MetadataBundleSource]) -> str:
    """Join fragments into one multi-document YAML stream for a single ``POST /import``.

    Non-empty pieces are trimmed; pieces are joined with ``\\n---\\n`` in iteration order.
    Caller order is import order (first piece becomes the first documents in the stream).

    Args:
        sources: Paths (read as UTF-8), raw YAML strings, or byte streams.
    """
    chunks: list[str] = []
    for s in sources:
        t = _read_source_as_text(s).strip()
        if t:
            chunks.append(t)
    return "\n---\n".join(chunks)


def import_metadata_bundle(
    client: MetadataClient,
    sources: Iterable[MetadataBundleSource],
    *,
    mode: str = "MERGE",
    actor: str = "system",
    filename: str = "metadata-bundle.yaml",
) -> ImportResultDto:
    """Import an ordered bundle via one multipart upload (concatenated YAML)."""
    payload = concat_metadata_yaml_documents(sources).encode("utf-8")
    return client.import_metadata(payload, filename=filename, mode=mode, actor=actor)


async def import_metadata_bundle_async(
    client: AsyncMetadataClient,
    sources: Iterable[MetadataBundleSource],
    *,
    mode: str = "MERGE",
    actor: str = "system",
    filename: str = "metadata-bundle.yaml",
) -> ImportResultDto:
    """Async variant of :func:`import_metadata_bundle`."""
    payload = concat_metadata_yaml_documents(sources).encode("utf-8")
    return await client.import_metadata(payload, filename=filename, mode=mode, actor=actor)


def export_canonical(
    client: MetadataClient,
    *,
    scope: str | None = None,
    format: str = "yaml",
) -> str:
    """Export canonical metadata; returns the raw response body (YAML or JSON text).

    Pass ``scope`` as for ``GET /api/v1/metadata/export`` (omit for global facet filter,
    comma-separated union, or ``all`` / ``*`` for all facet rows). ``format`` is ``yaml`` or ``json``.
    """
    return client.export_metadata(scope=scope, format=format)


async def export_canonical_async(
    client: AsyncMetadataClient,
    *,
    scope: str | None = None,
    format: str = "yaml",
) -> str:
    """Async variant of :func:`export_canonical`."""
    return await client.export_metadata(scope=scope, format=format)


def parse_metadata_export_json(body: str) -> list[dict[str, Any]]:
    """Parse a JSON metadata export body (top-level array of document objects, WI-202)."""
    data = json.loads(body)
    if not isinstance(data, list):
        raise ValueError("metadata JSON export must be a top-level JSON array")
    out: list[dict[str, Any]] = []
    for i, item in enumerate(data):
        if not isinstance(item, dict):
            raise ValueError(f"metadata JSON export document at index {i} must be a JSON object")
        out.append(cast(dict[str, Any], item))
    return out
