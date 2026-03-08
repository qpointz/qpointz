"""Initial ibis backend for Mill service transports."""
from __future__ import annotations

import contextlib
from typing import Any, Mapping

import ibis
import ibis.expr.types as ir
import ibis.expr.schema as sch
import sqlglot as sg
from ibis.backends.sql import SQLBackend
from ibis.backends.sql.compilers import DuckDBCompiler

from mill import connect as mill_connect
from mill.auth import Credential
from mill.sql import CALCITE_DEFAULT, MillDialectDescriptor
from mill.types import MillField, MillSchema, MillTable

from .compiler import MillIbisCompiler
from .types import to_ibis_dtype


def _coerce_descriptor(raw: Any) -> MillDialectDescriptor:
    if raw is None:
        return CALCITE_DEFAULT
    if isinstance(raw, MillDialectDescriptor):
        return raw
    if hasattr(raw, "dialect"):
        descriptor = raw.dialect
        if isinstance(descriptor, MillDialectDescriptor):
            return descriptor
    return CALCITE_DEFAULT


class Backend(SQLBackend):
    """A practical first-iteration ibis backend for Mill."""

    name = "mill"
    compiler = DuckDBCompiler()

    def do_connect(
        self,
        url: str,
        *,
        auth: Credential = None,
        encoding: str = "json",
        tls_ca: str | bytes | None = None,
        tls_cert: str | bytes | None = None,
        tls_key: str | bytes | None = None,
        dialect_id: str | None = None,
        **kwargs: Any,
    ) -> None:
        self._client = mill_connect(
            url,
            auth=auth,
            encoding=encoding,
            tls_ca=tls_ca,
            tls_cert=tls_cert,
            tls_key=tls_key,
            **kwargs,
        )
        self._schema_cache: dict[str, MillSchema] = {}

        descriptor = CALCITE_DEFAULT
        try:
            if self._client.supports_dialect():
                response = self._client._transport.get_dialect(dialect_id)  # noqa: SLF001
                descriptor = _coerce_descriptor(response)
        except Exception:
            descriptor = CALCITE_DEFAULT

        self.dialect_descriptor = descriptor
        self._capability_compiler = MillIbisCompiler(descriptor)

    @property
    def version(self) -> str:
        return "mill-ibis-v1"

    def disconnect(self) -> None:
        self._client.close()

    def _schema_name(self, database: tuple[str, str] | str | None) -> str:
        if isinstance(database, tuple):
            return database[-1]
        if isinstance(database, str) and database:
            return database
        schemas = self._client.list_schemas()
        return schemas[0] if schemas else "default"

    def _load_schema(self, schema_name: str) -> MillSchema:
        cached = self._schema_cache.get(schema_name)
        if cached is not None:
            return cached
        schema = self._client.get_schema(schema_name)
        self._schema_cache[schema_name] = schema
        return schema

    def _find_table(self, schema_name: str, table_name: str) -> MillTable:
        schema = self._load_schema(schema_name)
        for table in schema.tables:
            if table.name.lower() == table_name.lower():
                return table
        raise KeyError(f"Table {schema_name}.{table_name} not found")

    def list_tables(
        self, like: str | None = None, database: tuple[str, str] | str | None = None
    ) -> list[str]:
        schema_name = self._schema_name(database)
        table_names = [table.name for table in self._load_schema(schema_name).tables]
        return self._filter_with_like(table_names, like)

    def get_schema(
        self,
        table_name: str,
        *,
        catalog: str | None = None,
        database: str | None = None,
    ) -> sch.Schema:
        if catalog is not None:
            raise TypeError("Mill backend does not currently support `catalog` lookups")
        schema_name = database or self._schema_name(database)
        table = self._find_table(schema_name, table_name)
        mapping = {field.name: to_ibis_dtype(field) for field in table.fields}
        return sch.Schema(mapping)

    def raw_sql(self, query: str | sg.Expression, **kwargs: Any):
        sql = query if isinstance(query, str) else query.sql(dialect=self.dialect)
        self._capability_compiler.validate_sql(sql)
        cursor = self._client.query(sql)
        return _ResultCursor(cursor)

    def compile(
        self,
        expr: ir.Expr,
        limit: str | None = None,
        params: Mapping[ir.Expr, Any] | None = None,
        pretty: bool = False,
    ):
        sql = super().compile(expr, limit=limit, params=params, pretty=pretty)
        self._capability_compiler.validate_sql(sql)
        return sql

    @contextlib.contextmanager
    def _safe_raw_sql(self, query: str | sg.Expression, **kwargs: Any):
        cursor = self.raw_sql(query, **kwargs)
        try:
            yield cursor
        finally:
            cursor.close()

    def _get_schema_using_query(self, query: str) -> sch.Schema:
        result = self._client.query(f"SELECT * FROM ({query}) AS q LIMIT 0")
        mapping = {
            field.name: to_ibis_dtype(
                MillField(
                    name=field.name,
                    index=field.index,
                    type=field.type,
                    nullable=field.nullable,
                    precision=field.precision,
                    scale=field.scale,
                )
            )
            for field in result.fields
        }
        return sch.Schema(mapping)

    def create_table(self, *args: Any, **kwargs: Any):
        raise NotImplementedError("Mill backend is read-only; create_table is not supported.")

    def _register_in_memory_table(self, op) -> None:
        raise NotImplementedError(
            "Mill backend does not support registering in-memory ibis tables yet."
        )

    def drop_table(self, *args: Any, **kwargs: Any) -> None:
        raise NotImplementedError("Mill backend is read-only; drop_table is not supported.")

    def create_view(self, *args: Any, **kwargs: Any):
        raise NotImplementedError("Mill backend is read-only; create_view is not supported.")

    def drop_view(self, *args: Any, **kwargs: Any) -> None:
        raise NotImplementedError("Mill backend is read-only; drop_view is not supported.")


class _ResultCursor:
    """Cursor wrapper over :class:`mill.result.ResultSet`."""

    def __init__(self, result) -> None:
        self._rows = result.fetchall()
        self._columns = [field.name for field in result.fields]
        self._pos = 0

    def __iter__(self):
        for row in self._rows:
            yield tuple(row.get(column) for column in self._columns)

    def fetchall(self):
        if self._pos >= len(self._rows):
            return []
        rows = [tuple(row.get(c) for c in self._columns) for row in self._rows[self._pos :]]
        self._pos = len(self._rows)
        return rows

    def fetchmany(self, size: int = 1):
        start = self._pos
        end = min(self._pos + size, len(self._rows))
        self._pos = end
        return [tuple(row.get(c) for c in self._columns) for row in self._rows[start:end]]

    def close(self) -> None:
        return None
