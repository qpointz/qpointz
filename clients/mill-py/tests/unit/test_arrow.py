"""Tests for mill.extras.arrow — VectorBlock to PyArrow conversion.

Tests all 16 Mill logical types, null handling, schema correctness,
multi-block table assembly, and the binary fixture.
"""
from __future__ import annotations

import datetime
import uuid
from pathlib import Path
from typing import Any

import pyarrow as pa
import pytest

from mill._proto import common_pb2 as cpb
from mill._proto import data_connect_svc_pb2 as svc
from mill._proto import vector_pb2 as vpb
from mill.extras.arrow import (
    _ARROW_TYPES,
    arrow_type,
    result_to_arrow,
    vector_block_to_record_batch,
)
from mill.result import ResultSet
from mill.types import MillType

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

_FIXTURE_DIR = Path(__file__).resolve().parents[2] / ".." / ".." / "test" / "messages"
_BIN_PATH = _FIXTURE_DIR / "logical-types.bin"


def _make_block(
    type_ids: list[tuple[str, int]],
    vectors: list[vpb.Vector],
    size: int,
) -> vpb.VectorBlock:
    """Build a VectorBlock from (name, typeId) specs, vectors, and size."""
    schema_fields = []
    for idx, (name, tid) in enumerate(type_ids):
        schema_fields.append(cpb.Field(
            name=name,
            fieldIdx=idx,
            type=cpb.DataType(
                type=cpb.LogicalDataType(typeId=tid),
                nullability=cpb.DataType.NULL,
            ),
        ))
    return vpb.VectorBlock(
        schema=vpb.VectorBlockSchema(fields=schema_fields),
        vectorSize=size,
        vectors=vectors,
    )


def _int_vector(field_idx: int, values: list[int]) -> vpb.Vector:
    v = vpb.Vector(fieldIdx=field_idx)
    v.i32Vector.values.extend(values)
    return v


def _bigint_vector(field_idx: int, values: list[int]) -> vpb.Vector:
    v = vpb.Vector(fieldIdx=field_idx)
    v.i64Vector.values.extend(values)
    return v


def _float_vector(field_idx: int, values: list[float]) -> vpb.Vector:
    v = vpb.Vector(fieldIdx=field_idx)
    v.fp32Vector.values.extend(values)
    return v


def _double_vector(field_idx: int, values: list[float]) -> vpb.Vector:
    v = vpb.Vector(fieldIdx=field_idx)
    v.fp64Vector.values.extend(values)
    return v


def _string_vector(field_idx: int, values: list[str]) -> vpb.Vector:
    v = vpb.Vector(fieldIdx=field_idx)
    v.stringVector.values.extend(values)
    return v


def _bool_vector(field_idx: int, values: list[bool]) -> vpb.Vector:
    v = vpb.Vector(fieldIdx=field_idx)
    v.boolVector.values.extend(values)
    return v


def _byte_vector(field_idx: int, values: list[bytes]) -> vpb.Vector:
    v = vpb.Vector(fieldIdx=field_idx)
    v.byteVector.values.extend(values)
    return v


def _null_int_vector(field_idx: int, values: list[int], nulls: list[bool]) -> vpb.Vector:
    v = _int_vector(field_idx, values)
    v.nulls.nulls.extend(nulls)
    return v


def _mock_result_set(blocks: list[vpb.VectorBlock]) -> ResultSet:
    responses = []
    for block in blocks:
        resp = svc.QueryResultResponse()
        resp.vector.CopyFrom(block)
        responses.append(resp)
    return ResultSet(iter(responses))


# ---------------------------------------------------------------------------
# Type mapping tests
# ---------------------------------------------------------------------------

@pytest.mark.unit
class TestArrowTypeMapping:
    """Verify the MillType → PyArrow type mapping."""

    def test_all_16_types_mapped(self) -> None:
        for mt in MillType:
            assert mt in _ARROW_TYPES, f"{mt.name} not in _ARROW_TYPES"

    def test_tiny_int(self) -> None:
        assert arrow_type(MillType.TINY_INT) == pa.int8()

    def test_small_int(self) -> None:
        assert arrow_type(MillType.SMALL_INT) == pa.int16()

    def test_int(self) -> None:
        assert arrow_type(MillType.INT) == pa.int32()

    def test_big_int(self) -> None:
        assert arrow_type(MillType.BIG_INT) == pa.int64()

    def test_bool(self) -> None:
        assert arrow_type(MillType.BOOL) == pa.bool_()

    def test_float(self) -> None:
        assert arrow_type(MillType.FLOAT) == pa.float32()

    def test_double(self) -> None:
        assert arrow_type(MillType.DOUBLE) == pa.float64()

    def test_string(self) -> None:
        assert arrow_type(MillType.STRING) == pa.string()

    def test_binary(self) -> None:
        assert arrow_type(MillType.BINARY) == pa.binary()

    def test_uuid(self) -> None:
        assert arrow_type(MillType.UUID) == pa.binary(16)

    def test_date(self) -> None:
        assert arrow_type(MillType.DATE) == pa.date32()

    def test_time(self) -> None:
        assert arrow_type(MillType.TIME) == pa.time64("ns")

    def test_timestamp(self) -> None:
        assert arrow_type(MillType.TIMESTAMP) == pa.timestamp("ms")

    def test_timestamp_tz(self) -> None:
        assert arrow_type(MillType.TIMESTAMP_TZ) == pa.timestamp("ms", tz="UTC")

    def test_interval_day(self) -> None:
        assert arrow_type(MillType.INTERVAL_DAY) == pa.duration("s")

    def test_interval_year(self) -> None:
        assert arrow_type(MillType.INTERVAL_YEAR) == pa.int32()


# ---------------------------------------------------------------------------
# RecordBatch conversion tests
# ---------------------------------------------------------------------------

@pytest.mark.unit
class TestVectorBlockToRecordBatch:
    """Test vector_block_to_record_batch() with synthetic data."""

    def test_int_column(self) -> None:
        block = _make_block([("x", MillType.INT.proto_id)], [_int_vector(0, [10, 20, 30])], 3)
        rb = vector_block_to_record_batch(block)
        assert rb.num_rows == 3
        assert rb.num_columns == 1
        assert rb.column("x").to_pylist() == [10, 20, 30]

    def test_string_column(self) -> None:
        block = _make_block([("name", MillType.STRING.proto_id)], [_string_vector(0, ["a", "b", "c"])], 3)
        rb = vector_block_to_record_batch(block)
        assert rb.column("name").to_pylist() == ["a", "b", "c"]

    def test_bool_column(self) -> None:
        block = _make_block([("flag", MillType.BOOL.proto_id)], [_bool_vector(0, [True, False, True])], 3)
        rb = vector_block_to_record_batch(block)
        assert rb.column("flag").to_pylist() == [True, False, True]

    def test_float_column(self) -> None:
        block = _make_block([("val", MillType.FLOAT.proto_id)], [_float_vector(0, [1.5, 2.5])], 2)
        rb = vector_block_to_record_batch(block)
        vals = rb.column("val").to_pylist()
        assert abs(vals[0] - 1.5) < 0.01
        assert abs(vals[1] - 2.5) < 0.01

    def test_double_column(self) -> None:
        block = _make_block([("val", MillType.DOUBLE.proto_id)], [_double_vector(0, [3.14, 2.72])], 2)
        rb = vector_block_to_record_batch(block)
        vals = rb.column("val").to_pylist()
        assert abs(vals[0] - 3.14) < 0.001
        assert abs(vals[1] - 2.72) < 0.001

    def test_bigint_column(self) -> None:
        block = _make_block([("big", MillType.BIG_INT.proto_id)], [_bigint_vector(0, [2**40, 2**41])], 2)
        rb = vector_block_to_record_batch(block)
        assert rb.column("big").to_pylist() == [2**40, 2**41]

    def test_date_column(self) -> None:
        block = _make_block([("d", MillType.DATE.proto_id)], [_bigint_vector(0, [0, 19000])], 2)
        rb = vector_block_to_record_batch(block)
        vals = rb.column("d").to_pylist()
        assert vals[0] == datetime.date(1970, 1, 1)

    def test_timestamp_column(self) -> None:
        block = _make_block([("ts", MillType.TIMESTAMP.proto_id)], [_bigint_vector(0, [0, 1000])], 2)
        rb = vector_block_to_record_batch(block)
        vals = rb.column("ts").to_pylist()
        assert vals[0] == datetime.datetime(1970, 1, 1)
        assert vals[1] == datetime.datetime(1970, 1, 1, 0, 0, 1)

    def test_multi_column(self) -> None:
        block = _make_block(
            [("id", MillType.INT.proto_id), ("name", MillType.STRING.proto_id)],
            [_int_vector(0, [1, 2]), _string_vector(1, ["a", "b"])],
            2,
        )
        rb = vector_block_to_record_batch(block)
        assert rb.num_columns == 2
        assert rb.column("id").to_pylist() == [1, 2]
        assert rb.column("name").to_pylist() == ["a", "b"]

    def test_null_handling(self) -> None:
        block = _make_block(
            [("x", MillType.INT.proto_id)],
            [_null_int_vector(0, [10, 0, 30], [False, True, False])],
            3,
        )
        rb = vector_block_to_record_batch(block)
        assert rb.column("x").to_pylist() == [10, None, 30]

    def test_schema_types_correct(self) -> None:
        block = _make_block(
            [("a", MillType.TINY_INT.proto_id), ("b", MillType.STRING.proto_id)],
            [_int_vector(0, [1]), _string_vector(1, ["x"])],
            1,
        )
        rb = vector_block_to_record_batch(block)
        assert rb.schema.field("a").type == pa.int8()
        assert rb.schema.field("b").type == pa.string()

    def test_nullable_flag_in_schema(self) -> None:
        block = _make_block([("x", MillType.INT.proto_id)], [_int_vector(0, [1])], 1)
        rb = vector_block_to_record_batch(block)
        assert rb.schema.field("x").nullable is True

    def test_uuid_column(self) -> None:
        u = uuid.uuid4()
        block = _make_block([("uid", MillType.UUID.proto_id)], [_byte_vector(0, [u.bytes])], 1)
        rb = vector_block_to_record_batch(block)
        assert rb.column("uid").to_pylist() == [u.bytes]

    def test_interval_day_column(self) -> None:
        block = _make_block([("dur", MillType.INTERVAL_DAY.proto_id)], [_int_vector(0, [5])], 1)
        rb = vector_block_to_record_batch(block)
        assert rb.column("dur").to_pylist() == [datetime.timedelta(seconds=432000)]


# ---------------------------------------------------------------------------
# ResultSet → Arrow Table tests
# ---------------------------------------------------------------------------

@pytest.mark.unit
class TestResultToArrow:
    """Test result_to_arrow() with mock ResultSets."""

    def test_single_block(self) -> None:
        block = _make_block([("id", MillType.INT.proto_id)], [_int_vector(0, [1, 2, 3])], 3)
        rs = _mock_result_set([block])
        table = result_to_arrow(rs)
        assert isinstance(table, pa.Table)
        assert table.num_rows == 3
        assert table.column("id").to_pylist() == [1, 2, 3]

    def test_multiple_blocks(self) -> None:
        block1 = _make_block([("x", MillType.INT.proto_id)], [_int_vector(0, [1, 2])], 2)
        block2 = _make_block([("x", MillType.INT.proto_id)], [_int_vector(0, [3, 4])], 2)
        rs = _mock_result_set([block1, block2])
        table = result_to_arrow(rs)
        assert table.num_rows == 4
        assert table.column("x").to_pylist() == [1, 2, 3, 4]

    def test_empty_result(self) -> None:
        rs = _mock_result_set([])
        table = result_to_arrow(rs)
        assert isinstance(table, pa.Table)
        assert table.num_rows == 0

    def test_result_set_to_arrow_method(self) -> None:
        block = _make_block([("v", MillType.STRING.proto_id)], [_string_vector(0, ["hello"])], 1)
        rs = _mock_result_set([block])
        table = rs.to_arrow()
        assert table.column("v").to_pylist() == ["hello"]


# ---------------------------------------------------------------------------
# Binary fixture test
# ---------------------------------------------------------------------------

# ---------------------------------------------------------------------------
# Parametrized all-16-types conversion test
# ---------------------------------------------------------------------------

from ._synth import SAMPLES, make_block_for_type, make_result_set_for_type

_ALL_TYPES = list(SAMPLES.keys())


@pytest.mark.unit
class TestAllTypesArrow:
    """Verify Arrow conversion for every Mill logical type."""

    @pytest.mark.parametrize("mill_type", _ALL_TYPES, ids=[t.name for t in _ALL_TYPES])
    def test_record_batch_column_count(self, mill_type: MillType) -> None:
        block = make_block_for_type(mill_type)
        rb = vector_block_to_record_batch(block)
        assert rb.num_columns == 1

    @pytest.mark.parametrize("mill_type", _ALL_TYPES, ids=[t.name for t in _ALL_TYPES])
    def test_record_batch_row_count(self, mill_type: MillType) -> None:
        sample = SAMPLES[mill_type]
        block = make_block_for_type(mill_type)
        rb = vector_block_to_record_batch(block)
        assert rb.num_rows == len(sample.raw_values)

    @pytest.mark.parametrize("mill_type", _ALL_TYPES, ids=[t.name for t in _ALL_TYPES])
    def test_record_batch_arrow_type(self, mill_type: MillType) -> None:
        block = make_block_for_type(mill_type)
        rb = vector_block_to_record_batch(block)
        assert rb.schema.field("col").type == arrow_type(mill_type)

    @pytest.mark.parametrize("mill_type", _ALL_TYPES, ids=[t.name for t in _ALL_TYPES])
    def test_result_to_arrow_roundtrip(self, mill_type: MillType) -> None:
        rs = make_result_set_for_type(mill_type)
        table = result_to_arrow(rs)
        assert isinstance(table, pa.Table)
        sample = SAMPLES[mill_type]
        assert table.num_rows == len(sample.raw_values)
        assert table.schema.field("col").type == arrow_type(mill_type)


# ---------------------------------------------------------------------------
# Binary fixture test
# ---------------------------------------------------------------------------

@pytest.mark.unit
@pytest.mark.skipif(not _BIN_PATH.exists(), reason="binary fixture not found")
class TestBinaryFixtureArrow:
    """Convert the shared binary fixture to Arrow and validate."""

    def test_record_batch_from_fixture(self) -> None:
        vb = vpb.VectorBlock()
        vb.ParseFromString(_BIN_PATH.read_bytes())
        rb = vector_block_to_record_batch(vb)
        assert rb.num_rows == vb.vectorSize
        assert rb.num_columns == len(vb.schema.fields)
