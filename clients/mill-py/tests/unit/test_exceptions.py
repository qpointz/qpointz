"""Tests for mill.exceptions — error hierarchy and mapping functions."""
from __future__ import annotations

from typing import Any

import grpc
import pytest

from mill.exceptions import (
    MillAuthError,
    MillConnectionError,
    MillError,
    MillQueryError,
    _from_grpc_error,
    _from_http_status,
)


# ---------------------------------------------------------------------------
# Hierarchy
# ---------------------------------------------------------------------------

class TestHierarchy:
    def test_mill_error_is_exception(self) -> None:
        assert issubclass(MillError, Exception)

    def test_connection_error_is_mill_error(self) -> None:
        assert issubclass(MillConnectionError, MillError)

    def test_auth_error_is_mill_error(self) -> None:
        assert issubclass(MillAuthError, MillError)

    def test_query_error_is_mill_error(self) -> None:
        assert issubclass(MillQueryError, MillError)

    def test_details_attribute(self) -> None:
        err = MillError("boom", details="extra")
        assert str(err) == "boom"
        assert err.details == "extra"

    def test_details_default_none(self) -> None:
        err = MillError("boom")
        assert err.details is None

    def test_mill_error_transparent_fields_defaults(self) -> None:
        err = MillError("boom")
        assert err.trace_id is None
        assert err.grpc_trailing_metadata is None

    def test_query_error_extended_fields_default(self) -> None:
        err = MillQueryError("boom")
        assert err.status_code is None
        assert err.error is None
        assert err.path is None
        assert err.timestamp is None
        assert err.raw_body is None
        assert err.request_method is None
        assert err.request_url is None
        assert err.request_headers is None
        assert err.response_headers is None
        assert err.mill_status is None
        assert err.mill_details is None
        assert err.problem_detail is None
        assert err.problem_title is None
        assert err.problem_type is None
        assert err.trace_id is None
        assert err.legacy_grpc_code is None
        assert err.legacy_grpc_message is None
        assert err.grpc_trailing_metadata is None


# ---------------------------------------------------------------------------
# gRPC error mapping
# ---------------------------------------------------------------------------

class _StubRpcError(Exception):
    """Minimal ``grpc.RpcError`` stub (avoids unittest.mock quirks on ``trailing_metadata``)."""

    def __init__(
        self,
        code: grpc.StatusCode,
        details_s: str,
        *,
        trailing_metadata: tuple[tuple[Any, Any], ...] | None = None,
    ) -> None:
        super().__init__(details_s)
        self._code = code
        self._details_s = details_s
        self._trailing = trailing_metadata

    def code(self) -> grpc.StatusCode:
        return self._code

    def details(self) -> str:
        return self._details_s

    def trailing_metadata(self) -> tuple[tuple[Any, Any], ...]:
        return self._trailing if self._trailing is not None else ()


def _stub_rpc_error(
    code: grpc.StatusCode,
    details_s: str = "oops",
    *,
    trailing_metadata: tuple[tuple[Any, Any], ...] | None = None,
) -> _StubRpcError:
    """Build a.grpc.RpcError-like object for mapper tests."""
    return _StubRpcError(code, details_s, trailing_metadata=trailing_metadata)


class TestFromGrpcError:
    @pytest.mark.parametrize("code", [
        grpc.StatusCode.UNAUTHENTICATED,
        grpc.StatusCode.PERMISSION_DENIED,
    ])
    def test_auth_codes(self, code: grpc.StatusCode) -> None:
        result = _from_grpc_error(_stub_rpc_error(code, "denied"))
        assert isinstance(result, MillAuthError)
        assert "denied" in str(result)

    @pytest.mark.parametrize("code", [
        grpc.StatusCode.UNAVAILABLE,
        grpc.StatusCode.DEADLINE_EXCEEDED,
    ])
    def test_connection_codes(self, code: grpc.StatusCode) -> None:
        result = _from_grpc_error(_stub_rpc_error(code))
        assert isinstance(result, MillConnectionError)

    @pytest.mark.parametrize("code", [
        grpc.StatusCode.INVALID_ARGUMENT,
        grpc.StatusCode.NOT_FOUND,
    ])
    def test_query_codes(self, code: grpc.StatusCode) -> None:
        result = _from_grpc_error(_stub_rpc_error(code))
        assert isinstance(result, MillQueryError)

    def test_unknown_code_returns_base(self) -> None:
        result = _from_grpc_error(_stub_rpc_error(grpc.StatusCode.INTERNAL, "internal"))
        assert isinstance(result, MillError)
        assert not isinstance(result, (MillAuthError, MillConnectionError, MillQueryError))

    def test_trailing_trace_id_query_error(self) -> None:
        err = _from_grpc_error(
            _stub_rpc_error(
                grpc.StatusCode.NOT_FOUND,
                "entity missing",
                trailing_metadata=(
                    ("x-correlation-id", b"corr-abc"),
                    (b"x-trace-id", b"tid-9"),
                ),
            )
        )
        assert isinstance(err, MillQueryError)
        assert err.trace_id == "tid-9"
        assert err.grpc_trailing_metadata is not None
        assert err.grpc_trailing_metadata.get("x-trace-id") == "tid-9"
        assert err.grpc_trailing_metadata.get("x-correlation-id") == "corr-abc"
        assert err.legacy_grpc_code == "NOT_FOUND"
        assert err.legacy_grpc_message == "entity missing"

    def test_trailing_trace_id_prefers_primary_headers(self) -> None:
        err = _from_grpc_error(
            _stub_rpc_error(
                grpc.StatusCode.UNAUTHENTICATED,
                "nope",
                trailing_metadata=(
                    ("x-correlation-id", "c"),
                    ("x-trace-id", "chosen"),
                ),
            )
        )
        assert isinstance(err, MillAuthError)
        assert err.trace_id == "chosen"


# ---------------------------------------------------------------------------
# HTTP error mapping
# ---------------------------------------------------------------------------

class TestFromHttpStatus:
    @pytest.mark.parametrize("status", [401, 403])
    def test_auth_status(self, status: int) -> None:
        result = _from_http_status(status, "forbidden")
        assert isinstance(result, MillAuthError)

    @pytest.mark.parametrize("status", [400, 404, 409, 422])
    def test_query_status(self, status: int) -> None:
        result = _from_http_status(status)
        assert isinstance(result, MillQueryError)

    @pytest.mark.parametrize("status", [500, 502, 503])
    def test_server_error_status(self, status: int) -> None:
        result = _from_http_status(status)
        assert isinstance(result, MillQueryError)

    def test_unknown_4xx_returns_base(self) -> None:
        result = _from_http_status(418)  # I'm a teapot
        assert isinstance(result, MillError)
        assert not isinstance(result, (MillAuthError, MillQueryError))

    def test_query_error_parses_json_details(self) -> None:
        body = (
            '{"timestamp":"2026-02-25T21:29:07.963+00:00",'
            '"status":404,'
            '"error":"Not Found",'
            '"message":"No static resource",'
            '"path":"/services/jet/Handshake"}'
        )
        result = _from_http_status(
            404,
            body,
            request_method="POST",
            request_url="http://mill-it:8080/services/jet/Handshake",
            request_headers={"Accept": "application/json"},
            response_headers={"content-type": "application/json"},
        )
        assert isinstance(result, MillQueryError)
        assert result.status_code == 404
        assert result.error == "Not Found"
        assert result.path == "/services/jet/Handshake"
        assert result.timestamp == "2026-02-25T21:29:07.963+00:00"
        assert result.raw_body == body
        assert result.request_method == "POST"
        assert result.request_url == "http://mill-it:8080/services/jet/Handshake"
        assert result.request_headers == {"Accept": "application/json"}
        assert result.response_headers == {"content-type": "application/json"}
        assert result.details == "HTTP 404"
        assert "No static resource" in str(result)

    def test_http_problem_details_fields(self) -> None:
        body = (
            '{"type":"urn:mill:test:nope","title":"Not available",'
            '"detail":"Unknown dialect","status":404,"traceId":"tid-xyz"}'
        )
        result = _from_http_status(404, body)
        assert isinstance(result, MillQueryError)
        assert result.trace_id == "tid-xyz"
        assert result.problem_type == "urn:mill:test:nope"
        assert result.problem_title == "Not available"
        assert result.problem_detail == "Unknown dialect"
        assert "Unknown dialect" in str(result)

    def test_query_error_keeps_raw_text_body(self) -> None:
        body = "plain server error"
        result = _from_http_status(500, body)
        assert isinstance(result, MillQueryError)
        assert result.status_code == 500
        assert result.error is None
        assert result.path is None
        assert result.timestamp is None
        assert result.raw_body == body
