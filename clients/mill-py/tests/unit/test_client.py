"""Tests for mill.client — MillClient and connect() factory."""
from __future__ import annotations

from unittest.mock import MagicMock, patch

import pytest

from mill import connect, MillClient
from mill._proto import common_pb2 as cpb
from mill._proto import data_connect_svc_pb2 as svc
from mill._transport import Transport


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _mock_transport() -> MagicMock:
    """Create a mock Transport with sensible defaults."""
    t = MagicMock(spec=Transport)
    t.handshake.return_value = svc.HandshakeResponse(version=1)
    t.list_schemas.return_value = svc.ListSchemasResponse(schemas=["S1", "S2"])
    t.get_schema.return_value = svc.GetSchemaResponse(
        schema=cpb.Schema(tables=[
            cpb.Table(schemaName="S1", name="T1", tableType=cpb.Table.TABLE),
        ]),
    )
    t.parse_sql.return_value = svc.ParseSqlResponse()
    t.exec_query.return_value = iter([])
    return t


# ---------------------------------------------------------------------------
# MillClient
# ---------------------------------------------------------------------------

class TestMillClient:
    def test_list_schemas(self) -> None:
        t = _mock_transport()
        c = MillClient(t)
        result = c.list_schemas()
        assert result == ["S1", "S2"]
        t.list_schemas.assert_called_once()

    def test_get_schema(self) -> None:
        t = _mock_transport()
        c = MillClient(t)
        schema = c.get_schema("S1")
        assert len(schema.tables) == 1
        assert schema.tables[0].name == "T1"
        t.get_schema.assert_called_once_with("S1")

    def test_handshake(self) -> None:
        t = _mock_transport()
        c = MillClient(t)
        resp = c.handshake()
        assert resp.version == 1

    def test_parse_sql(self) -> None:
        t = _mock_transport()
        c = MillClient(t)
        resp = c.parse_sql("SELECT 1")
        t.parse_sql.assert_called_once_with("SELECT 1")

    def test_query_returns_result_set(self) -> None:
        t = _mock_transport()
        c = MillClient(t)
        rs = c.query("SELECT 1")
        from mill.result import ResultSet
        assert isinstance(rs, ResultSet)
        t.exec_query.assert_called_once()

    def test_close_delegates(self) -> None:
        t = _mock_transport()
        c = MillClient(t)
        c.close()
        t.close.assert_called_once()

    def test_context_manager(self) -> None:
        t = _mock_transport()
        with MillClient(t) as c:
            c.list_schemas()
        t.close.assert_called_once()

    def test_repr(self) -> None:
        t = _mock_transport()
        c = MillClient(t)
        assert "MillClient" in repr(c)


# ---------------------------------------------------------------------------
# connect() factory — URL parsing
# ---------------------------------------------------------------------------

class TestConnect:
    @patch("mill._transport._grpc.grpc.insecure_channel")
    @patch("mill._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_grpc_url(self, _stub: MagicMock, mock_chan: MagicMock) -> None:
        c = connect("grpc://myhost:9099")
        assert isinstance(c, MillClient)
        mock_chan.assert_called_once_with("myhost:9099")
        c.close()

    @patch("mill._transport._grpc.grpc.secure_channel")
    @patch("mill._transport._grpc.grpc.ssl_channel_credentials")
    @patch("mill._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_grpcs_url(self, _stub: MagicMock, _creds: MagicMock, mock_chan: MagicMock) -> None:
        c = connect("grpcs://host:443")
        assert isinstance(c, MillClient)
        c.close()

    def test_http_url(self) -> None:
        c = connect("http://localhost:8501/services/jet")
        assert isinstance(c, MillClient)
        c.close()

    def test_https_url(self) -> None:
        c = connect("https://host:443/api")
        assert isinstance(c, MillClient)
        c.close()

    def test_http_default_base_path(self) -> None:
        c = connect("http://host:80")
        # Should use /services/jet as default
        from mill._transport._http import HttpTransport
        assert isinstance(c._transport, HttpTransport)
        c.close()

    def test_encoding_passed_to_http(self) -> None:
        c = connect("http://host:80", encoding="protobuf")
        assert c._transport._content_type == "application/x-protobuf"  # type: ignore[union-attr]
        c.close()

    def test_host_override(self) -> None:
        c = connect("http://ignored:80", host="actual")
        assert "actual" in c._transport._base_url  # type: ignore[union-attr]
        c.close()

    def test_unsupported_scheme_raises(self) -> None:
        with pytest.raises(ValueError, match="Unsupported URL scheme"):
            connect("ftp://host:21")

    @patch("mill.discovery.fetch_descriptor")
    def test_bare_hostname_triggers_discovery(self, mock_fetch: MagicMock) -> None:
        mock_fetch.return_value = MagicMock()
        with pytest.raises(NotImplementedError, match="discovery mode is not implemented"):
            connect("myhost")

    @patch("mill._transport._grpc.grpc.insecure_channel")
    @patch("mill._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_grpc_default_port(self, _stub: MagicMock, mock_chan: MagicMock) -> None:
        c = connect("grpc://host")
        mock_chan.assert_called_once_with("host:9099")
        c.close()

    def test_http_default_port(self) -> None:
        c = connect("http://host")
        assert ":80" in c._transport._base_url  # type: ignore[union-attr]
        c.close()

    @patch("mill._transport._grpc.grpc.insecure_channel")
    @patch("mill._transport._grpc._grpc_stub.DataConnectServiceStub")
    def test_auth_passed_to_grpc(self, _stub: MagicMock, _chan: MagicMock) -> None:
        from mill.auth import BasicAuth
        c = connect("grpc://host:9099", auth=BasicAuth("u", "p"))
        meta = c._transport._meta()  # type: ignore[union-attr]
        assert meta is not None
        c.close()

    def test_auth_passed_to_http(self) -> None:
        from mill.auth import BearerToken
        c = connect("http://host:80", auth=BearerToken("tok"))
        assert "Authorization" in c._transport._client.headers  # type: ignore[union-attr]
        c.close()
