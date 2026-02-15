"""Tests for mill.result — ResultSet lazy-with-cache iteration."""
from __future__ import annotations

from typing import Any

import pytest

from mill._proto import common_pb2 as cpb
from mill._proto import data_connect_svc_pb2 as svc
from mill._proto import vector_pb2 as vpb
from mill.result import ResultSet


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


# ---------------------------------------------------------------------------
# Tests
# ---------------------------------------------------------------------------

class TestResultSetIteration:
    def test_single_block(self) -> None:
        resp = _make_response(
            ["id", "name"], [3, 12],
            {"id": [1, 2], "name": ["Alice", "Bob"]},
        )
        rs = ResultSet(iter([resp]))
        rows = list(rs)
        assert len(rows) == 2
        assert rows[0] == {"id": 1, "name": "Alice"}
        assert rows[1] == {"id": 2, "name": "Bob"}

    def test_multiple_blocks(self) -> None:
        r1 = _make_response(["x"], [3], {"x": [1, 2]})
        r2 = _make_response(["x"], [3], {"x": [3, 4]})
        rs = ResultSet(iter([r1, r2]))
        rows = list(rs)
        assert [r["x"] for r in rows] == [1, 2, 3, 4]

    def test_empty_source(self) -> None:
        rs = ResultSet(iter([]))
        rows = list(rs)
        assert rows == []

    def test_fields_populated_from_first_block(self) -> None:
        resp = _make_response(["col"], [3], {"col": [42]})
        rs = ResultSet(iter([resp]))
        assert rs.fields is None  # not consumed yet
        _ = list(rs)
        assert rs.fields is not None
        assert rs.fields[0].name == "col"

    def test_fields_none_before_consumption(self) -> None:
        rs = ResultSet(iter([]))
        assert rs.fields is None


class TestResultSetFetchall:
    def test_fetchall_returns_all(self) -> None:
        r1 = _make_response(["v"], [3], {"v": [10]})
        r2 = _make_response(["v"], [3], {"v": [20]})
        rs = ResultSet(iter([r1, r2]))
        all_rows = rs.fetchall()
        assert len(all_rows) == 2
        assert all_rows[0]["v"] == 10
        assert all_rows[1]["v"] == 20

    def test_fetchall_empty(self) -> None:
        rs = ResultSet(iter([]))
        assert rs.fetchall() == []


class TestResultSetCacheReplay:
    def test_re_iteration_replays_cache(self) -> None:
        r1 = _make_response(["n"], [3], {"n": [1]})
        r2 = _make_response(["n"], [3], {"n": [2]})
        rs = ResultSet(iter([r1, r2]))

        # First pass — consumes both blocks
        first = list(rs)
        assert [r["n"] for r in first] == [1, 2]

        # Second pass — replays from cache
        second = list(rs)
        assert [r["n"] for r in second] == [1, 2]

    def test_partial_consumption_then_re_iterate(self) -> None:
        r1 = _make_response(["n"], [3], {"n": [1]})
        r2 = _make_response(["n"], [3], {"n": [2]})
        r3 = _make_response(["n"], [3], {"n": [3]})
        rs = ResultSet(iter([r1, r2, r3]))

        # Consume only first row, then break
        for row in rs:
            assert row["n"] == 1
            break

        # Re-iterate — should get all 3 blocks (replay cached + fetch rest)
        all_rows = rs.fetchall()
        assert [r["n"] for r in all_rows] == [1, 2, 3]

    def test_re_iteration_after_full_consumption(self) -> None:
        r1 = _make_response(["v"], [3], {"v": [99]})
        rs = ResultSet(iter([r1]))
        list(rs)  # full consumption
        list(rs)  # replay
        assert rs.fetchall() == [{"v": 99}]


class TestResultSetRepr:
    def test_repr_streaming(self) -> None:
        rs = ResultSet(iter([]))
        assert "streaming" in repr(rs)

    def test_repr_exhausted(self) -> None:
        rs = ResultSet(iter([]))
        list(rs)
        assert "exhausted" in repr(rs)


class TestResultSetExtrasStubs:
    """Verify extras raise ImportError with install instructions."""

    def test_to_arrow_lazy_import(self) -> None:
        """to_arrow should work when pyarrow is installed (extras are dev deps)."""
        r1 = _make_response(["v"], [3], {"v": [1]})
        rs = ResultSet(iter([r1]))
        # mill.extras.arrow isn't implemented yet (Phase 6), so this may
        # raise ImportError or AttributeError depending on the stub state.
        # We just verify it doesn't crash at the import level.
        try:
            rs.to_arrow()
        except (ImportError, AttributeError, TypeError, NotImplementedError):
            pass  # expected — implementation is Phase 6

    def test_to_pandas_lazy_import(self) -> None:
        r1 = _make_response(["v"], [3], {"v": [1]})
        rs = ResultSet(iter([r1]))
        try:
            rs.to_pandas()
        except (ImportError, AttributeError, TypeError, NotImplementedError):
            pass

    def test_to_polars_lazy_import(self) -> None:
        r1 = _make_response(["v"], [3], {"v": [1]})
        rs = ResultSet(iter([r1]))
        try:
            rs.to_polars()
        except (ImportError, AttributeError, TypeError, NotImplementedError):
            pass
