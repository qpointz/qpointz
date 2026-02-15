"""Tests for mill.extras.polars — ResultSet to polars.DataFrame."""
from __future__ import annotations

import pytest
import polars as pl

from mill._proto import common_pb2 as cpb
from mill._proto import data_connect_svc_pb2 as svc
from mill._proto import vector_pb2 as vpb
from mill.extras.polars import result_to_polars
from mill.result import ResultSet
from mill.types import MillType


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_block(
    type_ids: list[tuple[str, int]],
    vectors: list[vpb.Vector],
    size: int,
) -> vpb.VectorBlock:
    schema_fields = []
    for idx, (name, tid) in enumerate(type_ids):
        schema_fields.append(cpb.Field(
            name=name, fieldIdx=idx,
            type=cpb.DataType(
                type=cpb.LogicalDataType(typeId=tid),
                nullability=cpb.DataType.NULL,
            ),
        ))
    return vpb.VectorBlock(
        schema=vpb.VectorBlockSchema(fields=schema_fields),
        vectorSize=size, vectors=vectors,
    )


def _int_vector(field_idx: int, values: list[int]) -> vpb.Vector:
    v = vpb.Vector(fieldIdx=field_idx)
    v.i32Vector.values.extend(values)
    return v


def _string_vector(field_idx: int, values: list[str]) -> vpb.Vector:
    v = vpb.Vector(fieldIdx=field_idx)
    v.stringVector.values.extend(values)
    return v


def _double_vector(field_idx: int, values: list[float]) -> vpb.Vector:
    v = vpb.Vector(fieldIdx=field_idx)
    v.fp64Vector.values.extend(values)
    return v


def _mock_result_set(blocks: list[vpb.VectorBlock]) -> ResultSet:
    responses = []
    for block in blocks:
        resp = svc.QueryResultResponse()
        resp.vector.CopyFrom(block)
        responses.append(resp)
    return ResultSet(iter(responses))


# ---------------------------------------------------------------------------
# Tests
# ---------------------------------------------------------------------------

@pytest.mark.unit
class TestResultToPolars:
    """Validate polars DataFrame conversion."""

    def test_basic_dataframe(self) -> None:
        block = _make_block(
            [("id", MillType.INT.proto_id), ("name", MillType.STRING.proto_id)],
            [_int_vector(0, [1, 2, 3]), _string_vector(1, ["a", "b", "c"])],
            3,
        )
        rs = _mock_result_set([block])
        df = result_to_polars(rs)
        assert isinstance(df, pl.DataFrame)
        assert len(df) == 3
        assert df.columns == ["id", "name"]

    def test_column_values(self) -> None:
        block = _make_block([("x", MillType.INT.proto_id)], [_int_vector(0, [10, 20])], 2)
        rs = _mock_result_set([block])
        df = result_to_polars(rs)
        assert df["x"].to_list() == [10, 20]

    def test_multi_block(self) -> None:
        b1 = _make_block([("v", MillType.DOUBLE.proto_id)], [_double_vector(0, [1.1, 2.2])], 2)
        b2 = _make_block([("v", MillType.DOUBLE.proto_id)], [_double_vector(0, [3.3])], 1)
        rs = _mock_result_set([b1, b2])
        df = result_to_polars(rs)
        assert len(df) == 3

    def test_empty_result(self) -> None:
        rs = _mock_result_set([])
        df = result_to_polars(rs)
        assert isinstance(df, pl.DataFrame)
        assert len(df) == 0

    def test_null_values(self) -> None:
        vec = _int_vector(0, [10, 0, 30])
        vec.nulls.nulls.extend([False, True, False])
        block = _make_block([("x", MillType.INT.proto_id)], [vec], 3)
        rs = _mock_result_set([block])
        df = result_to_polars(rs)
        assert df["x"][0] == 10
        assert df["x"][1] is None
        assert df["x"][2] == 30

    def test_to_polars_method(self) -> None:
        block = _make_block([("s", MillType.STRING.proto_id)], [_string_vector(0, ["world"])], 1)
        rs = _mock_result_set([block])
        df = rs.to_polars()
        assert isinstance(df, pl.DataFrame)
        assert df["s"][0] == "world"

    def test_string_column(self) -> None:
        block = _make_block([("txt", MillType.STRING.proto_id)], [_string_vector(0, ["foo", "bar"])], 2)
        rs = _mock_result_set([block])
        df = result_to_polars(rs)
        assert df["txt"].to_list() == ["foo", "bar"]


# ---------------------------------------------------------------------------
# Parametrized all-16-types conversion test
# ---------------------------------------------------------------------------

from ._synth import SAMPLES, make_result_set_for_type

_ALL_TYPES = list(SAMPLES.keys())


@pytest.mark.unit
class TestAllTypesPolars:
    """Verify polars conversion for every Mill logical type."""

    @pytest.mark.parametrize("mill_type", _ALL_TYPES, ids=[t.name for t in _ALL_TYPES])
    def test_dataframe_row_count(self, mill_type: MillType) -> None:
        sample = SAMPLES[mill_type]
        rs = make_result_set_for_type(mill_type)
        df = result_to_polars(rs)
        assert isinstance(df, pl.DataFrame)
        assert len(df) == len(sample.raw_values)

    @pytest.mark.parametrize("mill_type", _ALL_TYPES, ids=[t.name for t in _ALL_TYPES])
    def test_dataframe_has_column(self, mill_type: MillType) -> None:
        rs = make_result_set_for_type(mill_type)
        df = result_to_polars(rs)
        assert "col" in df.columns
