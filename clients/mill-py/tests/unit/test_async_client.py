"""Tests for mill.aio.client — AsyncMillClient and async connect() factory."""
from __future__ import annotations

from unittest.mock import AsyncMock, MagicMock, patch

import pytest

from mill._proto import common_pb2 as cpb
from mill._proto import data_connect_svc_pb2 as svc
from mill.aio import connect, AsyncMillClient
from mill.aio._transport import AsyncTransport


pytestmark = pytest.mark.unit


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _mock_async_transport() -> MagicMock:
    """Create a mock AsyncTransport with sensible defaults."""
    t = MagicMock(spec=AsyncTransport)
    t.handshake = AsyncMock(return_value=svc.HandshakeResponse(version=1))
    t.list_schemas = AsyncMock(
        return_value=svc.ListSchemasResponse(schemas=["S1", "S2"]),
    )
    t.get_schema = AsyncMock(
        return_value=svc.GetSchemaResponse(
            schema=cpb.Schema(tables=[
                cpb.Table(schemaName="S1", name="T1", tableType=cpb.Table.TABLE),
            ]),
        ),
    )
    t.parse_sql = AsyncMock(return_value=svc.ParseSqlResponse())

    async def _empty_exec_query(request):
        return
        yield  # noqa: make this an async generator

    t.exec_query = _empty_exec_query
    t.close = AsyncMock()
    return t


# ---------------------------------------------------------------------------
# AsyncMillClient
# ---------------------------------------------------------------------------

class TestAsyncMillClient:
    async def test_list_schemas(self) -> None:
        t = _mock_async_transport()
        c = AsyncMillClient(t)
        result = await c.list_schemas()
        assert result == ["S1", "S2"]
        t.list_schemas.assert_awaited_once()

    async def test_get_schema(self) -> None:
        t = _mock_async_transport()
        c = AsyncMillClient(t)
        schema = await c.get_schema("S1")
        assert len(schema.tables) == 1
        assert schema.tables[0].name == "T1"
        t.get_schema.assert_awaited_once_with("S1")

    async def test_handshake(self) -> None:
        t = _mock_async_transport()
        c = AsyncMillClient(t)
        resp = await c.handshake()
        assert resp.version == 1

    async def test_parse_sql(self) -> None:
        t = _mock_async_transport()
        c = AsyncMillClient(t)
        await c.parse_sql("SELECT 1")
        t.parse_sql.assert_awaited_once_with("SELECT 1")

    async def test_query_returns_async_result_set(self) -> None:
        t = _mock_async_transport()
        c = AsyncMillClient(t)
        rs = await c.query("SELECT 1")
        from mill.aio.result import AsyncResultSet
        assert isinstance(rs, AsyncResultSet)

    async def test_close_delegates(self) -> None:
        t = _mock_async_transport()
        c = AsyncMillClient(t)
        await c.close()
        t.close.assert_awaited_once()

    async def test_async_context_manager(self) -> None:
        t = _mock_async_transport()
        async with AsyncMillClient(t) as c:
            await c.list_schemas()
        t.close.assert_awaited_once()

    def test_repr(self) -> None:
        t = _mock_async_transport()
        c = AsyncMillClient(t)
        assert "AsyncMillClient" in repr(c)


# ---------------------------------------------------------------------------
# async connect() factory — URL parsing
# ---------------------------------------------------------------------------

class TestAsyncConnect:
    @patch("mill.aio._transport._grpc.grpc.aio.insecure_channel")
    @patch("mill.aio._transport._grpc._grpc_stub.DataConnectServiceStub")
    async def test_grpc_url(self, _stub: MagicMock, mock_chan: MagicMock) -> None:
        # Make the channel mock's close() awaitable
        mock_chan.return_value.close = AsyncMock()
        c = await connect("grpc://myhost:9090")
        assert isinstance(c, AsyncMillClient)
        mock_chan.assert_called_once_with("myhost:9090")
        await c.close()

    @patch("mill.aio._transport._grpc.grpc.aio.secure_channel")
    @patch("mill.aio._transport._grpc.grpc.ssl_channel_credentials")
    @patch("mill.aio._transport._grpc._grpc_stub.DataConnectServiceStub")
    async def test_grpcs_url(self, _stub: MagicMock, _creds: MagicMock, mock_chan: MagicMock) -> None:
        mock_chan.return_value.close = AsyncMock()
        c = await connect("grpcs://host:443")
        assert isinstance(c, AsyncMillClient)
        await c.close()

    async def test_http_url(self) -> None:
        c = await connect("http://localhost:8501/services/jet")
        assert isinstance(c, AsyncMillClient)
        await c.close()

    async def test_https_url(self) -> None:
        c = await connect("https://host:443/api")
        assert isinstance(c, AsyncMillClient)
        await c.close()

    async def test_http_default_base_path(self) -> None:
        c = await connect("http://host:80")
        from mill.aio._transport._http import AsyncHttpTransport
        assert isinstance(c._transport, AsyncHttpTransport)
        await c.close()

    async def test_encoding_passed_to_http(self) -> None:
        c = await connect("http://host:80", encoding="protobuf")
        assert c._transport._content_type == "application/x-protobuf"  # type: ignore[union-attr]
        await c.close()

    async def test_host_override(self) -> None:
        c = await connect("http://ignored:80", host="actual")
        assert "actual" in c._transport._base_url  # type: ignore[union-attr]
        await c.close()

    async def test_unsupported_scheme_raises(self) -> None:
        with pytest.raises(ValueError, match="Unsupported URL scheme"):
            await connect("ftp://host:21")

    async def test_bare_hostname_triggers_discovery(self) -> None:
        with pytest.raises(NotImplementedError, match="discovery mode is not implemented"):
            await connect("myhost")

    @patch("mill.aio._transport._grpc.grpc.aio.insecure_channel")
    @patch("mill.aio._transport._grpc._grpc_stub.DataConnectServiceStub")
    async def test_grpc_default_port(self, _stub: MagicMock, mock_chan: MagicMock) -> None:
        mock_chan.return_value.close = AsyncMock()
        c = await connect("grpc://host")
        mock_chan.assert_called_once_with("host:9090")
        await c.close()

    async def test_http_default_port(self) -> None:
        c = await connect("http://host")
        assert ":80" in c._transport._base_url  # type: ignore[union-attr]
        await c.close()

    @patch("mill.aio._transport._grpc.grpc.aio.insecure_channel")
    @patch("mill.aio._transport._grpc._grpc_stub.DataConnectServiceStub")
    async def test_auth_passed_to_grpc(self, _stub: MagicMock, _chan: MagicMock) -> None:
        _chan.return_value.close = AsyncMock()
        from mill.auth import BasicAuth
        c = await connect("grpc://host:9090", auth=BasicAuth("u", "p"))
        meta = c._transport._meta()  # type: ignore[union-attr]
        assert meta is not None
        await c.close()

    async def test_auth_passed_to_http(self) -> None:
        from mill.auth import BearerToken
        c = await connect("http://host:80", auth=BearerToken("tok"))
        assert "Authorization" in c._transport._client.headers  # type: ignore[union-attr]
        await c.close()
