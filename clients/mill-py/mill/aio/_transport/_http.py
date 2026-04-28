"""Async HTTP transport implementation.

Uses ``httpx.AsyncClient`` with both JSON and binary-protobuf encoding
against the Mill HTTP service (base path ``/services/jet`` by default).
Query execution uses the paging pattern: ``SubmitQuery`` → ``FetchQueryResult`` loop.
"""
from __future__ import annotations

from typing import Any, AsyncIterator

import httpx
from google.protobuf import json_format, message as _pb_message

from mill._proto import data_connect_svc_pb2 as _svc
from mill._proto import dialect_pb2 as _dialect
from mill._proto import statement_pb2 as _stmt
from mill.aio._transport import AsyncTransport
from mill._http_common import (
    connection_error_from_transport,
    jet_base_url,
    jet_client_headers,
    sanitize_headers_for_log,
    tls_verify_and_cert,
)
from mill.auth import Credential
from mill.exceptions import _from_http_status

# Content types
_CT_JSON = "application/json"
_CT_PROTO = "application/x-protobuf"


class AsyncHttpTransport(AsyncTransport):
    """Asynchronous HTTP transport using ``httpx.AsyncClient``.

    Args:
        host: Server hostname.
        port: Server port.
        ssl: If ``True`` use ``https``; if ``False`` use ``http``.
        base_path: URL path prefix (default ``/services/jet``).
        encoding: ``"json"`` (default) or ``"protobuf"`` — controls both
            the ``Content-Type`` and ``Accept`` headers.
        auth: Optional credential (``BasicAuth``, ``BearerToken``, or ``None``).
        tls_ca: Path to a PEM-encoded CA bundle for server certificate
            verification.  ``None`` uses the system default.  Set to
            ``False`` to disable verification (not recommended).
        tls_cert: Path to a PEM client certificate file (for mutual-TLS).
        tls_key: Path to a PEM private-key file (for mutual-TLS).

    Example::

        >>> t = AsyncHttpTransport("localhost", 8501, encoding="json")
        >>> resp = await t.handshake()
        >>> await t.close()
    """

    def __init__(
        self,
        host: str,
        port: int,
        *,
        ssl: bool = False,
        base_path: str = "/services/jet",
        encoding: str = "json",
        auth: Credential = None,
        tls_ca: str | bool | None = None,
        tls_cert: str | None = None,
        tls_key: str | None = None,
    ) -> None:
        self._base_url = jet_base_url(host, port, ssl=ssl, base_path=base_path)
        self._encoding = encoding.lower()

        if self._encoding == "protobuf":
            self._content_type = _CT_PROTO
        else:
            self._content_type = _CT_JSON

        http_headers = jet_client_headers(auth, self._content_type)
        verify, cert = tls_verify_and_cert(tls_ca, tls_cert, tls_key)

        self._client = httpx.AsyncClient(
            base_url=self._base_url,
            headers=http_headers,
            timeout=30.0,
            verify=verify,
            cert=cert,
        )

    # -- serialisation helpers --

    def _serialize(self, msg: _pb_message.Message) -> bytes | str:
        """Serialise a proto message according to the encoding."""
        if self._encoding == "protobuf":
            return msg.SerializeToString()
        return json_format.MessageToJson(msg, preserving_proto_field_name=True)

    def _deserialize(self, data: bytes, msg_type: type[_pb_message.Message]) -> Any:
        """Deserialise a response body into a proto message."""
        msg = msg_type()
        if self._encoding == "protobuf":
            msg.ParseFromString(data)
        else:
            json_format.Parse(data.decode("utf-8"), msg)
        return msg

    # -- request helper --

    async def _post(
        self,
        path: str,
        request_msg: _pb_message.Message,
        response_type: type[_pb_message.Message],
    ) -> Any:
        """POST a proto request asynchronously, deserialise the response.

        Args:
            path: Endpoint path (e.g. ``"/Handshake"``).
            request_msg: The proto request to serialise.
            response_type: The proto type to deserialise into.

        Returns:
            The deserialised proto response message.

        Raises:
            MillError: On HTTP error status.
            MillConnectionError: On transport failure.
        """
        # Ensure endpoint stays relative so httpx base_url path is preserved.
        rel_path = path.lstrip("/")
        body = self._serialize(request_msg)
        content_kwarg: dict[str, Any]
        if isinstance(body, bytes):
            content_kwarg = {"content": body}
        else:
            content_kwarg = {"content": body.encode("utf-8")}
        request_method = "POST"
        request_url = f"{self._base_url}/{rel_path}"
        request_headers = sanitize_headers_for_log(dict(self._client.headers))

        try:
            resp = await self._client.post(rel_path, **content_kwarg)
        except httpx.TransportError as e:
            raise connection_error_from_transport(
                e,
                method=request_method,
                url=request_url,
                headers=dict(self._client.headers),
            ) from e

        if resp.status_code >= 400:
            raise _from_http_status(
                resp.status_code,
                resp.text,
                request_method=request_method,
                request_url=str(resp.request.url),
                request_headers=request_headers,
                response_headers=dict(resp.headers),
            )

        return self._deserialize(resp.content, response_type)

    # -- AsyncTransport interface --

    async def handshake(self) -> _svc.HandshakeResponse:
        """Perform protocol handshake via async HTTP."""
        return await self._post("/Handshake", _svc.HandshakeRequest(), _svc.HandshakeResponse)

    async def list_schemas(self) -> _svc.ListSchemasResponse:
        """List schemas via async HTTP."""
        return await self._post("/ListSchemas", _svc.ListSchemasRequest(), _svc.ListSchemasResponse)

    async def get_schema(self, name: str) -> _svc.GetSchemaResponse:
        """Retrieve schema definition by name via async HTTP."""
        req = _svc.GetSchemaRequest(schemaName=name)
        return await self._post("/GetSchema", req, _svc.GetSchemaResponse)

    async def parse_sql(self, sql: str) -> _svc.ParseSqlResponse:
        """Parse SQL into a Substrait plan via async HTTP."""
        req = _svc.ParseSqlRequest(
            statement=_stmt.SQLStatement(sql=sql),
        )
        return await self._post("/ParseSql", req, _svc.ParseSqlResponse)

    async def get_dialect(self, dialect_id: str | None = None) -> _dialect.GetDialectResponse:
        """Retrieve dialect metadata via async HTTP."""
        req = _dialect.GetDialectRequest()
        if dialect_id is not None:
            req.dialectId = dialect_id
        return await self._post("/GetDialect", req, _dialect.GetDialectResponse)

    async def exec_query(self, request: _svc.QueryRequest) -> AsyncIterator[_svc.QueryResultResponse]:
        """Execute via paging: ``SubmitQuery`` then ``FetchQueryResult`` loop.

        Args:
            request: A ``QueryRequest`` proto.

        Yields:
            ``QueryResultResponse`` messages (one per page).

        Raises:
            MillError: On HTTP error.
        """
        # First page via SubmitQuery
        resp: _svc.QueryResultResponse = await self._post(
            "/SubmitQuery", request, _svc.QueryResultResponse,
        )
        yield resp

        # Subsequent pages via FetchQueryResult
        while resp.HasField("pagingId") and resp.pagingId:
            fetch_req = _svc.QueryResultRequest(
                pagingId=resp.pagingId,
                fetchSize=request.config.fetchSize if request.HasField("config") else 0,
            )
            resp = await self._post("/FetchQueryResult", fetch_req, _svc.QueryResultResponse)
            yield resp

    async def close(self) -> None:
        """Close the async HTTP client and release connections."""
        await self._client.aclose()
