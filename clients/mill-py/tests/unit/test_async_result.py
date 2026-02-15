"""Tests for mill.aio.result — AsyncResultSet async lazy-with-cache iteration."""
from __future__ import annotations

from typing import Any, AsyncIterator

import pytest

from mill._proto import common_pb2 as cpb
from mill._proto import data_connect_svc_pb2 as svc
from mill._proto import vector_pb2 as vpb
from mill.aio.result import AsyncResultSet


pytestmark = [pytest.mark.unit, pytest.mark.asyncio]


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_response(
    names: list[str],
    type_ids: list[int],
    columns: dict[str, list[Any]],
    *,
    paging_id: str | None = None,
) -> svc.QueryResultResponse:
    """Build a QueryResultResponse with a VectorBlock."""
    size = len(next(iter(columns.values())))
    fields = [
        cpb.Field(
            name=n,
            fieldIdx=i,
            type=cpb.DataType(
                type=cpb.LogicalDataType(typeId=tid),
                nullability=cpb.DataType.NULL,
            ),
        )
        for i, (n, tid) in enumerate(zip(names, type_ids))
    ]

    vectors = []
    for i, (name, tid) in enumerate(zip(names, type_ids)):
        vals = columns[name]
        if tid in (1, 2, 3, 10, 11):  # i32 types
            vec = vpb.Vector(fieldIdx=i, i32Vector=vpb.Vector.I32Vector(values=vals))
        elif tid == 4:  # i64
            vec = vpb.Vector(fieldIdx=i, i64Vector=vpb.Vector.I64Vector(values=vals))
        elif tid == 12:  # string
            vec = vpb.Vector(fieldIdx=i, stringVector=vpb.Vector.StringVector(values=vals))
        else:
            raise ValueError(f"unsupported type_id {tid} in test helper")
        vectors.append(vec)

    block = vpb.VectorBlock(
        schema=vpb.VectorBlockSchema(fields=fields),
        vectorSize=size,
        vectors=vectors,
    )
    resp = svc.QueryResultResponse(vector=block)
    if paging_id:
        resp.pagingId = paging_id
    return resp


async def _to_async_iter(items: list[svc.QueryResultResponse]) -> AsyncIterator[svc.QueryResultResponse]:
    """Convert a list into an AsyncIterator."""
    for item in items:
        yield item


# ---------------------------------------------------------------------------
# Tests
# ---------------------------------------------------------------------------

class TestAsyncResultSetIteration:
    async def test_single_block(self) -> None:
        resp = _make_response(
            ["id", "name"], [3, 12],
            {"id": [1, 2], "name": ["Alice", "Bob"]},
        )
        rs = AsyncResultSet(_to_async_iter([resp]))
        rows = await rs.fetchall()
        assert len(rows) == 2
        assert rows[0] == {"id": 1, "name": "Alice"}
        assert rows[1] == {"id": 2, "name": "Bob"}

    async def test_multiple_blocks(self) -> None:
        r1 = _make_response(["x"], [3], {"x": [1, 2]})
        r2 = _make_response(["x"], [3], {"x": [3, 4]})
        rs = AsyncResultSet(_to_async_iter([r1, r2]))
        rows = await rs.fetchall()
        assert [r["x"] for r in rows] == [1, 2, 3, 4]

    async def test_empty_source(self) -> None:
        rs = AsyncResultSet(_to_async_iter([]))
        rows = await rs.fetchall()
        assert rows == []

    async def test_fields_populated_from_first_block(self) -> None:
        resp = _make_response(["col"], [3], {"col": [42]})
        rs = AsyncResultSet(_to_async_iter([resp]))
        assert rs.fields is None  # not consumed yet
        _ = await rs.fetchall()
        assert rs.fields is not None
        assert rs.fields[0].name == "col"

    async def test_fields_none_before_consumption(self) -> None:
        rs = AsyncResultSet(_to_async_iter([]))
        assert rs.fields is None

    async def test_async_for_iteration(self) -> None:
        resp = _make_response(
            ["id", "val"], [3, 12],
            {"id": [10, 20], "val": ["a", "b"]},
        )
        rs = AsyncResultSet(_to_async_iter([resp]))
        rows: list[dict[str, Any]] = []
        async for row in rs:
            rows.append(row)
        assert len(rows) == 2
        assert rows[0] == {"id": 10, "val": "a"}


class TestAsyncResultSetFetchall:
    async def test_fetchall_returns_all(self) -> None:
        r1 = _make_response(["v"], [3], {"v": [10]})
        r2 = _make_response(["v"], [3], {"v": [20]})
        rs = AsyncResultSet(_to_async_iter([r1, r2]))
        all_rows = await rs.fetchall()
        assert len(all_rows) == 2
        assert all_rows[0]["v"] == 10
        assert all_rows[1]["v"] == 20

    async def test_fetchall_empty(self) -> None:
        rs = AsyncResultSet(_to_async_iter([]))
        assert await rs.fetchall() == []


class TestAsyncResultSetCacheReplay:
    async def test_re_iteration_replays_cache(self) -> None:
        r1 = _make_response(["n"], [3], {"n": [1]})
        r2 = _make_response(["n"], [3], {"n": [2]})
        rs = AsyncResultSet(_to_async_iter([r1, r2]))

        # First pass
        first = await rs.fetchall()
        assert [r["n"] for r in first] == [1, 2]

        # Second pass — replays from cache
        second = await rs.fetchall()
        assert [r["n"] for r in second] == [1, 2]

    async def test_re_iteration_after_full_consumption(self) -> None:
        r1 = _make_response(["v"], [3], {"v": [99]})
        rs = AsyncResultSet(_to_async_iter([r1]))
        await rs.fetchall()
        again = await rs.fetchall()
        assert again == [{"v": 99}]


class TestAsyncResultSetRepr:
    async def test_repr_streaming(self) -> None:
        rs = AsyncResultSet(_to_async_iter([]))
        assert "streaming" in repr(rs)

    async def test_repr_exhausted(self) -> None:
        rs = AsyncResultSet(_to_async_iter([]))
        await rs.fetchall()
        assert "exhausted" in repr(rs)


class TestAsyncResultSetExtras:
    """Verify DataFrame extras work via async result set."""

    async def test_to_arrow(self) -> None:
        r1 = _make_response(["v"], [3], {"v": [1, 2, 3]})
        rs = AsyncResultSet(_to_async_iter([r1]))
        try:
            import pyarrow as pa

            table = await rs.to_arrow()
            assert isinstance(table, pa.Table)
            assert table.num_rows == 3
        except ImportError:
            pytest.skip("pyarrow not installed")

    async def test_to_pandas(self) -> None:
        r1 = _make_response(["v"], [3], {"v": [1, 2]})
        rs = AsyncResultSet(_to_async_iter([r1]))
        try:
            import pandas as pd

            df = await rs.to_pandas()
            assert isinstance(df, pd.DataFrame)
            assert len(df) == 2
        except ImportError:
            pytest.skip("pandas not installed")

    async def test_to_polars(self) -> None:
        r1 = _make_response(["v"], [3], {"v": [1, 2]})
        rs = AsyncResultSet(_to_async_iter([r1]))
        try:
            import polars as pl

            df = await rs.to_polars()
            assert isinstance(df, pl.DataFrame)
            assert len(df) == 2
        except ImportError:
            pytest.skip("polars not installed")
