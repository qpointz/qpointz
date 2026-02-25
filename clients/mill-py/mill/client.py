"""Mill synchronous client.

:class:`MillClient` is the primary user-facing interface.  It wraps a
:class:`Transport` and exposes schema introspection, SQL parsing, and
query execution returning :class:`ResultSet`.
"""
from __future__ import annotations

from typing import TYPE_CHECKING, Any

from mill._proto import data_connect_svc_pb2 as _svc
from mill._proto import statement_pb2 as _stmt
from mill._transport import Transport
from mill.types import MillSchema, MillTable

if TYPE_CHECKING:
    from mill.result import ResultSet


class MillClient:
    """Synchronous Mill data-service client.

    Typically created via :func:`mill.connect` rather than directly.

    Args:
        transport: An open :class:`Transport` instance.

    Example::

        >>> from mill import connect
        >>> with connect("grpc://localhost:9090") as client:
        ...     for name in client.list_schemas():
        ...         print(name)
    """

    def __init__(self, transport: Transport) -> None:
        self._transport = transport

    # -- schema operations --

    def handshake(self) -> _svc.HandshakeResponse:
        """Perform a protocol handshake.

        Returns:
            The raw ``HandshakeResponse`` proto (version, capabilities,
            authentication context).
        """
        return self._transport.handshake()

    def list_schemas(self) -> list[str]:
        """List available schema names.

        Returns:
            A list of schema name strings.
        """
        resp = self._transport.list_schemas()
        return list(resp.schemas)

    def get_schema(self, name: str) -> MillSchema:
        """Retrieve the full definition of a schema.

        Args:
            name: Schema name (e.g. ``"MONETA"``).

        Returns:
            A :class:`MillSchema` with its tables and fields.

        Raises:
            MillQueryError: If the schema does not exist.
        """
        resp = self._transport.get_schema(name)
        return MillSchema.from_proto(resp.schema)

    def parse_sql(self, sql: str) -> Any:
        """Parse a SQL statement into a Substrait plan.

        Args:
            sql: SQL query string.

        Returns:
            The ``ParseSqlResponse`` proto (contains an optional
            ``substrait.Plan``).
        """
        return self._transport.parse_sql(sql)

    # -- query execution --

    def query(self, sql: str, *, fetch_size: int = 10_000) -> ResultSet:
        """Execute a SQL query and return a lazy result set.

        Args:
            sql: SQL query string.
            fetch_size: Maximum number of rows per server page.

        Returns:
            A :class:`ResultSet` that lazily fetches and caches results.

        Example::

            >>> rs = client.query("SELECT * FROM MONETA.CLIENTS")
            >>> for row in rs:
            ...     print(row["FIRST_NAME"])
        """
        from mill.result import ResultSet  # avoid circular import

        request = _svc.QueryRequest(
            config=_svc.QueryExecutionConfig(fetchSize=fetch_size),
            statement=_stmt.SQLStatement(sql=sql),
        )
        stream = self._transport.exec_query(request)
        return ResultSet(stream)

    # -- lifecycle --

    def close(self) -> None:
        """Close the underlying transport and release resources."""
        self._transport.close()

    def __enter__(self) -> MillClient:
        return self

    def __exit__(self, *exc: object) -> None:
        self.close()

    def __repr__(self) -> str:
        return f"<MillClient transport={self._transport!r}>"
