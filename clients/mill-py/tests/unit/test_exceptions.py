"""Tests for mill.exceptions — error hierarchy and mapping functions."""
from __future__ import annotations

from unittest.mock import MagicMock

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


# ---------------------------------------------------------------------------
# gRPC error mapping
# ---------------------------------------------------------------------------

def _mock_rpc_error(code: grpc.StatusCode, detail: str = "oops") -> MagicMock:
    """Create a mock grpc.RpcError with .code() and .details()."""
    err = MagicMock()
    err.code.return_value = code
    err.details.return_value = detail
    return err


class TestFromGrpcError:
    @pytest.mark.parametrize("code", [
        grpc.StatusCode.UNAUTHENTICATED,
        grpc.StatusCode.PERMISSION_DENIED,
    ])
    def test_auth_codes(self, code: grpc.StatusCode) -> None:
        result = _from_grpc_error(_mock_rpc_error(code, "denied"))
        assert isinstance(result, MillAuthError)
        assert "denied" in str(result)

    @pytest.mark.parametrize("code", [
        grpc.StatusCode.UNAVAILABLE,
        grpc.StatusCode.DEADLINE_EXCEEDED,
    ])
    def test_connection_codes(self, code: grpc.StatusCode) -> None:
        result = _from_grpc_error(_mock_rpc_error(code))
        assert isinstance(result, MillConnectionError)

    @pytest.mark.parametrize("code", [
        grpc.StatusCode.INVALID_ARGUMENT,
        grpc.StatusCode.NOT_FOUND,
    ])
    def test_query_codes(self, code: grpc.StatusCode) -> None:
        result = _from_grpc_error(_mock_rpc_error(code))
        assert isinstance(result, MillQueryError)

    def test_unknown_code_returns_base(self) -> None:
        result = _from_grpc_error(_mock_rpc_error(grpc.StatusCode.INTERNAL, "internal"))
        assert isinstance(result, MillError)
        assert not isinstance(result, (MillAuthError, MillConnectionError, MillQueryError))


# ---------------------------------------------------------------------------
# HTTP error mapping
# ---------------------------------------------------------------------------

class TestFromHttpStatus:
    @pytest.mark.parametrize("status", [401, 403])
    def test_auth_status(self, status: int) -> None:
        result = _from_http_status(status, "forbidden")
        assert isinstance(result, MillAuthError)

    @pytest.mark.parametrize("status", [400, 404, 422])
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

    def test_query_error_keeps_raw_text_body(self) -> None:
        body = "plain server error"
        result = _from_http_status(500, body)
        assert isinstance(result, MillQueryError)
        assert result.status_code == 500
        assert result.error is None
        assert result.path is None
        assert result.timestamp is None
        assert result.raw_body == body
