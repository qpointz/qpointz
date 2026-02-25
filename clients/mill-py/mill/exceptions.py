"""Mill exception hierarchy.

All Mill client errors derive from :class:`MillError`.  Transport-layer
code maps gRPC status codes and HTTP status codes to the appropriate
subclass so callers can catch specific failure modes.
"""
from __future__ import annotations

import json
from typing import Any


class MillError(Exception):
    """Base exception for all Mill client errors.

    Args:
        message: Human-readable error description.
        details: Optional additional context (e.g. server error payload).
    """

    def __init__(self, message: str, *, details: str | None = None) -> None:
        super().__init__(message)
        self.details = details


class MillConnectionError(MillError):
    """Raised when the transport cannot reach the server.

    Covers DNS failures, connection refused, TLS handshake errors, and
    transport-level timeouts.
    """


class MillAuthError(MillError):
    """Raised when authentication or authorisation fails.

    Maps from gRPC ``UNAUTHENTICATED`` / ``PERMISSION_DENIED`` and
    HTTP 401 / 403.
    """


class MillQueryError(MillError):
    """Raised when the server rejects or fails a query.

    Covers SQL parse errors, execution errors, and invalid schema
    references.  Maps from gRPC ``INVALID_ARGUMENT`` / ``INTERNAL``
    and HTTP 400 / 500.
    """

    def __init__(
        self,
        message: str,
        *,
        details: str | None = None,
        status_code: int | None = None,
        error: str | None = None,
        path: str | None = None,
        timestamp: str | None = None,
        raw_body: str | None = None,
        request_method: str | None = None,
        request_url: str | None = None,
        request_headers: dict[str, str] | None = None,
        response_headers: dict[str, str] | None = None,
    ) -> None:
        super().__init__(message, details=details)
        self.status_code = status_code
        self.error = error
        self.path = path
        self.timestamp = timestamp
        self.raw_body = raw_body
        self.request_method = request_method
        self.request_url = request_url
        self.request_headers = request_headers
        self.response_headers = response_headers

    def as_dict(self) -> dict[str, Any]:
        """Return structured diagnostics for logging/telemetry."""
        return {
            "message": str(self),
            "details": self.details,
            "status_code": self.status_code,
            "error": self.error,
            "path": self.path,
            "timestamp": self.timestamp,
            "raw_body": self.raw_body,
            "request_method": self.request_method,
            "request_url": self.request_url,
            "request_headers": self.request_headers,
            "response_headers": self.response_headers,
        }


# ---------------------------------------------------------------------------
# Error mapping helpers
# ---------------------------------------------------------------------------

def _from_grpc_error(rpc_error: Exception) -> MillError:
    """Convert a ``grpc.RpcError`` to the appropriate ``MillError`` subclass.

    Args:
        rpc_error: A gRPC exception (must expose ``.code()`` and ``.details()``).

    Returns:
        A ``MillError`` (or subclass) instance.
    """
    import grpc  # lazy — avoid import cost when not using gRPC

    code = rpc_error.code()  # type: ignore[union-attr]
    detail = rpc_error.details()  # type: ignore[union-attr]

    if code in (grpc.StatusCode.UNAUTHENTICATED, grpc.StatusCode.PERMISSION_DENIED):
        return MillAuthError(detail or "Authentication failed", details=str(code))
    if code in (grpc.StatusCode.UNAVAILABLE, grpc.StatusCode.DEADLINE_EXCEEDED):
        return MillConnectionError(detail or "Connection failed", details=str(code))
    if code in (grpc.StatusCode.INVALID_ARGUMENT, grpc.StatusCode.NOT_FOUND):
        return MillQueryError(detail or "Query error", details=str(code))
    # Catch-all
    return MillError(detail or str(rpc_error), details=str(code))


def _from_http_status(
    status_code: int,
    body: str = "",
    *,
    request_method: str | None = None,
    request_url: str | None = None,
    request_headers: dict[str, str] | None = None,
    response_headers: dict[str, str] | None = None,
) -> MillError:
    """Convert an HTTP error status to the appropriate ``MillError`` subclass.

    Args:
        status_code: HTTP status code (4xx / 5xx).
        body: Optional response body for diagnostics.

    Returns:
        A ``MillError`` (or subclass) instance.
    """
    payload = _parse_http_body(body)
    detail = payload.get("message") or body or f"HTTP {status_code}"

    query_error = MillQueryError(
        detail,
        details=f"HTTP {status_code}",
        status_code=status_code,
        error=payload.get("error"),
        path=payload.get("path"),
        timestamp=payload.get("timestamp"),
        raw_body=body or None,
        request_method=request_method,
        request_url=request_url,
        request_headers=request_headers,
        response_headers=response_headers,
    )

    if status_code in (401, 403):
        return MillAuthError(detail, details=f"HTTP {status_code}")
    if status_code in (400, 404, 422):
        return query_error
    if status_code >= 500:
        return query_error
    return MillError(detail, details=f"HTTP {status_code}")


def _parse_http_body(body: str) -> dict[str, str]:
    """Best-effort parse for standard JSON error envelopes."""
    if not body:
        return {}
    try:
        parsed = json.loads(body)
    except Exception:
        return {}
    if not isinstance(parsed, dict):
        return {}
    result: dict[str, str] = {}
    for key in ("timestamp", "status", "error", "message", "path"):
        value = parsed.get(key)
        if isinstance(value, str):
            result[key] = value
        elif value is not None:
            result[key] = str(value)
    return result
