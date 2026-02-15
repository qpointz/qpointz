"""Integration tests — SQL query execution against skymill dataset."""
from __future__ import annotations

import pytest

from mill import MillClient
from mill.result import ResultSet


@pytest.mark.integration
class TestSimpleSelect:
    """Basic SELECT queries."""

    def test_select_cities_returns_rows(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        rs = mill_client.query(
            f'SELECT "ID", "CITY" FROM "{schema_name}"."CITIES"',
        )
        rows = rs.fetchall()
        assert len(rows) > 0, "Expected at least one city row"

    def test_select_cities_row_has_expected_keys(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        rs = mill_client.query(
            f'SELECT "ID", "CITY", "STATE" FROM "{schema_name}"."CITIES"',
        )
        rows = rs.fetchall()
        assert len(rows) > 0
        first = rows[0]
        for key in ("ID", "CITY", "STATE"):
            assert key in first, f"Key {key!r} missing from row: {first.keys()}"


@pytest.mark.integration
class TestWhereClause:
    """SELECT with WHERE filtering."""

    def test_where_returns_filtered(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        rs = mill_client.query(
            f'SELECT "ID", "NAME" FROM "{schema_name}"."AIRCRAFT_TYPES" '
            f"WHERE \"NAME\" = 'narrow'",
        )
        rows = rs.fetchall()
        assert len(rows) >= 1
        for row in rows:
            assert row["NAME"] == "narrow"

    def test_impossible_where_returns_empty_with_fields(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        rs = mill_client.query(
            f'SELECT "ID", "CITY" FROM "{schema_name}"."CITIES" WHERE "ID" < -999999',
        )
        rows = rs.fetchall()
        assert len(rows) == 0
        # Fields should still be populated from schema
        assert rs.fields is not None
        field_names = [f.name for f in rs.fields]
        assert "ID" in field_names
        assert "CITY" in field_names


@pytest.mark.integration
class TestJoinQuery:
    """Multi-table JOIN queries."""

    def test_join_segments_cities(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        sql = (
            f'SELECT "s"."ID", "s"."DISTANCE", "c"."CITY" AS "ORIGIN_CITY" '
            f'FROM "{schema_name}"."SEGMENTS" AS "s" '
            f'JOIN "{schema_name}"."CITIES" AS "c" ON "s"."ORIGIN" = "c"."ID"'
        )
        rs = mill_client.query(sql)
        rows = rs.fetchall()
        assert len(rows) > 0
        first = rows[0]
        assert "ORIGIN_CITY" in first


@pytest.mark.integration
class TestFetchallAndIteration:
    """fetchall(), iteration, and re-iteration behaviour."""

    def test_fetchall_returns_list(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        rs = mill_client.query(
            f'SELECT "ID" FROM "{schema_name}"."CITIES"',
        )
        result = rs.fetchall()
        assert isinstance(result, list)

    def test_iteration_yields_dicts(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        rs = mill_client.query(
            f'SELECT "ID", "CITY" FROM "{schema_name}"."CITIES"',
        )
        count = 0
        for row in rs:
            assert isinstance(row, dict)
            count += 1
        assert count > 0

    def test_re_iteration_returns_same_data(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        rs = mill_client.query(
            f'SELECT "ID", "CITY" FROM "{schema_name}"."CITIES"',
        )
        first_pass = list(rs)
        second_pass = list(rs)
        assert first_pass == second_pass, "Re-iteration should return identical rows"


@pytest.mark.integration
class TestLargeQuery:
    """Queries returning many rows — validates streaming / paging."""

    def test_cargo_shipments_many_rows(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        rs = mill_client.query(
            f'SELECT "ID", "WEIGHT_KG", "REVENUE" '
            f'FROM "{schema_name}"."CARGO_SHIPMENTS"',
            fetch_size=100,
        )
        rows = rs.fetchall()
        # skymill generates ~2000 * 0.7 ≈ 1400 rows
        assert len(rows) > 100, (
            f"Expected many rows from CARGO_SHIPMENTS, got {len(rows)}"
        )

    def test_bookings_paging(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        rs = mill_client.query(
            f'SELECT "ID", "PASSENGER_ID", "SEAT_NUMBER" '
            f'FROM "{schema_name}"."BOOKINGS"',
            fetch_size=50,
        )
        rows = rs.fetchall()
        assert len(rows) > 50, (
            f"Expected many rows from BOOKINGS, got {len(rows)}"
        )


@pytest.mark.integration
class TestDataFrameConversion:
    """Validate to_arrow(), to_pandas(), to_polars() against live data."""

    def test_to_arrow(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        import pyarrow as pa

        rs = mill_client.query(
            f'SELECT "ID", "CITY", "POPULATION" FROM "{schema_name}"."CITIES"',
        )
        table = rs.to_arrow()
        assert isinstance(table, pa.Table)
        assert table.num_rows > 0
        assert "ID" in table.column_names
        assert "CITY" in table.column_names
        assert "POPULATION" in table.column_names

    def test_to_pandas(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        import pandas as pd

        rs = mill_client.query(
            f'SELECT "ID", "CITY", "STATE" FROM "{schema_name}"."CITIES"',
        )
        df = rs.to_pandas()
        assert isinstance(df, pd.DataFrame)
        assert len(df) > 0
        assert list(df.columns) == ["ID", "CITY", "STATE"]

    def test_to_polars(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        import polars as pl

        rs = mill_client.query(
            f'SELECT "ID", "CITY", "POPULATION" FROM "{schema_name}"."CITIES"',
        )
        df = rs.to_polars()
        assert isinstance(df, pl.DataFrame)
        assert len(df) > 0
        assert df.columns == ["ID", "CITY", "POPULATION"]

    def test_arrow_large_result(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        import pyarrow as pa

        rs = mill_client.query(
            f'SELECT "ID", "WEIGHT_KG", "REVENUE" '
            f'FROM "{schema_name}"."CARGO_SHIPMENTS"',
            fetch_size=100,
        )
        table = rs.to_arrow()
        assert isinstance(table, pa.Table)
        assert table.num_rows > 100

    def test_pandas_column_types(
        self, mill_client: MillClient, schema_name: str,
    ) -> None:
        import pandas as pd

        rs = mill_client.query(
            f'SELECT "ID", "NAME", "DESCRIPTION" '
            f'FROM "{schema_name}"."AIRCRAFT_TYPES"',
        )
        df = rs.to_pandas()
        assert isinstance(df, pd.DataFrame)
        assert len(df) == 3  # narrow, wide, regional
