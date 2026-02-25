"""Tests for mill._transport — ABC compliance and transport construction.

These tests verify URL construction, header injection, and encoding
selection without requiring a live server.  gRPC channel creation is
mocked; HTTP transport uses httpx's mock transport.
"""
from __future__ import annotations

from unittest.mock import MagicMock, patch

import httpx
import pytest

from mill._transport import Transport
from mill._transport._grpc import GrpcTransport
from mill._transport._http import HttpTransport
from mill.auth import BasicAuth, BearerToken


# ---------------------------------------------------------------------------
# Transport ABC
# ---------------------------------------------------------------------------

class TestTransportABC:
    def test_cannot_instantiate(self) -> None:
        with pytest.raises(TypeError):
            Transport()  # type: ignore[abstract]

    def test_concrete_must_implement_all(self) -> None:
        """A subclass missing any method should not be instantiable."""

        class Incomplete(Transport):
            def handshake(self):  # type: ignore[override]
                ...

        with pytest.raises(TypeError):
            Incomplete()  # type: ignore[abstract]


# ---------------------------------------------------------------------------
# GrpcTransport construction
# ---------------------------------------------------------------------------

class TestGrpcTransportConstruction:
    @patch("mill._transport._grpc.grpc.insecure_channel")
    @patch("mill._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_insecure_channel(self, mock_stub: MagicMock, mock_channel: MagicMock) -> None:
        t = GrpcTransport("localhost", 9090)
        mock_channel.assert_called_once_with("localhost:9090")
        mock_stub.assert_called_once()
        t.close()

    @patch("mill._transport._grpc.grpc.secure_channel")
    @patch("mill._transport._grpc.grpc.ssl_channel_credentials")
    @patch("mill._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_secure_channel(
        self, mock_stub: MagicMock, mock_creds: MagicMock, mock_channel: MagicMock,
    ) -> None:
        t = GrpcTransport("host", 443, ssl=True)
        mock_creds.assert_called_once()
        mock_channel.assert_called_once()
        t.close()

    @patch("mill._transport._grpc.grpc.insecure_channel")
    @patch("mill._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_metadata_from_basic_auth(self, _stub: MagicMock, _chan: MagicMock) -> None:
        t = GrpcTransport("host", 9090, auth=BasicAuth("u", "p"))
        meta = t._meta()
        assert meta is not None
        assert any(k == "authorization" and v.startswith("Basic ") for k, v in meta)
        t.close()

    @patch("mill._transport._grpc.grpc.insecure_channel")
    @patch("mill._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_metadata_anonymous(self, _stub: MagicMock, _chan: MagicMock) -> None:
        t = GrpcTransport("host", 9090)
        assert t._meta() is None
        t.close()


# ---------------------------------------------------------------------------
# HttpTransport construction
# ---------------------------------------------------------------------------

class TestHttpTransportConstruction:
    def test_base_url_http(self) -> None:
        t = HttpTransport("localhost", 8501)
        assert t._base_url == "http://localhost:8501/services/jet"
        t.close()

    def test_base_url_https(self) -> None:
        t = HttpTransport("host", 443, ssl=True)
        assert t._base_url == "https://host:443/services/jet"
        t.close()

    def test_custom_base_path(self) -> None:
        t = HttpTransport("h", 80, base_path="/api/v2/")
        assert t._base_url == "http://h:80/api/v2"
        t.close()

    def test_encoding_json_default(self) -> None:
        t = HttpTransport("h", 80)
        assert t._content_type == "application/json"
        t.close()

    def test_encoding_protobuf(self) -> None:
        t = HttpTransport("h", 80, encoding="protobuf")
        assert t._content_type == "application/x-protobuf"
        t.close()

    def test_auth_header_capitalised(self) -> None:
        t = HttpTransport("h", 80, auth=BearerToken("tok"))
        # httpx merges headers; check the client's headers
        assert "Authorization" in t._client.headers
        assert t._client.headers["Authorization"] == "Bearer tok"
        t.close()

    def test_anonymous_no_auth_header(self) -> None:
        t = HttpTransport("h", 80)
        assert "Authorization" not in t._client.headers
        t.close()


# ---------------------------------------------------------------------------
# HttpTransport serialisation
# ---------------------------------------------------------------------------

class TestHttpTransportSerialisation:
    def test_json_serialise_roundtrip(self) -> None:
        from mill._proto import data_connect_svc_pb2 as svc
        t = HttpTransport("h", 80, encoding="json")
        req = svc.GetSchemaRequest(schemaName="TEST")
        payload = t._serialize(req)
        assert isinstance(payload, str)
        assert "TEST" in payload
        # Deserialise
        result = t._deserialize(payload.encode(), svc.GetSchemaRequest)
        assert result.schemaName == "TEST"
        t.close()

    def test_protobuf_serialise_roundtrip(self) -> None:
        from mill._proto import data_connect_svc_pb2 as svc
        t = HttpTransport("h", 80, encoding="protobuf")
        req = svc.GetSchemaRequest(schemaName="PB")
        payload = t._serialize(req)
        assert isinstance(payload, bytes)
        result = t._deserialize(payload, svc.GetSchemaRequest)
        assert result.schemaName == "PB"
        t.close()
