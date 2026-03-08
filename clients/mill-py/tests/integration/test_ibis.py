"""Integration tests for mill.ibis backend (broad-slice WI-025)."""
# pyright: reportMissingImports=false
from __future__ import annotations

import pytest

ibis = pytest.importorskip("ibis")

from mill.ibis import connect as ibis_connect


@pytest.mark.integration
def test_ibis_table_execute_basic(schema_name: str, mill_config) -> None:
    backend = ibis_connect(
        mill_config.url,
        auth=mill_config.credential,
        encoding=mill_config.encoding,
        base_path=mill_config.base_path,
        tls_ca=mill_config.tls_ca,
        tls_cert=mill_config.tls_cert,
        tls_key=mill_config.tls_key,
    )
    try:
        expr = backend.table("cities", database=schema_name).select("id", "city").limit(10)
        frame = backend.execute(expr)
        assert len(frame) > 0
        assert "id" in frame.columns
        assert "city" in frame.columns
    finally:
        backend.disconnect()


@pytest.mark.integration
def test_ibis_raw_sql_path(schema_name: str, mill_config) -> None:
    backend = ibis_connect(
        mill_config.url,
        auth=mill_config.credential,
        encoding=mill_config.encoding,
        base_path=mill_config.base_path,
        tls_ca=mill_config.tls_ca,
        tls_cert=mill_config.tls_cert,
        tls_key=mill_config.tls_key,
    )
    try:
        sql_expr = backend.sql(
            f'SELECT "id", "city" FROM "{schema_name}"."cities" LIMIT 5',
            schema=ibis.schema({"id": "int32", "city": "string"}),
        )
        rows = backend.execute(sql_expr)
        assert len(rows) > 0
    finally:
        backend.disconnect()


@pytest.mark.integration
@pytest.mark.xfail(reason="WI-023 pending: certify set-op support against dialect reports", strict=False)
def test_ibis_intersect_gap(schema_name: str, mill_config) -> None:
    backend = ibis_connect(
        mill_config.url,
        auth=mill_config.credential,
        encoding=mill_config.encoding,
        base_path=mill_config.base_path,
        tls_ca=mill_config.tls_ca,
        tls_cert=mill_config.tls_cert,
        tls_key=mill_config.tls_key,
    )
    try:
        cities = backend.table("cities", database=schema_name).select("id")
        expr = cities.intersect(cities)
        result = backend.execute(expr)
        assert len(result) > 0
    finally:
        backend.disconnect()
