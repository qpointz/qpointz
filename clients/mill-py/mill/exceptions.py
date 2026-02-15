"""Mill exception hierarchy.

All Mill client errors derive from :class:`MillError`.  Transport-layer
code maps gRPC status codes and HTTP status codes to the appropriate
subclass so callers can catch specific failure modes.
"""
from __future__ import annotations


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


def _from_http_status(status_code: int, body: str = "") -> MillError:
    """Convert an HTTP error status to the appropriate ``MillError`` subclass.

    Args:
        status_code: HTTP status code (4xx / 5xx).
        body: Optional response body for diagnostics.

    Returns:
        A ``MillError`` (or subclass) instance.
    """
    detail = body or f"HTTP {status_code}"
    if status_code in (401, 403):
        return MillAuthError(detail, details=f"HTTP {status_code}")
    if status_code in (400, 404, 422):
        return MillQueryError(detail, details=f"HTTP {status_code}")
    if status_code >= 500:
        return MillQueryError(detail, details=f"HTTP {status_code}")
    return MillError(detail, details=f"HTTP {status_code}")
