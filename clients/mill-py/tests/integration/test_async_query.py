"""Integration tests — async SQL query execution and retrieval.

Covers the full ``mill.aio`` stack: ``AsyncMillClient``, ``AsyncResultSet``,
``async for`` iteration, ``fetchall()``, and DataFrame conversion against a
live Mill service.
"""
from __future__ import annotations

import pytest

import mill.aio
from mill.aio.client import AsyncMillClient
from mill.aio.result import AsyncResultSet
from .conftest import IntegrationConfig


pytestmark = [pytest.mark.integration, pytest.mark.asyncio]


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
async def async_mill_client(mill_config: IntegrationConfig):
    """Per-test async Mill client (function-scoped to match event loop lifecycle)."""
    client = await mill.aio.connect(
        mill_config.url,
        auth=mill_config.credential,
        encoding=mill_config.encoding,
        base_path=mill_config.base_path,
        tls_ca=mill_config.tls_ca,
        tls_cert=mill_config.tls_cert,
        tls_key=mill_config.tls_key,
    )
    yield client
    await client.close()


# ---------------------------------------------------------------------------
# Handshake & introspection
# ---------------------------------------------------------------------------

class TestAsyncHandshake:
    async def test_protocol_version(self, async_mill_client: AsyncMillClient) -> None:
        resp = await async_mill_client.handshake()
        assert resp.version is not None

    async def test_list_schemas_contains_skymill(
        self, async_mill_client: AsyncMillClient, schema_name: str,
    ) -> None:
        schemas = await async_mill_client.list_schemas()
        # Case-insensitive check
        upper_schemas = [s.upper() for s in schemas]
        assert schema_name.upper() in upper_schemas


# ---------------------------------------------------------------------------
# Async query execution
# ---------------------------------------------------------------------------

class TestAsyncQuery:
    async def test_async_query_cities(
        self, async_mill_client: AsyncMillClient, schema_name: str,
    ) -> None:
        """``async for row in rs`` yields dicts."""
        rs = await async_mill_client.query(
            f'SELECT "ID", "CITY" FROM "{schema_name}"."CITIES"',
        )
        assert isinstance(rs, AsyncResultSet)
        rows: list[dict] = []
        async for row in rs:
            assert isinstance(row, dict)
            rows.append(row)
        assert len(rows) > 0

    async def test_async_fetchall(
        self, async_mill_client: AsyncMillClient, schema_name: str,
    ) -> None:
        """``await rs.fetchall()`` returns a list of dicts."""
        rs = await async_mill_client.query(
            f'SELECT "ID", "CITY", "STATE" FROM "{schema_name}"."CITIES"',
        )
        rows = await rs.fetchall()
        assert isinstance(rows, list)
        assert len(rows) > 0
        first = rows[0]
        for key in ("ID", "CITY", "STATE"):
            assert key in first

    async def test_async_re_iteration(
        self, async_mill_client: AsyncMillClient, schema_name: str,
    ) -> None:
        """Re-iterating an AsyncResultSet replays cached data."""
        rs = await async_mill_client.query(
            f'SELECT "ID" FROM "{schema_name}"."CITIES"',
        )
        first_pass = await rs.fetchall()
        second_pass = await rs.fetchall()
        assert first_pass == second_pass

    async def test_async_where_clause(
        self, async_mill_client: AsyncMillClient, schema_name: str,
    ) -> None:
        rs = await async_mill_client.query(
            f'SELECT "ID", "NAME" FROM "{schema_name}"."AIRCRAFT_TYPES" '
            f"WHERE \"NAME\" = 'narrow'",
        )
        rows = await rs.fetchall()
        assert len(rows) >= 1
        for row in rows:
            assert row["NAME"] == "narrow"

    async def test_async_large_result_paging(
        self, async_mill_client: AsyncMillClient, schema_name: str,
    ) -> None:
        """Streaming / paging works for large result sets."""
        rs = await async_mill_client.query(
            f'SELECT "ID", "WEIGHT_KG", "REVENUE" '
            f'FROM "{schema_name}"."CARGO_SHIPMENTS"',
            fetch_size=100,
        )
        rows = await rs.fetchall()
        assert len(rows) > 100


# ---------------------------------------------------------------------------
# Async DataFrame conversion
# ---------------------------------------------------------------------------

class TestAsyncDataFrameConversion:
    async def test_async_to_arrow(
        self, async_mill_client: AsyncMillClient, schema_name: str,
    ) -> None:
        import pyarrow as pa

        rs = await async_mill_client.query(
            f'SELECT "ID", "CITY", "POPULATION" FROM "{schema_name}"."CITIES"',
        )
        table = await rs.to_arrow()
        assert isinstance(table, pa.Table)
        assert table.num_rows > 0
        assert "ID" in table.column_names

    async def test_async_to_pandas(
        self, async_mill_client: AsyncMillClient, schema_name: str,
    ) -> None:
        import pandas as pd

        rs = await async_mill_client.query(
            f'SELECT "ID", "CITY", "STATE" FROM "{schema_name}"."CITIES"',
        )
        df = await rs.to_pandas()
        assert isinstance(df, pd.DataFrame)
        assert len(df) > 0
        assert list(df.columns) == ["ID", "CITY", "STATE"]

    async def test_async_to_polars(
        self, async_mill_client: AsyncMillClient, schema_name: str,
    ) -> None:
        import polars as pl

        rs = await async_mill_client.query(
            f'SELECT "ID", "CITY", "POPULATION" FROM "{schema_name}"."CITIES"',
        )
        df = await rs.to_polars()
        assert isinstance(df, pl.DataFrame)
        assert len(df) > 0
        assert df.columns == ["ID", "CITY", "POPULATION"]
