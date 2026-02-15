"""HTTP transport implementation.

Supports both JSON and binary-protobuf encoding against the Mill HTTP
service (base path ``/services/jet`` by default).  Query execution uses
the paging pattern: ``SubmitQuery`` → ``FetchQueryResult`` loop.
"""
from __future__ import annotations

from typing import Any, Iterator

import httpx
from google.protobuf import json_format, message as _pb_message

from mill._proto import data_connect_svc_pb2 as _svc
from mill._proto import statement_pb2 as _stmt
from mill._transport import Transport
from mill.auth import Credential, _auth_headers
from mill.exceptions import _from_http_status, MillConnectionError

# Content types
_CT_JSON = "application/json"
_CT_PROTO = "application/x-protobuf"


class HttpTransport(Transport):
    """Synchronous HTTP transport using ``httpx``.

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

        >>> t = HttpTransport("localhost", 8501, encoding="json")
        >>> resp = t.handshake()
        >>> t.close()
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
        scheme = "https" if ssl else "http"
        # Normalise base_path to have leading slash, no trailing slash
        base_path = "/" + base_path.strip("/") if base_path else ""
        self._base_url = f"{scheme}://{host}:{port}{base_path}"
        self._encoding = encoding.lower()

        if self._encoding == "protobuf":
            self._content_type = _CT_PROTO
        else:
            self._content_type = _CT_JSON

        # Capitalise auth headers for HTTP (server expects standard casing)
        raw_headers = _auth_headers(auth)
        http_headers: dict[str, str] = {}
        for k, v in raw_headers.items():
            http_headers["Authorization"] = v  # always capitalised for HTTP
        http_headers["Content-Type"] = self._content_type
        http_headers["Accept"] = self._content_type

        # TLS certificate options
        verify: str | bool = True  # httpx default
        if tls_ca is not None:
            verify = tls_ca  # str path or False to disable
        cert: tuple[str, str] | str | None = None
        if tls_cert and tls_key:
            cert = (tls_cert, tls_key)
        elif tls_cert:
            cert = tls_cert

        self._client = httpx.Client(
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

    def _post(
        self,
        path: str,
        request_msg: _pb_message.Message,
        response_type: type[_pb_message.Message],
    ) -> Any:
        """POST a proto request, deserialise the response.

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
        body = self._serialize(request_msg)
        content_kwarg: dict[str, Any]
        if isinstance(body, bytes):
            content_kwarg = {"content": body}
        else:
            content_kwarg = {"content": body.encode("utf-8")}

        try:
            resp = self._client.post(path, **content_kwarg)
        except httpx.TransportError as e:
            raise MillConnectionError(str(e)) from e

        if resp.status_code >= 400:
            raise _from_http_status(resp.status_code, resp.text)

        return self._deserialize(resp.content, response_type)

    # -- Transport interface --

    def handshake(self) -> _svc.HandshakeResponse:
        """Perform protocol handshake via HTTP."""
        return self._post("/Handshake", _svc.HandshakeRequest(), _svc.HandshakeResponse)

    def list_schemas(self) -> _svc.ListSchemasResponse:
        """List schemas via HTTP."""
        return self._post("/ListSchemas", _svc.ListSchemasRequest(), _svc.ListSchemasResponse)

    def get_schema(self, name: str) -> _svc.GetSchemaResponse:
        """Retrieve schema definition by name via HTTP."""
        req = _svc.GetSchemaRequest(schemaName=name)
        return self._post("/GetSchema", req, _svc.GetSchemaResponse)

    def parse_sql(self, sql: str) -> _svc.ParseSqlResponse:
        """Parse SQL into a Substrait plan via HTTP."""
        req = _svc.ParseSqlRequest(
            statement=_stmt.SQLStatement(sql=sql),
        )
        return self._post("/ParseSql", req, _svc.ParseSqlResponse)

    def exec_query(self, request: _svc.QueryRequest) -> Iterator[_svc.QueryResultResponse]:
        """Execute via paging: ``SubmitQuery`` then ``FetchQueryResult`` loop.

        Args:
            request: A ``QueryRequest`` proto.

        Yields:
            ``QueryResultResponse`` messages (one per page).

        Raises:
            MillError: On HTTP error.
        """
        # First page via SubmitQuery
        resp: _svc.QueryResultResponse = self._post(
            "/SubmitQuery", request, _svc.QueryResultResponse,
        )
        yield resp

        # Subsequent pages via FetchQueryResult
        while resp.HasField("pagingId") and resp.pagingId:
            fetch_req = _svc.QueryResultRequest(
                pagingId=resp.pagingId,
                fetchSize=request.config.fetchSize if request.HasField("config") else 0,
            )
            resp = self._post("/FetchQueryResult", fetch_req, _svc.QueryResultResponse)
            yield resp

    def close(self) -> None:
        """Close the HTTP client and release connections."""
        self._client.close()
