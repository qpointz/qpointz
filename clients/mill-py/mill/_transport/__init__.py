"""Transport abstraction for Mill data services.

Defines the :class:`Transport` abstract base class that both
:class:`GrpcTransport` and :class:`HttpTransport` implement.
"""
from __future__ import annotations

import abc
from typing import Iterator

from mill._proto import data_connect_svc_pb2 as _svc


class Transport(abc.ABC):
    """Abstract transport — the internal interface used by :class:`MillClient`.

    Concrete implementations handle protocol specifics (gRPC channels,
    HTTP requests) while exposing a uniform set of service operations.
    """

    @abc.abstractmethod
    def handshake(self) -> _svc.HandshakeResponse:
        """Perform a protocol handshake with the server.

        Returns:
            A ``HandshakeResponse`` proto message.
        """

    @abc.abstractmethod
    def list_schemas(self) -> _svc.ListSchemasResponse:
        """List available schemas.

        Returns:
            A ``ListSchemasResponse`` proto message.
        """

    @abc.abstractmethod
    def get_schema(self, name: str) -> _svc.GetSchemaResponse:
        """Retrieve the full schema definition by name.

        Args:
            name: Schema name (e.g. ``"MONETA"``).

        Returns:
            A ``GetSchemaResponse`` proto message.
        """

    @abc.abstractmethod
    def parse_sql(self, sql: str) -> _svc.ParseSqlResponse:
        """Parse a SQL statement into a Substrait plan.

        Args:
            sql: SQL query string.

        Returns:
            A ``ParseSqlResponse`` proto message.
        """

    @abc.abstractmethod
    def exec_query(self, request: _svc.QueryRequest) -> Iterator[_svc.QueryResultResponse]:
        """Execute a query and return a streaming iterator of result pages.

        Args:
            request: A ``QueryRequest`` proto message.

        Returns:
            An iterator yielding ``QueryResultResponse`` messages.
        """

    @abc.abstractmethod
    def close(self) -> None:
        """Release transport resources (channels, connections)."""

    def __enter__(self) -> Transport:
        return self

    def __exit__(self, *exc: object) -> None:
        self.close()
