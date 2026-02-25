"""Mill asynchronous client.

:class:`AsyncMillClient` mirrors the synchronous :class:`MillClient` but
all methods are ``async``.  It wraps an :class:`AsyncTransport` and exposes
schema introspection, SQL parsing, and query execution returning
:class:`AsyncResultSet`.
"""
from __future__ import annotations

from typing import TYPE_CHECKING, Any

from mill._proto import data_connect_svc_pb2 as _svc
from mill._proto import statement_pb2 as _stmt
from mill.aio._transport import AsyncTransport
from mill.types import MillSchema

if TYPE_CHECKING:
    from mill.aio.result import AsyncResultSet


class AsyncMillClient:
    """Asynchronous Mill data-service client.

    Typically created via :func:`mill.aio.connect` rather than directly.

    Args:
        transport: An open :class:`AsyncTransport` instance.

    Example::

        >>> from mill.aio import connect
        >>> async with await connect("grpc://localhost:9090") as client:
        ...     for name in await client.list_schemas():
        ...         print(name)
    """

    def __init__(self, transport: AsyncTransport) -> None:
        self._transport = transport

    # -- schema operations --

    async def handshake(self) -> _svc.HandshakeResponse:
        """Perform a protocol handshake.

        Returns:
            The raw ``HandshakeResponse`` proto (version, capabilities,
            authentication context).
        """
        return await self._transport.handshake()

    async def list_schemas(self) -> list[str]:
        """List available schema names.

        Returns:
            A list of schema name strings.
        """
        resp = await self._transport.list_schemas()
        return list(resp.schemas)

    async def get_schema(self, name: str) -> MillSchema:
        """Retrieve the full definition of a schema.

        Args:
            name: Schema name (e.g. ``"MONETA"``).

        Returns:
            A :class:`MillSchema` with its tables and fields.

        Raises:
            MillQueryError: If the schema does not exist.
        """
        resp = await self._transport.get_schema(name)
        return MillSchema.from_proto(resp.schema)

    async def parse_sql(self, sql: str) -> Any:
        """Parse a SQL statement into a Substrait plan.

        Args:
            sql: SQL query string.

        Returns:
            The ``ParseSqlResponse`` proto.
        """
        return await self._transport.parse_sql(sql)

    # -- query execution --

    async def query(self, sql: str, *, fetch_size: int = 10_000) -> AsyncResultSet:
        """Execute a SQL query and return a lazy async result set.

        Args:
            sql: SQL query string.
            fetch_size: Maximum number of rows per server page.

        Returns:
            An :class:`AsyncResultSet` that lazily fetches and caches
            results.

        Example::

            >>> rs = await client.query("SELECT * FROM MONETA.CLIENTS")
            >>> async for row in rs:
            ...     print(row["FIRST_NAME"])
        """
        from mill.aio.result import AsyncResultSet  # avoid circular import

        request = _svc.QueryRequest(
            config=_svc.QueryExecutionConfig(fetchSize=fetch_size),
            statement=_stmt.SQLStatement(sql=sql),
        )
        stream = self._transport.exec_query(request)
        return AsyncResultSet(stream)

    # -- lifecycle --

    async def close(self) -> None:
        """Close the underlying transport and release resources."""
        await self._transport.close()

    async def __aenter__(self) -> AsyncMillClient:
        return self

    async def __aexit__(self, *exc: object) -> None:
        await self.close()

    def __repr__(self) -> str:
        return f"<AsyncMillClient transport={self._transport!r}>"
