"""Shared :mod:`httpx` client construction and HTTP error helpers.

Used by Jet transports and platform REST clients so TLS, auth, and error
mapping stay consistent.
"""
from __future__ import annotations

from urllib.parse import quote

import httpx

from mill.auth import Credential, _auth_headers
from mill.exceptions import MillConnectionError, _from_http_status


def normalize_base_path(base_path: str) -> str:
    """Return ``base_path`` with a single leading slash and no trailing slash."""
    return "/" + base_path.strip("/") if base_path else ""


def jet_base_url(host: str, port: int, *, ssl: bool, base_path: str) -> str:
    """Build the Jet HTTP ``base_url`` (scheme, host, port, path prefix)."""
    scheme = "https" if ssl else "http"
    path = normalize_base_path(base_path)
    return f"{scheme}://{host}:{port}{path}"


def tls_verify_and_cert(
    tls_ca: str | bool | None,
    tls_cert: str | None,
    tls_key: str | None,
) -> tuple[str | bool, tuple[str, str] | str | None]:
    """Return ``(verify, cert)`` tuple for :class:`httpx.Client` / :class:`httpx.AsyncClient`."""
    verify: str | bool = True
    if tls_ca is not None:
        verify = tls_ca
    cert: tuple[str, str] | str | None = None
    if tls_cert and tls_key:
        cert = (tls_cert, tls_key)
    elif tls_cert:
        cert = tls_cert
    return verify, cert


def jet_client_headers(auth: Credential, content_type: str) -> dict[str, str]:
    """Default Jet headers: ``Authorization``, ``Content-Type``, ``Accept``."""
    raw = _auth_headers(auth)
    headers: dict[str, str] = {}
    for _, v in raw.items():
        headers["Authorization"] = v
    headers["Content-Type"] = content_type
    headers["Accept"] = content_type
    return headers


def platform_client_headers(auth: Credential) -> dict[str, str]:
    """Minimal platform REST defaults: ``Authorization`` only (per-request CT/Accept)."""
    raw = _auth_headers(auth)
    headers: dict[str, str] = {}
    for _, v in raw.items():
        headers["Authorization"] = v
    return headers


def sanitize_headers_for_log(headers: dict[str, str]) -> dict[str, str]:
    """Copy headers with ``Authorization`` redacted for error messages."""
    redacted: dict[str, str] = {}
    for key, value in headers.items():
        if key.lower() == "authorization":
            redacted[key] = "<redacted>"
        else:
            redacted[key] = value
    return redacted


def connection_error_from_transport(
    exc: httpx.TransportError,
    *,
    method: str,
    url: str,
    headers: dict[str, str],
) -> MillConnectionError:
    """Wrap :class:`httpx.TransportError` as :class:`MillConnectionError`."""
    safe = sanitize_headers_for_log(headers)
    return MillConnectionError(
        str(exc),
        details=(f"HTTP {method} {url} headers={safe}"),
    )


def raise_for_status(
    resp: httpx.Response,
    *,
    request_method: str | None = None,
    request_headers: dict[str, str] | None = None,
) -> None:
    """Raise a :class:`mill.exceptions.MillError` if ``resp`` is an error status."""
    if resp.status_code < 400:
        return
    raise _from_http_status(
        resp.status_code,
        resp.text,
        request_method=request_method or resp.request.method,
        request_url=str(resp.request.url),
        request_headers=request_headers,
        response_headers=dict(resp.headers),
    )


def encode_metadata_entity_path_segment(entity_id: str) -> str:
    """Percent-encode a metadata entity ``{id}`` path segment for REST.

    Accepts a raw URN or slug; encodes so ``/`` becomes ``%2F`` per server rules.

    Args:
        entity_id: Entity URN or slug as returned by APIs (not pre-encoded).

    Returns:
        A single path segment safe to append after ``.../entities/``.
    """
    return quote(entity_id, safe="")


def build_platform_client(
    base_url: str,
    *,
    auth: Credential = None,
    tls_ca: str | bool | None = None,
    tls_cert: str | None = None,
    tls_key: str | None = None,
    timeout: float = 30.0,
    default_headers: dict[str, str] | None = None,
) -> httpx.Client:
    """Create an :class:`httpx.Client` for Mill platform HTTP (metadata, schema).

    ``base_url`` should be the service origin including any global prefix
    (e.g. ``http://localhost:8080``). Callers set ``Accept`` / ``Content-Type``
    per request.

    Args:
        base_url: Origin URL (no trailing slash required).
        auth: Optional credential.
        tls_ca: CA bundle path, ``False`` to disable verify, or ``None`` for default.
        tls_cert: Optional client certificate path.
        tls_key: Optional client private key path.
        timeout: Request timeout in seconds.
        default_headers: Optional extra default headers (merged after auth).

    Returns:
        A configured synchronous client (caller must :meth:`~httpx.Client.close`).
    """
    verify, cert = tls_verify_and_cert(tls_ca, tls_cert, tls_key)
    headers = platform_client_headers(auth)
    if default_headers:
        headers = {**headers, **default_headers}
    return httpx.Client(
        base_url=base_url.rstrip("/"),
        headers=headers,
        timeout=timeout,
        verify=verify,
        cert=cert,
    )


def build_platform_async_client(
    base_url: str,
    *,
    auth: Credential = None,
    tls_ca: str | bool | None = None,
    tls_cert: str | None = None,
    tls_key: str | None = None,
    timeout: float = 30.0,
    default_headers: dict[str, str] | None = None,
) -> httpx.AsyncClient:
    """Async variant of :func:`build_platform_client`."""
    verify, cert = tls_verify_and_cert(tls_ca, tls_cert, tls_key)
    headers = platform_client_headers(auth)
    if default_headers:
        headers = {**headers, **default_headers}
    return httpx.AsyncClient(
        base_url=base_url.rstrip("/"),
        headers=headers,
        timeout=timeout,
        verify=verify,
        cert=cert,
    )
