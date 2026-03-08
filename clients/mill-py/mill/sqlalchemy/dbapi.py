"""Minimal DBAPI 2.0 adapter used by SQLAlchemy Mill dialects."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Iterable, Mapping, Sequence

from mill import connect as mill_connect
from mill.auth import BasicAuth
from mill.sql import CALCITE_DEFAULT

apilevel = "2.0"
threadsafety = 1
paramstyle = "qmark"


class Warning(Exception):
    """DBAPI warning."""


class Error(Exception):
    """Base DBAPI error."""


class InterfaceError(Error):
    """DBAPI interface error."""


class DatabaseError(Error):
    """Base DBAPI database error."""


class DataError(DatabaseError):
    """DBAPI data error."""


class OperationalError(DatabaseError):
    """DBAPI operational error."""


class IntegrityError(DatabaseError):
    """DBAPI integrity error."""


class InternalError(DatabaseError):
    """DBAPI internal error."""


class ProgrammingError(DatabaseError):
    """DBAPI programming error."""


class NotSupportedError(DatabaseError):
    """Raised for unsupported DBAPI operations."""


def _escape_sql_string(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def _literal(value: Any) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "TRUE" if value else "FALSE"
    if isinstance(value, (int, float)):
        return str(value)
    return _escape_sql_string(str(value))


def _bind_positional(sql: str, values: Sequence[Any]) -> str:
    bound = sql
    for value in values:
        bound = bound.replace("?", _literal(value), 1)
    return bound


def _bind_named(sql: str, values: Mapping[str, Any]) -> str:
    bound = sql
    for key, value in values.items():
        bound = bound.replace(f":{key}", _literal(value))
    return bound


@dataclass
class MillConnection:
    """DBAPI connection proxy backed by :class:`mill.client.MillClient`."""

    host: str
    port: int
    transport: str = "grpc"
    base_path: str = "/services/jet"
    encoding: str = "json"
    username: str | None = None
    password: str | None = None
    dialect_id: str | None = None

    def __post_init__(self) -> None:
        self._closed = False
        self._dialect_response = None
        auth = BasicAuth(self.username, self.password) if self.username and self.password else None

        if self.transport == "http":
            url = f"http://{self.host}:{self.port}{self.base_path}"
            self._client = mill_connect(url, auth=auth, encoding=self.encoding)
        else:
            url = f"grpc://{self.host}:{self.port}"
            self._client = mill_connect(url, auth=auth)

        try:
            if self._client.supports_dialect():
                self._dialect_response = self._client._transport.get_dialect(self.dialect_id)
        except Exception:
            self._dialect_response = None

        self.paramstyle = (
            self._dialect_response.dialect.paramstyle
            if self._dialect_response and self._dialect_response.dialect.paramstyle
            else CALCITE_DEFAULT.paramstyle
        )
        self.default_schema = None

    @property
    def client(self):  # pragma: no cover - tiny forwarding property
        return self._client

    @property
    def dialect_response(self):
        return self._dialect_response

    def cursor(self) -> "MillCursor":
        if self._closed:
            raise InterfaceError("connection already closed")
        return MillCursor(self)

    def close(self) -> None:
        if not self._closed:
            self._client.close()
            self._closed = True

    def commit(self) -> None:
        # Mill is currently read-only; commit is a no-op.
        return None

    def rollback(self) -> None:
        raise NotSupportedError("Mill transport is read-only; rollback is not supported")


class MillCursor:
    """DBAPI cursor over a buffered Mill query result."""

    arraysize = 1

    def __init__(self, connection: MillConnection) -> None:
        self.connection = connection
        self._closed = False
        self._rows: list[tuple[Any, ...]] = []
        self._columns: list[str] = []
        self._pos = 0
        self.description = None
        self.rowcount = -1

    def _ensure_open(self) -> None:
        if self._closed:
            raise InterfaceError("cursor already closed")

    def execute(self, operation: str, params: Sequence[Any] | Mapping[str, Any] | None = None):
        self._ensure_open()

        sql = operation
        if params is not None:
            if isinstance(params, Mapping):
                sql = _bind_named(sql, params)
            elif isinstance(params, Sequence) and not isinstance(params, (str, bytes, bytearray)):
                sql = _bind_positional(sql, params)
            else:
                raise ProgrammingError(f"Unsupported parameter type: {type(params)!r}")

        result = self.connection.client.query(sql)
        records = result.fetchall()

        self._columns = list(records[0].keys()) if records else []
        self._rows = [tuple(record[col] for col in self._columns) for record in records]
        self._pos = 0
        self.rowcount = len(self._rows)
        self.description = [
            (name, None, None, None, None, None, True) for name in self._columns
        ] if self._columns else None
        return self

    def executemany(self, operation: str, seq_of_params: Iterable[Sequence[Any] | Mapping[str, Any]]):
        self._ensure_open()
        last = None
        for params in seq_of_params:
            last = self.execute(operation, params)
        return last if last is not None else self

    def fetchone(self):
        self._ensure_open()
        if self._pos >= len(self._rows):
            return None
        row = self._rows[self._pos]
        self._pos += 1
        return row

    def fetchmany(self, size: int | None = None):
        self._ensure_open()
        step = size or self.arraysize
        start, end = self._pos, min(self._pos + step, len(self._rows))
        self._pos = end
        return self._rows[start:end]

    def fetchall(self):
        self._ensure_open()
        if self._pos >= len(self._rows):
            return []
        rows = self._rows[self._pos:]
        self._pos = len(self._rows)
        return rows

    def close(self) -> None:
        self._closed = True


def connect(*args: Any, **kwargs: Any) -> MillConnection:
    """Create DBAPI connection.

    SQLAlchemy calls this with kwargs produced by the Mill dialect.
    """
    if args:
        raise InterfaceError("DSN positional arguments are not supported")
    host = kwargs.get("host", "localhost")
    port = int(kwargs.get("port", 9090))
    transport = kwargs.get("transport", "grpc")
    base_path = kwargs.get("base_path", "/services/jet")
    encoding = kwargs.get("encoding", "json")
    username = kwargs.get("username")
    password = kwargs.get("password")
    dialect_id = kwargs.get("dialect_id")
    return MillConnection(
        host=host,
        port=port,
        transport=transport,
        base_path=base_path,
        encoding=encoding,
        username=username,
        password=password,
        dialect_id=dialect_id,
    )
