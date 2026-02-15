"""Service discovery via ``/.well-known/mill``.

This module provides the :func:`fetch_descriptor` function and the
:class:`MillServiceDescriptor` model.  Currently discovery is a **stub** —
the descriptor is fetched and parsed, but automatic protocol selection
is not implemented yet.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

import httpx


# ---------------------------------------------------------------------------
# Descriptor model
# ---------------------------------------------------------------------------

@dataclass(frozen=True, slots=True)
class MillServiceEndpoint:
    """A single service endpoint advertised by the server.

    Attributes:
        stereotype: Service type (e.g. ``"grpc"``, ``"http-json"``, ``"http-protobuf"``).
        url: The endpoint URL.
    """

    stereotype: str = ""
    url: str = ""


@dataclass(frozen=True, slots=True)
class MillSecurityDescriptor:
    """Security configuration advertised by the server.

    Attributes:
        enabled: Whether authentication is required.
        auth_methods: List of supported auth methods (e.g. ``["BASIC", "OAUTH2"]``).
    """

    enabled: bool = False
    auth_methods: tuple[str, ...] = ()


@dataclass(frozen=True, slots=True)
class MillSchemaLink:
    """A schema reference in the discovery document.

    Attributes:
        name: Schema name.
        link: URL or path to the schema endpoint.
    """

    name: str = ""
    link: str = ""


@dataclass(frozen=True, slots=True)
class MillServiceDescriptor:
    """The parsed ``/.well-known/mill`` discovery document.

    Attributes:
        services: Available service endpoints.
        security: Security configuration.
        schemas: Schema references keyed by name.
    """

    services: tuple[MillServiceEndpoint, ...] = ()
    security: MillSecurityDescriptor = field(default_factory=MillSecurityDescriptor)
    schemas: dict[str, MillSchemaLink] = field(default_factory=dict)


# ---------------------------------------------------------------------------
# Fetch + parse
# ---------------------------------------------------------------------------

def _parse_descriptor(data: dict[str, Any]) -> MillServiceDescriptor:
    """Parse a raw JSON dict into a :class:`MillServiceDescriptor`.

    Args:
        data: The parsed JSON from ``/.well-known/mill``.

    Returns:
        A populated descriptor.
    """
    services = tuple(
        MillServiceEndpoint(
            stereotype=s.get("stereotype", ""),
            url=s.get("url", ""),
        )
        for s in data.get("services", [])
    )

    sec_data = data.get("security", {})
    security = MillSecurityDescriptor(
        enabled=sec_data.get("enabled", False),
        auth_methods=tuple(sec_data.get("authMethods", [])),
    )

    schemas_data = data.get("schemas", {})
    schemas = {
        k: MillSchemaLink(name=v.get("name", k), link=v.get("link", ""))
        for k, v in schemas_data.items()
    }

    return MillServiceDescriptor(
        services=services,
        security=security,
        schemas=schemas,
    )


def fetch_descriptor(host: str, *, port: int = 80, ssl: bool = False) -> MillServiceDescriptor:
    """Fetch and parse the ``/.well-known/mill`` discovery document.

    Args:
        host: Server hostname.
        port: Server port (default ``80``).
        ssl: Use HTTPS if ``True``.

    Returns:
        A :class:`MillServiceDescriptor`.

    Raises:
        MillConnectionError: If the server is unreachable.
        MillError: If the response cannot be parsed.
    """
    from mill.exceptions import MillConnectionError, MillError

    scheme = "https" if ssl else "http"
    url = f"{scheme}://{host}:{port}/.well-known/mill"
    try:
        resp = httpx.get(url, timeout=10.0)
    except httpx.TransportError as e:
        raise MillConnectionError(f"Discovery failed: {e}") from e

    if resp.status_code >= 400:
        raise MillError(
            f"Discovery endpoint returned HTTP {resp.status_code}",
            details=resp.text,
        )

    return _parse_descriptor(resp.json())
