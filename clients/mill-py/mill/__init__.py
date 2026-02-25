"""mill — Python client for Mill data services.

Quick start::

    from mill import connect, BasicAuth

    with connect("grpc://localhost:9090", auth=BasicAuth("user", "pass")) as client:
        for name in client.list_schemas():
            print(name)

        rs = client.query("SELECT * FROM MONETA.CLIENTS")
        for row in rs:
            print(row)
"""
from __future__ import annotations

from urllib.parse import urlparse
from typing import Any

from mill.auth import BasicAuth, BearerToken, Credential
from mill.client import MillClient
from mill.exceptions import (
    MillAuthError,
    MillConnectionError,
    MillError,
    MillQueryError,
)
from mill.result import ResultSet
from mill.types import MillField, MillSchema, MillTable, MillType

__all__ = [
    # Factory
    "connect",
    # Client
    "MillClient",
    # Result
    "ResultSet",
    # Types
    "MillType",
    "MillField",
    "MillTable",
    "MillSchema",
    # Auth
    "BasicAuth",
    "BearerToken",
    # Errors
    "MillError",
    "MillConnectionError",
    "MillQueryError",
    "MillAuthError",
]


def connect(
    url: str,
    *,
    auth: Credential = None,
    encoding: str = "json",
    tls_ca: str | bytes | None = None,
    tls_cert: str | bytes | None = None,
    tls_key: str | bytes | None = None,
    **kwargs: Any,
) -> MillClient:
    """Create a :class:`MillClient` connected to a Mill service.

    Args:
        url: Service URL.  Supported schemes:

            - ``grpc://host:port`` — insecure gRPC
            - ``grpcs://host:port`` — secure gRPC (TLS)
            - ``http://host:port/path`` — HTTP (JSON or protobuf encoding)
            - ``https://host:port/path`` — HTTPS

            If *url* has no recognised scheme (bare hostname), the client
            attempts service discovery via ``/.well-known/mill`` and raises
            ``NotImplementedError``.

        auth: Authentication credential (:class:`BasicAuth`,
            :class:`BearerToken`, or ``None`` for anonymous).
        encoding: HTTP encoding mode — ``"json"`` (default) or
            ``"protobuf"``.  Ignored for gRPC connections.
        tls_ca: Path to a PEM-encoded CA certificate file (or raw PEM bytes
            for gRPC).  Used to verify the server certificate.  ``None``
            uses system defaults.
        tls_cert: Path to a PEM-encoded client certificate file (or raw PEM
            bytes for gRPC) for mutual-TLS.
        tls_key: Path to a PEM-encoded private-key file (or raw PEM bytes
            for gRPC) for mutual-TLS.
        **kwargs: Override parsed URL components:

            - ``host`` — override hostname
            - ``port`` — override port
            - ``base_path`` — override HTTP path prefix

    Returns:
        An open :class:`MillClient`.  Use as a context manager to ensure
        cleanup::

            with connect("grpc://localhost:9090") as c:
                ...

    Raises:
        NotImplementedError: If the URL triggers service discovery (bare
            hostname without a scheme).
        ValueError: If the URL scheme is not recognised.
    """
    parsed = urlparse(url)
    scheme = parsed.scheme.lower()

    host = kwargs.get("host", parsed.hostname or "localhost")
    port = kwargs.get("port", parsed.port)

    if scheme in ("grpc", "grpcs"):
        from mill._transport._grpc import GrpcTransport

        ssl = scheme == "grpcs"
        port = port or (443 if ssl else 9090)
        transport = GrpcTransport(
            host, port, ssl=ssl, auth=auth,
            tls_ca=tls_ca, tls_cert=tls_cert, tls_key=tls_key,
        )
        return MillClient(transport)

    if scheme in ("http", "https"):
        from mill._transport._http import HttpTransport

        ssl = scheme == "https"
        port = port or (443 if ssl else 80)
        base_path = kwargs.get("base_path", parsed.path or "/services/jet")
        transport = HttpTransport(
            host, port, ssl=ssl, base_path=base_path, encoding=encoding, auth=auth,
            tls_ca=tls_ca, tls_cert=tls_cert, tls_key=tls_key,
        )
        return MillClient(transport)

    if not scheme:
        # Bare hostname → attempt discovery
        from mill.discovery import fetch_descriptor

        _descriptor = fetch_descriptor(host, port=port or 80)
        raise NotImplementedError(
            "Service discovery mode is not implemented yet. "
            "Use an explicit URL: grpc://host:port or http://host:port/path"
        )

    raise ValueError(f"Unsupported URL scheme: {scheme!r}. Use grpc://, grpcs://, http://, or https://.")
