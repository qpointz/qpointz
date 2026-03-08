"""Unit tests for mill.ibis first iteration."""
# pyright: reportMissingImports=false
from __future__ import annotations

from dataclasses import dataclass

import pytest

ibis = pytest.importorskip("ibis")

from mill.ibis import connect
from mill.ibis.compiler import MillIbisCapabilityError, MillIbisCompiler
from mill.ibis.types import to_ibis_dtype
from mill.sql import MillDialectDescriptor
from mill.types import MillField, MillSchema, MillTable, MillType, TableType


def test_to_ibis_dtype_basic_mapping() -> None:
    field = MillField(name="id", index=0, type=MillType.INT, nullable=False)
    dtype = to_ibis_dtype(field)
    assert str(dtype) == "!int32"
    assert dtype.nullable is False


def test_capability_compiler_blocks_intersect() -> None:
    descriptor = MillDialectDescriptor(
        id="CALCITE",
        name="Calcite",
        read_only=True,
        paramstyle="qmark",
        feature_flags={"supports-intersect": False},
    )
    compiler = MillIbisCompiler(descriptor)
    with pytest.raises(MillIbisCapabilityError, match="INTERSECT"):
        compiler.validate_sql("SELECT 1 INTERSECT SELECT 2")


@dataclass
class _FakeResult:
    rows: list[dict]
    fields: tuple[MillField, ...]

    def fetchall(self):
        return self.rows


class _FakeMillClient:
    def __init__(self):
        self._schema = MillSchema(
            tables=(
                MillTable(
                    name="cities",
                    schema_name="skymill",
                    table_type=TableType.TABLE,
                    fields=(
                        MillField("id", 0, MillType.INT, False),
                        MillField("city", 1, MillType.STRING, True),
                    ),
                ),
            )
        )

    def supports_dialect(self) -> bool:
        return False

    def list_schemas(self) -> list[str]:
        return ["skymill"]

    def get_schema(self, schema_name: str) -> MillSchema:  # noqa: ARG002
        return self._schema

    def query(self, sql: str):
        sql_upper = sql.upper()
        if "LIMIT 0" in sql_upper:
            return _FakeResult(
                rows=[],
                fields=(
                    MillField("id", 0, MillType.INT, False),
                    MillField("city", 1, MillType.STRING, True),
                ),
            )
        return _FakeResult(
            rows=[{"id": 1, "city": "Tokyo"}, {"id": 2, "city": "London"}],
            fields=(
                MillField("id", 0, MillType.INT, False),
                MillField("city", 1, MillType.STRING, True),
            ),
        )

    def close(self) -> None:
        return None


def test_backend_table_and_compile(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr("mill.ibis.backend.mill_connect", lambda *a, **k: _FakeMillClient())
    backend = connect("grpc://localhost:9090")
    expr = backend.table("cities", database="skymill").select("id").limit(5)
    sql = backend.compile(expr)
    assert "SELECT" in sql.upper()
    assert "LIMIT" in sql.upper()
    assert backend.list_tables(database="skymill") == ["cities"]
    backend.disconnect()
