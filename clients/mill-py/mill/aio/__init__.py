"""mill.aio — Asynchronous Python client for Mill data services.

Mirrors the synchronous :mod:`mill` API with ``async``/``await``.

Quick start::

    from mill.aio import connect
    from mill.auth import BasicAuth

    async with await connect("grpc://localhost:9090", auth=BasicAuth("user", "pass")) as client:
        for name in await client.list_schemas():
            print(name)

        rs = await client.query("SELECT * FROM MONETA.CLIENTS")
        async for row in rs:
            print(row)
"""
from __future__ import annotations

from typing import Any
from urllib.parse import urlparse

from mill.aio.client import AsyncMillClient
from mill.aio.result import AsyncResultSet
from mill.auth import Credential

__all__ = [
    "connect",
    "AsyncMillClient",
    "AsyncResultSet",
]


async def connect(
    url: str,
    *,
    auth: Credential = None,
    encoding: str = "json",
    tls_ca: str | bytes | None = None,
    tls_cert: str | bytes | None = None,
    tls_key: str | bytes | None = None,
    **kwargs: Any,
) -> AsyncMillClient:
    """Create an :class:`AsyncMillClient` connected to a Mill service.

    Args:
        url: Service URL.  Supported schemes:

            - ``grpc://host:port`` — insecure gRPC
            - ``grpcs://host:port`` — secure gRPC (TLS)
            - ``http://host:port/path`` — HTTP (JSON or protobuf encoding)
            - ``https://host:port/path`` — HTTPS

        auth: Authentication credential (:class:`BasicAuth`,
            :class:`BearerToken`, or ``None`` for anonymous).
        encoding: HTTP encoding mode — ``"json"`` (default) or
            ``"protobuf"``.  Ignored for gRPC connections.
        tls_ca: Path to a PEM-encoded CA certificate file (or raw PEM bytes
            for gRPC).
        tls_cert: Path to a PEM-encoded client certificate file (or raw PEM
            bytes for gRPC) for mutual-TLS.
        tls_key: Path to a PEM-encoded private-key file (or raw PEM bytes
            for gRPC) for mutual-TLS.
        **kwargs: Override parsed URL components:

            - ``host`` — override hostname
            - ``port`` — override port
            - ``base_path`` — override HTTP path prefix

    Returns:
        An open :class:`AsyncMillClient`.  Use as an async context manager
        to ensure cleanup::

            async with await connect("grpc://localhost:9090") as c:
                ...

    Raises:
        NotImplementedError: If the URL triggers service discovery.
        ValueError: If the URL scheme is not recognised.
    """
    parsed = urlparse(url)
    scheme = parsed.scheme.lower()

    host = kwargs.get("host", parsed.hostname or "localhost")
    port = kwargs.get("port", parsed.port)

    if scheme in ("grpc", "grpcs"):
        from mill.aio._transport._grpc import AsyncGrpcTransport

        ssl = scheme == "grpcs"
        port = port or (443 if ssl else 9090)
        transport = AsyncGrpcTransport(
            host, port, ssl=ssl, auth=auth,
            tls_ca=tls_ca, tls_cert=tls_cert, tls_key=tls_key,
        )
        return AsyncMillClient(transport)

    if scheme in ("http", "https"):
        from mill.aio._transport._http import AsyncHttpTransport

        ssl = scheme == "https"
        port = port or (443 if ssl else 80)
        base_path = kwargs.get("base_path", parsed.path or "/services/jet")
        transport = AsyncHttpTransport(
            host, port, ssl=ssl, base_path=base_path, encoding=encoding, auth=auth,
            tls_ca=tls_ca, tls_cert=tls_cert, tls_key=tls_key,
        )
        return AsyncMillClient(transport)

    if not scheme:
        raise NotImplementedError(
            "Service discovery mode is not implemented yet. "
            "Use an explicit URL: grpc://host:port or http://host:port/path"
        )

    raise ValueError(f"Unsupported URL scheme: {scheme!r}. Use grpc://, grpcs://, http://, or https://.")
