"""Async transport abstraction for Mill data services.

Defines the :class:`AsyncTransport` abstract base class that both
:class:`AsyncGrpcTransport` and :class:`AsyncHttpTransport` implement.
"""
from __future__ import annotations

import abc
from typing import AsyncIterator

from mill._proto import data_connect_svc_pb2 as _svc


class AsyncTransport(abc.ABC):
    """Abstract async transport — the internal interface used by :class:`AsyncMillClient`.

    Mirrors :class:`mill._transport.Transport` but all methods are async.
    """

    @abc.abstractmethod
    async def handshake(self) -> _svc.HandshakeResponse:
        """Perform a protocol handshake with the server."""

    @abc.abstractmethod
    async def list_schemas(self) -> _svc.ListSchemasResponse:
        """List available schemas."""

    @abc.abstractmethod
    async def get_schema(self, name: str) -> _svc.GetSchemaResponse:
        """Retrieve the full schema definition by name."""

    @abc.abstractmethod
    async def parse_sql(self, sql: str) -> _svc.ParseSqlResponse:
        """Parse a SQL statement into a Substrait plan."""

    @abc.abstractmethod
    def exec_query(self, request: _svc.QueryRequest) -> AsyncIterator[_svc.QueryResultResponse]:
        """Execute a query and return an async iterator of result pages."""

    @abc.abstractmethod
    async def close(self) -> None:
        """Release transport resources (channels, connections)."""

    async def __aenter__(self) -> AsyncTransport:
        return self

    async def __aexit__(self, *exc: object) -> None:
        await self.close()
