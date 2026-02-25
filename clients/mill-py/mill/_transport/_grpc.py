"""gRPC transport implementation.

Uses ``grpc.insecure_channel`` / ``grpc.secure_channel`` plus the
generated ``DataConnectServiceStub`` for blocking (sync) calls.
"""
from __future__ import annotations

from typing import Iterator, Sequence

import grpc

from mill._proto import data_connect_svc_pb2 as _svc
from mill._proto import data_connect_svc_pb2_grpc as _grpc_stub
from mill._proto import statement_pb2 as _stmt
from mill._transport import Transport
from mill.auth import Credential, _auth_headers
from mill.exceptions import _from_grpc_error

from pathlib import Path


def _read_pem(value: str | bytes | None) -> bytes | None:
    """Read PEM data from a file path or pass raw bytes through.

    Args:
        value: A filesystem path (``str``), raw PEM ``bytes``, or ``None``.

    Returns:
        PEM bytes, or ``None`` if *value* is ``None``.
    """
    if value is None:
        return None
    if isinstance(value, bytes):
        return value
    # Treat as a file path
    return Path(value).read_bytes()


class GrpcTransport(Transport):
    """Synchronous gRPC transport.

    Args:
        host: Server hostname.
        port: Server port.
        ssl: If ``True``, use a secure channel with default credentials.
            Alternatively pass ``grpc.ChannelCredentials`` for custom TLS.
        auth: Optional credential (``BasicAuth``, ``BearerToken``, or ``None``).
        tls_ca: Path to a PEM-encoded CA certificate file, or the raw PEM
            bytes.  Used to verify the server certificate.  Only meaningful
            when *ssl* is ``True``.
        tls_cert: Path to a PEM-encoded client certificate file, or raw PEM
            bytes (for mutual-TLS).
        tls_key: Path to a PEM-encoded private-key file, or raw PEM bytes
            (for mutual-TLS).

    Example::

        >>> t = GrpcTransport("localhost", 9090)
        >>> resp = t.handshake()
        >>> t.close()
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
            self._channel = grpc.secure_channel(self._target, creds)
        elif ssl is False:
            self._channel = grpc.insecure_channel(self._target)
        else:
            # Caller provided custom ChannelCredentials
            self._channel = grpc.secure_channel(self._target, ssl)

        self._stub = _grpc_stub.DataConnectServiceStub(self._channel)

    # -- helpers --

    def _meta(self) -> Sequence[tuple[str, str]] | None:
        """Return call metadata, or ``None`` if empty (anonymous)."""
        return self._metadata or None

    def _call(self, method_name: str, request: object) -> object:
        """Invoke a unary-unary RPC, mapping errors.

        Args:
            method_name: Name of the stub method (e.g. ``"Handshake"``).
            request: The proto request message.

        Returns:
            The proto response message.

        Raises:
            MillError: On gRPC failure.
        """
        try:
            method = getattr(self._stub, method_name)
            return method(request, metadata=self._meta())
        except grpc.RpcError as e:
            raise _from_grpc_error(e) from e

    # -- Transport interface --

    def handshake(self) -> _svc.HandshakeResponse:
        """Perform protocol handshake via gRPC."""
        return self._call("Handshake", _svc.HandshakeRequest())  # type: ignore[return-value]

    def list_schemas(self) -> _svc.ListSchemasResponse:
        """List schemas via gRPC."""
        return self._call("ListSchemas", _svc.ListSchemasRequest())  # type: ignore[return-value]

    def get_schema(self, name: str) -> _svc.GetSchemaResponse:
        """Retrieve schema definition by name via gRPC."""
        req = _svc.GetSchemaRequest(schemaName=name)
        return self._call("GetSchema", req)  # type: ignore[return-value]

    def parse_sql(self, sql: str) -> _svc.ParseSqlResponse:
        """Parse SQL into a Substrait plan via gRPC."""
        req = _svc.ParseSqlRequest(
            statement=_stmt.SQLStatement(sql=sql),
        )
        return self._call("ParseSql", req)  # type: ignore[return-value]

    def exec_query(self, request: _svc.QueryRequest) -> Iterator[_svc.QueryResultResponse]:
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
            yield from stream
        except grpc.RpcError as e:
            raise _from_grpc_error(e) from e

    def close(self) -> None:
        """Close the gRPC channel."""
        self._channel.close()
