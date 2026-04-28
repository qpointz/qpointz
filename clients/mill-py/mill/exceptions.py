"""Mill exception hierarchy.

All Mill client errors derive from :class:`MillError`.  Transport-layer
code maps gRPC status codes and HTTP status codes to the appropriate
subclass so callers can catch specific failure modes.
"""
from __future__ import annotations

import json
from typing import Any, cast


class MillError(Exception):
    """Base exception for all Mill client errors.

    Args:
        message: Human-readable error description.
        details: Optional additional context (e.g. server error payload).
        trace_id: Correlation id when provided by HTTP Problem Details or gRPC metadata.
        grpc_trailing_metadata: String values from gRPC trailing metadata (diagnostics).
    """

    def __init__(
        self,
        message: str,
        *,
        details: str | None = None,
        trace_id: str | None = None,
        grpc_trailing_metadata: dict[str, str] | None = None,
    ) -> None:
        super().__init__(message)
        self.details = details
        self.trace_id = trace_id
        self.grpc_trailing_metadata = grpc_trailing_metadata

    def as_dict(self) -> dict[str, Any]:
        """Minimal structured diagnostics shared by HTTP and gRPC failures."""
        return {
            "message": str(self),
            "details": self.details,
            "trace_id": self.trace_id,
            "grpc_trailing_metadata": self.grpc_trailing_metadata,
        }


class MillConnectionError(MillError):
    """Raised when the transport cannot reach the server.

    Covers DNS failures, connection refused, TLS handshake errors, and
    transport-level timeouts.
    """

    def __init__(
        self,
        message: str,
        *,
        details: str | None = None,
        trace_id: str | None = None,
        grpc_trailing_metadata: dict[str, str] | None = None,
    ) -> None:
        super().__init__(
            message,
            details=details,
            trace_id=trace_id,
            grpc_trailing_metadata=grpc_trailing_metadata,
        )


class MillAuthError(MillError):
    """Raised when authentication or authorisation fails.

    Maps from gRPC ``UNAUTHENTICATED`` / ``PERMISSION_DENIED`` and
    HTTP 401 / 403.
    """

    def __init__(
        self,
        message: str,
        *,
        details: str | None = None,
        trace_id: str | None = None,
        grpc_trailing_metadata: dict[str, str] | None = None,
    ) -> None:
        super().__init__(
            message,
            details=details,
            trace_id=trace_id,
            grpc_trailing_metadata=grpc_trailing_metadata,
        )


class MillQueryError(MillError):
    """Raised when the server rejects or fails a query.

    Covers SQL parse errors, execution errors, and invalid schema
    references.  Maps from gRPC ``INVALID_ARGUMENT`` / ``INTERNAL``
    and HTTP 400 / 500.

    RFC 9457 Problem Details fields (when present in JSON bodies) are stored on
    ``problem_*`` and ``trace_id`` attributes for structured diagnostics while
    :meth:`str` prioritises the best human-readable line (typically ``detail``).
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
        mill_status: str | None = None,
        mill_details: dict[str, Any] | None = None,
        problem_detail: str | None = None,
        problem_title: str | None = None,
        problem_type: str | None = None,
        trace_id: str | None = None,
        grpc_trailing_metadata: dict[str, str] | None = None,
        legacy_grpc_code: str | None = None,
        legacy_grpc_message: str | None = None,
    ) -> None:
        super().__init__(
            message,
            details=details,
            trace_id=trace_id,
            grpc_trailing_metadata=grpc_trailing_metadata,
        )
        self.status_code = status_code
        self.error = error
        self.path = path
        self.timestamp = timestamp
        self.raw_body = raw_body
        self.request_method = request_method
        self.request_url = request_url
        self.request_headers = request_headers
        self.response_headers = response_headers
        self.mill_status = mill_status
        self.mill_details = mill_details
        self.problem_detail = problem_detail
        self.problem_title = problem_title
        self.problem_type = problem_type
        self.legacy_grpc_code = legacy_grpc_code
        self.legacy_grpc_message = legacy_grpc_message

    def as_dict(self) -> dict[str, Any]:
        """Return structured diagnostics for logging/telemetry."""
        d = super().as_dict()
        d.update(
            {
                "status_code": self.status_code,
                "error": self.error,
                "path": self.path,
                "timestamp": self.timestamp,
                "raw_body": self.raw_body,
                "request_method": self.request_method,
                "request_url": self.request_url,
                "request_headers": self.request_headers,
                "response_headers": self.response_headers,
                "mill_status": self.mill_status,
                "mill_details": self.mill_details,
                "problem_detail": self.problem_detail,
                "problem_title": self.problem_title,
                "problem_type": self.problem_type,
                "legacy_grpc_code": self.legacy_grpc_code,
                "legacy_grpc_message": self.legacy_grpc_message,
            }
        )
        return d


# ---------------------------------------------------------------------------
# Error mapping helpers
# ---------------------------------------------------------------------------

_GRPC_TRACE_HEADER_KEYS = (
    "x-trace-id",
    "trace-id",
    "traceid",
    "x-request-id",
    "x-correlation-id",
)


def _meta_key_str(key: Any) -> str:
    if isinstance(key, bytes):
        return key.decode("latin-1").strip().lower()
    return str(key).strip().lower()


def _meta_val_str(val: Any) -> str:
    if isinstance(val, bytes):
        try:
            return val.decode("utf-8")
        except UnicodeDecodeError:
            return val.decode("latin-1")
    return str(val)


def _grpc_trailing_kv(rpc_error: Any) -> dict[str, str]:
    """Collect gRPC trailing metadata as lower-cased keys to string values."""
    getter = getattr(rpc_error, "trailing_metadata", None)
    if not callable(getter):
        return {}
    try:
        raw = getter()
    except Exception:
        return {}
    if not raw:
        return {}
    out: dict[str, str] = {}
    for item in raw:
        if not isinstance(item, (list, tuple)) or len(item) < 2:
            continue
        lk = _meta_key_str(item[0])
        if not lk or lk == "grpc-trace-bin":
            continue
        out[lk] = _meta_val_str(item[1])
    return out


def _grpc_trace_from_trailing(kv: dict[str, str]) -> str | None:
    for k in _GRPC_TRACE_HEADER_KEYS:
        v = kv.get(k)
        if v and v.strip():
            return v.strip()
    return None


def _grpc_display_message(details_text: str | None, code_name: str, tid: str | None) -> str:
    """Human line for :class:`str(exception)` including optional trace suffix."""
    dt = details_text.strip() if details_text and details_text.strip() else ""
    core = dt if dt else f"gRPC {code_name}"
    if tid and tid not in core:
        return f"{core} [traceId={tid}]"
    return core


def _from_grpc_error(rpc_error: Exception) -> MillError:
    """Convert a ``grpc.RpcError`` to the appropriate ``MillError`` subclass.

    Reads ``trailing_metadata()`` when present (e.g. ``x-trace-id``) for parity with
    HTTP Problem Details ``traceId``.

    Args:
        rpc_error: A gRPC exception (must expose ``.code()`` and ``.details()``).

    Returns:
        A ``MillError`` (or subclass) instance.
    """
    import grpc  # lazy — avoid import cost when not using gRPC

    meta = _grpc_trailing_kv(rpc_error)
    tid = _grpc_trace_from_trailing(meta)

    code = rpc_error.code()  # type: ignore[union-attr]
    detail = rpc_error.details()  # type: ignore[union-attr]
    detail_s = detail.strip() if isinstance(detail, str) and detail.strip() else ""
    code_name = code.name
    msg = _grpc_display_message(detail if isinstance(detail, str) else None, code_name, tid)

    legacy_message = detail_s if detail_s else None
    grpc_enum_name = code.name

    common = dict(
        trace_id=tid,
        grpc_trailing_metadata=meta or None,
    )

    if code in (grpc.StatusCode.UNAUTHENTICATED, grpc.StatusCode.PERMISSION_DENIED):
        return MillAuthError(msg, details=str(code), **common)
    if code in (grpc.StatusCode.UNAVAILABLE, grpc.StatusCode.DEADLINE_EXCEEDED):
        return MillConnectionError(msg, details=str(code), **common)
    if code in (grpc.StatusCode.INVALID_ARGUMENT, grpc.StatusCode.NOT_FOUND):
        return MillQueryError(
            msg,
            details=str(code),
            legacy_grpc_code=grpc_enum_name,
            legacy_grpc_message=legacy_message,
            **common,
        )
    return MillError(msg, details=str(code), **common)


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
    detail_line = _primary_http_message(payload, body, status_code)
    tid = payload.get("trace_id")

    query_error = MillQueryError(
        detail_line,
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
        mill_status=payload.get("mill_status"),
        mill_details=payload.get("mill_details"),
        problem_detail=payload.get("problem_detail"),
        problem_title=payload.get("problem_title"),
        problem_type=payload.get("problem_type"),
        trace_id=tid,
        legacy_grpc_code=payload.get("legacy_grpc_code"),
        legacy_grpc_message=payload.get("legacy_grpc_message"),
    )

    if status_code in (401, 403):
        return MillAuthError(
            detail_line,
            details=f"HTTP {status_code}",
            trace_id=tid,
        )
    if status_code in (400, 404, 409, 422):
        return query_error
    if status_code >= 500:
        return query_error
    return MillError(detail_line, details=f"HTTP {status_code}", trace_id=tid)


def _primary_http_message(payload: dict[str, Any], body: str, status_code: int) -> str:
    """Pick the best human-visible line from parsed JSON + raw body."""
    cand = (
        payload.get("problem_detail")
        or payload.get("message")
        or payload.get("legacy_grpc_message")
        or payload.get("error")
        or payload.get("problem_title")
    )
    if isinstance(cand, str) and cand.strip():
        return cand.strip()
    if body and body.strip():
        return body.strip()
    return f"HTTP {status_code}"


def _parse_http_body(body: str) -> dict[str, Any]:
    """Best-effort parse for JSON error bodies.

    Supports RFC 9457 Problem Details (`detail`, `title`, `type`, numeric `status`,
    `traceId`), legacy Spring Boot shapes, Mill ``mill_status`` strings, MillStatusDetails
    ``details`` objects, and GRPC-shaped bodies (`code` + ``message``) emitted by the data
    HTTP access layer during rollout.
    """
    if not body:
        return {}
    try:
        parsed = json.loads(body)
    except Exception:
        return {}
    if not isinstance(parsed, dict):
        return {}
    result: dict[str, Any] = {}

    # RFC 9457 / Spring ProblemDetail (camelCase + trace extension)
    pd = parsed.get("detail")
    if pd is not None:
        result["problem_detail"] = pd if isinstance(pd, str) else str(pd)

    ttl = parsed.get("title")
    if ttl is not None:
        result["problem_title"] = ttl if isinstance(ttl, str) else str(ttl)

    typ = parsed.get("type")
    if typ is not None:
        result["problem_type"] = typ if isinstance(typ, str) else str(typ)

    for tid_key in ("traceId", "trace_id"):
        tv = parsed.get(tid_key)
        if isinstance(tv, str) and tv.strip():
            result["trace_id"] = tv.strip()
            break
        if tv is not None and not isinstance(tv, (dict, list)):
            result["trace_id"] = str(tv)

    # Legacy Spring Boot / plain REST
    for key in ("error", "message", "path"):
        value = parsed.get(key)
        if isinstance(value, str):
            result[key] = value
        elif value is not None:
            result[key] = str(value)

    ts = parsed.get("timestamp")
    if isinstance(ts, str):
        result["timestamp"] = ts
    elif isinstance(ts, (int, float)):
        result["timestamp"] = str(int(ts))

    st = parsed.get("status")
    if isinstance(st, str):
        result["mill_status"] = st
    elif st is not None and not isinstance(st, dict):
        result["status"] = str(st)

    det = parsed.get("details")
    if isinstance(det, dict):
        result["mill_details"] = cast(dict[str, Any], det)

    props = parsed.get("properties")
    if isinstance(props, dict):
        pk = props.get("code")
        if isinstance(pk, str) and pk.strip():
            result.setdefault("legacy_grpc_code", pk.strip())
        pm = props.get("message")
        if isinstance(pm, str) and pm.strip():
            result.setdefault("legacy_grpc_message", pm.strip())
        pt = props.get("traceId")
        if isinstance(pt, str) and pt.strip():
            result.setdefault("trace_id", pt.strip())

    code = parsed.get("code")
    if isinstance(code, str) and code.strip():
        result.setdefault("legacy_grpc_code", code.strip())
        lm = parsed.get("message")
        if isinstance(lm, str) and lm.strip():
            result.setdefault("legacy_grpc_message", lm.strip())

    return result
