"""Unit tests for mill.sqlalchemy integration."""
# pyright: reportMissingImports=false
from __future__ import annotations

from dataclasses import dataclass

import pytest

from mill.types import MillField, MillSchema, MillTable, MillType, TableType

sa = pytest.importorskip("sqlalchemy")
from sqlalchemy.engine.url import make_url
from sqlalchemy.sql import select, table, column

from mill.sqlalchemy import dbapi
from mill.sqlalchemy.dialect import MillGrpcDialect, MillHttpDialect, MillSQLCompiler


def test_create_connect_args_grpc() -> None:
    dialect = MillGrpcDialect()
    cargs, cparams = dialect.create_connect_args(make_url("mill+grpc://localhost:9191"))
    assert cargs == []
    assert cparams["transport"] == "grpc"
    assert cparams["host"] == "localhost"
    assert cparams["port"] == 9191


def test_create_connect_args_http() -> None:
    dialect = MillHttpDialect()
    _, cparams = dialect.create_connect_args(
        make_url("mill+http://localhost:8080?base_path=%2Fservices%2Fjet&encoding=protobuf")
    )
    assert cparams["transport"] == "http"
    assert cparams["base_path"] == "/services/jet"
    assert cparams["encoding"] == "protobuf"


def test_compiler_limit_clause_uses_paging_template() -> None:
    dialect = MillGrpcDialect()
    dialect._dialect_descriptor = type(
        "Descriptor",
        (),
        {
            "paging": type(
                "Paging",
                (),
                {
                    "styles": [type("Style", (), {"syntax": "FETCH FIRST {n} ROWS ONLY"})()],
                    "offset": "OFFSET {m} ROWS",
                    "noLimitValue": "",
                },
            )()
        },
    )()
    compiler = MillSQLCompiler(dialect, select(column("id")).limit(10).offset(5))
    clause = compiler.limit_clause(select(column("id")).limit(10).offset(5))
    assert "FETCH FIRST" in clause
    assert "OFFSET" in clause


@dataclass
class _FakeClient:
    schema: MillSchema

    def list_schemas(self) -> list[str]:
        return ["skymill"]

    def get_schema(self, name: str) -> MillSchema:  # noqa: ARG002
        return self.schema


@dataclass
class _FakeDbapiConn:
    client: _FakeClient
    default_schema: str | None = "skymill"
    dialect_response: object | None = None


class _FakeWrappedConnection:
    def __init__(self, dbapi_connection: _FakeDbapiConn) -> None:
        self.connection = type("Inner", (), {"dbapi_connection": dbapi_connection})()


def test_get_columns_maps_mill_schema() -> None:
    dialect = MillGrpcDialect()
    schema = MillSchema(
        tables=(
            MillTable(
                name="CITIES",
                schema_name="skymill",
                table_type=TableType.TABLE,
                fields=(
                    MillField("ID", 0, MillType.INT, False, 0, 0),
                    MillField("CITY", 1, MillType.STRING, True, 128, 0),
                ),
            ),
        )
    )
    conn = _FakeWrappedConnection(_FakeDbapiConn(client=_FakeClient(schema=schema)))
    cols = dialect.get_columns(conn, "CITIES", schema="skymill")
    assert cols[0]["name"] == "ID"
    assert cols[0]["nullable"] is False
    assert cols[1]["name"] == "CITY"


def test_dbapi_cursor_execute_and_fetch(monkeypatch: pytest.MonkeyPatch) -> None:
    class _FakeResult:
        def fetchall(self):
            return [{"ID": 1, "CITY": "Paris"}, {"ID": 2, "CITY": "London"}]

    class _FakeMillClient:
        def supports_dialect(self) -> bool:
            return False

        def query(self, sql: str):  # noqa: ARG002
            return _FakeResult()

        def close(self) -> None:
            return None

    monkeypatch.setattr("mill.sqlalchemy.dbapi.mill_connect", lambda *a, **k: _FakeMillClient())
    conn = dbapi.connect(host="localhost", port=9090, transport="grpc")
    cur = conn.cursor()
    cur.execute("SELECT * FROM CITIES WHERE ID = ?", [1])
    assert cur.rowcount == 2
    assert cur.fetchone() == (1, "Paris")
    assert cur.fetchall() == [(2, "London")]
    cur.close()
    conn.close()


def test_sqlalchemy_select_compile_with_mill_dialect() -> None:
    dialect = MillGrpcDialect()
    compiled = str(select(table("CITIES", column("ID")).c.ID).limit(3).compile(dialect=dialect))
    assert "SELECT" in compiled.upper()
