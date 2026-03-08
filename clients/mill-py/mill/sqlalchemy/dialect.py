"""SQLAlchemy dialect integration for Mill transports."""
from __future__ import annotations

from typing import Any

from sqlalchemy import types as satypes
from sqlalchemy.engine import Connection
from sqlalchemy.engine.url import URL
from sqlalchemy.exc import NoSuchTableError
from sqlalchemy.sql.compiler import IdentifierPreparer, SQLCompiler
from sqlalchemy.engine.default import DefaultDialect

from mill.types import MillField, MillType, TableType
from mill.sqlalchemy import dbapi


def _to_sqla_type(field: MillField) -> satypes.TypeEngine[Any]:
    mapping: dict[MillType, satypes.TypeEngine[Any]] = {
        MillType.TINY_INT: satypes.SmallInteger(),
        MillType.SMALL_INT: satypes.SmallInteger(),
        MillType.INT: satypes.Integer(),
        MillType.BIG_INT: satypes.BigInteger(),
        MillType.BOOL: satypes.Boolean(),
        MillType.FLOAT: satypes.Float(),
        MillType.DOUBLE: satypes.Float(),
        MillType.STRING: satypes.String(length=field.precision or None),
        MillType.BINARY: satypes.LargeBinary(length=field.precision or None),
        MillType.DATE: satypes.Date(),
        MillType.TIME: satypes.Time(),
        MillType.TIMESTAMP: satypes.DateTime(timezone=False),
        MillType.TIMESTAMP_TZ: satypes.DateTime(timezone=True),
        MillType.INTERVAL_DAY: satypes.Interval(),
        MillType.INTERVAL_YEAR: satypes.Integer(),
        MillType.UUID: satypes.Uuid(),
    }
    return mapping.get(field.type, satypes.String())


class MillIdentifierPreparer(IdentifierPreparer):
    """Identifier quoting derived from dialect metadata."""

    def __init__(self, dialect: "MillDialect") -> None:
        quote_start = '"'
        quote_end = '"'
        escape_quote = '"'

        descriptor = getattr(dialect, "_dialect_descriptor", None)
        if descriptor is not None:
            ids = descriptor.identifiers
            quote_start = ids.quote.start or quote_start
            quote_end = ids.quote.end or quote_end
            escape_quote = ids.escapeQuote or escape_quote

        super().__init__(
            dialect,
            initial_quote=quote_start,
            final_quote=quote_end,
            escape_quote=escape_quote,
        )


class MillSQLCompiler(SQLCompiler):
    """Compiler that renders paging clauses from dialect descriptor templates."""

    def limit_clause(self, select, **kw):  # noqa: ANN001 - SQLAlchemy API
        descriptor = getattr(self.dialect, "_dialect_descriptor", None)
        if descriptor is None:
            return super().limit_clause(select, **kw)

        limit_clause = select._limit_clause
        offset_clause = select._offset_clause
        if limit_clause is None and offset_clause is None:
            return ""

        styles = list(descriptor.paging.styles)
        limit_template = styles[0].syntax if styles else "LIMIT {n}"
        offset_template = descriptor.paging.offset or "OFFSET {m}"

        chunks: list[str] = []
        if limit_clause is not None:
            rendered_limit = self.process(limit_clause, **kw)
            chunks.append(limit_template.replace("{n}", rendered_limit))
        elif offset_clause is not None and descriptor.paging.noLimitValue:
            chunks.append(limit_template.replace("{n}", descriptor.paging.noLimitValue))

        if offset_clause is not None:
            rendered_offset = self.process(offset_clause, **kw)
            chunks.append(offset_template.replace("{m}", rendered_offset))

        return " " + " ".join(chunks) if chunks else ""


class MillDialect(DefaultDialect):
    """Base SQLAlchemy dialect for Mill."""

    name = "mill"
    driver = "grpc"
    preparer = MillIdentifierPreparer
    statement_compiler = MillSQLCompiler
    supports_statement_cache = False
    supports_native_boolean = True
    supports_schemas = True
    default_paramstyle = "qmark"

    def __init__(self, **kwargs: Any) -> None:
        super().__init__(**kwargs)
        self._dialect_descriptor = None

    @classmethod
    def import_dbapi(cls):
        return dbapi

    def create_connect_args(self, url: URL):
        transport = self.driver
        default_port = 9090 if transport == "grpc" else 80
        kwargs = {
            "host": url.host or "localhost",
            "port": int(url.port or default_port),
            "transport": transport,
            "base_path": url.query.get("base_path", "/services/jet"),
            "encoding": url.query.get("encoding", "json"),
            "username": url.username,
            "password": url.password,
            "dialect_id": url.query.get("dialect_id"),
        }
        return [], kwargs

    def initialize(self, connection: Connection) -> None:
        super().initialize(connection)
        raw = self._raw_mill_connection(connection)
        if raw is None:
            return

        response = raw.dialect_response
        if response is not None:
            self._dialect_descriptor = response.dialect
            if response.dialect.paramstyle:
                self.paramstyle = response.dialect.paramstyle
                self.default_paramstyle = response.dialect.paramstyle
            self.max_identifier_length = response.dialect.identifiers.maxLength or 0
            self.supports_schemas = response.dialect.catalogSchema.supportsSchemas
            self.supports_native_boolean = response.dialect.featureFlags.get(
                "supports-native-boolean", True
            )

    def _raw_mill_connection(self, connection: Connection):
        candidate = getattr(connection, "connection", None)
        if candidate is None:
            return None
        dbapi_connection = getattr(candidate, "dbapi_connection", None)
        if dbapi_connection is not None:
            return dbapi_connection
        legacy_connection = getattr(candidate, "connection", None)
        if legacy_connection is not None:
            return legacy_connection
        return candidate

    def _fetch_schema(self, connection: Connection, schema: str | None):
        raw = self._raw_mill_connection(connection)
        if raw is None:
            return None

        schema_name = schema or raw.default_schema
        if not schema_name:
            schemas = raw.client.list_schemas()
            schema_name = schemas[0] if schemas else None
        if not schema_name:
            return None
        return raw.client.get_schema(schema_name)

    def get_schema_names(self, connection: Connection, **kw):  # noqa: ANN001
        raw = self._raw_mill_connection(connection)
        if raw is None:
            return []
        return raw.client.list_schemas()

    def get_table_names(self, connection: Connection, schema: str | None = None, **kw):  # noqa: ANN001
        schema_obj = self._fetch_schema(connection, schema)
        if schema_obj is None:
            return []
        return [table.name for table in schema_obj.tables if table.table_type == TableType.TABLE]

    def get_view_names(self, connection: Connection, schema: str | None = None, **kw):  # noqa: ANN001
        schema_obj = self._fetch_schema(connection, schema)
        if schema_obj is None:
            return []
        return [table.name for table in schema_obj.tables if table.table_type == TableType.VIEW]

    def has_table(self, connection: Connection, table_name: str, schema: str | None = None, **kw):  # noqa: ANN001
        table_names = set(self.get_table_names(connection, schema=schema))
        view_names = set(self.get_view_names(connection, schema=schema))
        return table_name in table_names or table_name in view_names

    def get_columns(self, connection: Connection, table_name: str, schema: str | None = None, **kw):  # noqa: ANN001
        schema_obj = self._fetch_schema(connection, schema)
        if schema_obj is None:
            raise NoSuchTableError(table_name)

        table = next((t for t in schema_obj.tables if t.name == table_name), None)
        if table is None:
            raise NoSuchTableError(table_name)

        return [
            {
                "name": field.name,
                "type": _to_sqla_type(field),
                "nullable": field.nullable,
                "default": None,
                "autoincrement": False,
            }
            for field in table.fields
        ]

    def get_pk_constraint(self, connection: Connection, table_name: str, schema: str | None = None, **kw):  # noqa: ANN001
        return {"constrained_columns": [], "name": None}

    def get_foreign_keys(self, connection: Connection, table_name: str, schema: str | None = None, **kw):  # noqa: ANN001
        return []

    def get_indexes(self, connection: Connection, table_name: str, schema: str | None = None, **kw):  # noqa: ANN001
        return []


class MillGrpcDialect(MillDialect):
    driver = "grpc"


class MillHttpDialect(MillDialect):
    driver = "http"
