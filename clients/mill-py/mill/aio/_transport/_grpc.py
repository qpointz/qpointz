"""Async gRPC transport implementation.

Uses ``grpc.aio.insecure_channel`` / ``grpc.aio.secure_channel`` plus the
generated ``DataConnectServiceStub`` for non-blocking (async) calls.
"""
from __future__ import annotations

from typing import AsyncIterator, Sequence

import grpc
import grpc.aio

from mill._proto import data_connect_svc_pb2 as _svc
from mill._proto import data_connect_svc_pb2_grpc as _grpc_stub
from mill._proto import statement_pb2 as _stmt
from mill._transport._grpc import _read_pem
from mill.aio._transport import AsyncTransport
from mill.auth import Credential, _auth_headers
from mill.exceptions import _from_grpc_error


class AsyncGrpcTransport(AsyncTransport):
    """Asynchronous gRPC transport.

    Args:
        host: Server hostname.
        port: Server port.
        ssl: If ``True``, use a secure channel with default credentials.
            Alternatively pass ``grpc.ChannelCredentials`` for custom TLS.
        auth: Optional credential (``BasicAuth``, ``BearerToken``, or ``None``).
        tls_ca: Path to a PEM-encoded CA certificate file, or raw PEM bytes.
        tls_cert: Path to a PEM-encoded client certificate file, or raw PEM bytes.
        tls_key: Path to a PEM-encoded private-key file, or raw PEM bytes.

    Example::

        >>> t = AsyncGrpcTransport("localhost", 9099)
        >>> resp = await t.handshake()
        >>> await t.close()
    """

    def __init__(
        self,
        host: str,
        port: int,
        *,
        ssl: bool | grpc.ChannelCredentials = False,
        auth: Credential = None,
        tls_ca: str | bytes | None = None,
        tls_cert: str | bytes | None = None,
        tls_key: str | bytes | None = None,
    ) -> None:
        self._target = f"{host}:{port}"
        self._metadata: list[tuple[str, str]] = list(_auth_headers(auth).items())

        if ssl is True:
            creds = grpc.ssl_channel_credentials(
                root_certificates=_read_pem(tls_ca),
                private_key=_read_pem(tls_key),
                certificate_chain=_read_pem(tls_cert),
            )
            self._channel = grpc.aio.secure_channel(self._target, creds)
        elif ssl is False:
            self._channel = grpc.aio.insecure_channel(self._target)
        else:
            self._channel = grpc.aio.secure_channel(self._target, ssl)

        self._stub = _grpc_stub.DataConnectServiceStub(self._channel)

    # -- helpers --

    def _meta(self) -> Sequence[tuple[str, str]] | None:
        """Return call metadata, or ``None`` if empty (anonymous)."""
        return self._metadata or None

    async def _call(self, method_name: str, request: object) -> object:
        """Invoke a unary-unary RPC asynchronously, mapping errors."""
        try:
            method = getattr(self._stub, method_name)
            return await method(request, metadata=self._meta())
        except grpc.RpcError as e:
            raise _from_grpc_error(e) from e

    # -- AsyncTransport interface --

    async def handshake(self) -> _svc.HandshakeResponse:
        """Perform protocol handshake via async gRPC."""
        return await self._call("Handshake", _svc.HandshakeRequest())  # type: ignore[return-value]

    async def list_schemas(self) -> _svc.ListSchemasResponse:
        """List schemas via async gRPC."""
        return await self._call("ListSchemas", _svc.ListSchemasRequest())  # type: ignore[return-value]

    async def get_schema(self, name: str) -> _svc.GetSchemaResponse:
        """Retrieve schema definition by name via async gRPC."""
        req = _svc.GetSchemaRequest(schemaName=name)
        return await self._call("GetSchema", req)  # type: ignore[return-value]

    async def parse_sql(self, sql: str) -> _svc.ParseSqlResponse:
        """Parse SQL into a Substrait plan via async gRPC."""
        req = _svc.ParseSqlRequest(
            statement=_stmt.SQLStatement(sql=sql),
        )
        return await self._call("ParseSql", req)  # type: ignore[return-value]

    async def exec_query(self, request: _svc.QueryRequest) -> AsyncIterator[_svc.QueryResultResponse]:
        """Execute via server-streaming ``ExecQuery`` RPC.

        Args:
            request: A ``QueryRequest`` proto.

        Yields:
            ``QueryResultResponse`` messages from the stream.

        Raises:
            MillError: On gRPC failure.
        """
        try:
            stream = self._stub.ExecQuery(request, metadata=self._meta())
            async for resp in stream:
                yield resp
        except grpc.RpcError as e:
            raise _from_grpc_error(e) from e

    async def close(self) -> None:
        """Close the async gRPC channel."""
        await self._channel.close()
