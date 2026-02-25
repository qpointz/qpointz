"""Tests for mill.aio._transport — async ABC and transport construction.

These tests verify that the async transport classes mirror their sync
counterparts: ABC compliance, channel/client construction, header injection,
and encoding selection.
"""
from __future__ import annotations

from unittest.mock import AsyncMock, MagicMock, patch

import pytest

from mill.aio._transport import AsyncTransport
from mill.aio._transport._grpc import AsyncGrpcTransport
from mill.aio._transport._http import AsyncHttpTransport
from mill.auth import BasicAuth, BearerToken


pytestmark = pytest.mark.unit


# ---------------------------------------------------------------------------
# AsyncTransport ABC
# ---------------------------------------------------------------------------

class TestAsyncTransportABC:
    def test_cannot_instantiate(self) -> None:
        with pytest.raises(TypeError):
            AsyncTransport()  # type: ignore[abstract]

    def test_concrete_must_implement_all(self) -> None:
        """A subclass missing any method should not be instantiable."""

        class Incomplete(AsyncTransport):
            async def handshake(self):  # type: ignore[override]
                ...

        with pytest.raises(TypeError):
            Incomplete()  # type: ignore[abstract]


# ---------------------------------------------------------------------------
# AsyncGrpcTransport construction
# ---------------------------------------------------------------------------

class TestAsyncGrpcTransportConstruction:
    @patch("mill.aio._transport._grpc.grpc.aio.insecure_channel")
    @patch("mill.aio._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_insecure_channel(self, mock_stub: MagicMock, mock_channel: MagicMock) -> None:
        t = AsyncGrpcTransport("localhost", 9090)
        mock_channel.assert_called_once_with("localhost:9090")
        mock_stub.assert_called_once()

    @patch("mill.aio._transport._grpc.grpc.aio.secure_channel")
    @patch("mill.aio._transport._grpc.grpc.ssl_channel_credentials")
    @patch("mill.aio._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_secure_channel(
        self, mock_stub: MagicMock, mock_creds: MagicMock, mock_channel: MagicMock,
    ) -> None:
        t = AsyncGrpcTransport("host", 443, ssl=True)
        mock_creds.assert_called_once()
        mock_channel.assert_called_once()

    @patch("mill.aio._transport._grpc.grpc.aio.insecure_channel")
    @patch("mill.aio._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_metadata_from_basic_auth(self, _stub: MagicMock, _chan: MagicMock) -> None:
        t = AsyncGrpcTransport("host", 9090, auth=BasicAuth("u", "p"))
        meta = t._meta()
        assert meta is not None
        assert any(k == "authorization" and v.startswith("Basic ") for k, v in meta)

    @patch("mill.aio._transport._grpc.grpc.aio.insecure_channel")
    @patch("mill.aio._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_metadata_from_bearer_token(self, _stub: MagicMock, _chan: MagicMock) -> None:
        t = AsyncGrpcTransport("host", 9090, auth=BearerToken("tok"))
        meta = t._meta()
        assert meta is not None
        assert any(k == "authorization" and v == "Bearer tok" for k, v in meta)

    @patch("mill.aio._transport._grpc.grpc.aio.insecure_channel")
    @patch("mill.aio._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_metadata_anonymous(self, _stub: MagicMock, _chan: MagicMock) -> None:
        t = AsyncGrpcTransport("host", 9090)
        assert t._meta() is None


# ---------------------------------------------------------------------------
# AsyncHttpTransport construction
# ---------------------------------------------------------------------------

class TestAsyncHttpTransportConstruction:
    def test_base_url_http(self) -> None:
        t = AsyncHttpTransport("localhost", 8501)
        assert t._base_url == "http://localhost:8501/services/jet"

    def test_base_url_https(self) -> None:
        t = AsyncHttpTransport("host", 443, ssl=True)
        assert t._base_url == "https://host:443/services/jet"

    def test_custom_base_path(self) -> None:
        t = AsyncHttpTransport("h", 80, base_path="/api/v2/")
        assert t._base_url == "http://h:80/api/v2"

    def test_encoding_json_default(self) -> None:
        t = AsyncHttpTransport("h", 80)
        assert t._content_type == "application/json"

    def test_encoding_protobuf(self) -> None:
        t = AsyncHttpTransport("h", 80, encoding="protobuf")
        assert t._content_type == "application/x-protobuf"

    def test_auth_header_capitalised(self) -> None:
        t = AsyncHttpTransport("h", 80, auth=BearerToken("tok"))
        assert "Authorization" in t._client.headers
        assert t._client.headers["Authorization"] == "Bearer tok"

    def test_anonymous_no_auth_header(self) -> None:
        t = AsyncHttpTransport("h", 80)
        assert "Authorization" not in t._client.headers


# ---------------------------------------------------------------------------
# AsyncHttpTransport serialisation
# ---------------------------------------------------------------------------

class TestAsyncHttpTransportSerialisation:
    def test_json_serialise_roundtrip(self) -> None:
        from mill._proto import data_connect_svc_pb2 as svc

        t = AsyncHttpTransport("h", 80, encoding="json")
        req = svc.GetSchemaRequest(schemaName="TEST")
        payload = t._serialize(req)
        assert isinstance(payload, str)
        assert "TEST" in payload
        result = t._deserialize(payload.encode(), svc.GetSchemaRequest)
        assert result.schemaName == "TEST"

    def test_protobuf_serialise_roundtrip(self) -> None:
        from mill._proto import data_connect_svc_pb2 as svc

        t = AsyncHttpTransport("h", 80, encoding="protobuf")
        req = svc.GetSchemaRequest(schemaName="PB")
        payload = t._serialize(req)
        assert isinstance(payload, bytes)
        result = t._deserialize(payload, svc.GetSchemaRequest)
        assert result.schemaName == "PB"
