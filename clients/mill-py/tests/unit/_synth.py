"""Shared synthetic test data builders for all 16 Mill types.

Provides a registry of sample values and vector builders used by
test_arrow.py, test_pandas.py, and test_polars.py.
"""
from __future__ import annotations

import datetime
import uuid
from dataclasses import dataclass
from typing import Any

from mill._proto import common_pb2 as cpb
from mill._proto import data_connect_svc_pb2 as svc
from mill._proto import vector_pb2 as vpb
from mill.result import ResultSet
from mill.types import MillType


@dataclass
class TypeSample:
    """Sample data for a single Mill type."""

    mill_type: MillType
    raw_values: list[Any]       # values to put into the proto vector
    expected_python: list[Any]  # expected Python native values after reading
    vector_field: str           # proto vector field name for building

    @property
    def proto_id(self) -> int:
        return self.mill_type.proto_id

    @property
    def label(self) -> str:
        return self.mill_type.name


def _build_vector(field_idx: int, sample: TypeSample) -> vpb.Vector:
    """Build a Vector proto from a TypeSample."""
    v = vpb.Vector(fieldIdx=field_idx)
    sub = getattr(v, sample.vector_field)
    sub.values.extend(sample.raw_values)
    return v


# ---------------------------------------------------------------------------
# Sample data for all 16 types
# ---------------------------------------------------------------------------

_TEST_UUID = uuid.UUID("12345678-1234-5678-1234-567812345678")

SAMPLES: dict[MillType, TypeSample] = {
    MillType.TINY_INT: TypeSample(
        MillType.TINY_INT, [1, -1, 127], [1, -1, 127], "i32Vector",
    ),
    MillType.SMALL_INT: TypeSample(
        MillType.SMALL_INT, [100, -100, 32767], [100, -100, 32767], "i32Vector",
    ),
    MillType.INT: TypeSample(
        MillType.INT, [42, 0, -999], [42, 0, -999], "i32Vector",
    ),
    MillType.BIG_INT: TypeSample(
        MillType.BIG_INT, [2**40, 0, -2**40], [2**40, 0, -2**40], "i64Vector",
    ),
    MillType.BOOL: TypeSample(
        MillType.BOOL, [True, False, True], [True, False, True], "boolVector",
    ),
    MillType.FLOAT: TypeSample(
        MillType.FLOAT, [1.5, -2.5, 0.0], [1.5, -2.5, 0.0], "fp32Vector",
    ),
    MillType.DOUBLE: TypeSample(
        MillType.DOUBLE, [3.14, -2.72, 0.0], [3.14, -2.72, 0.0], "fp64Vector",
    ),
    MillType.STRING: TypeSample(
        MillType.STRING, ["hello", "", "world"], ["hello", "", "world"], "stringVector",
    ),
    MillType.BINARY: TypeSample(
        MillType.BINARY, [b"\x01\x02", b"", b"\xff"], [b"\x01\x02", b"", b"\xff"], "byteVector",
    ),
    MillType.UUID: TypeSample(
        MillType.UUID, [_TEST_UUID.bytes], [_TEST_UUID], "byteVector",
    ),
    MillType.DATE: TypeSample(
        MillType.DATE,
        [0, 18628],  # 1970-01-01, 2021-01-01
        [datetime.date(1970, 1, 1), datetime.date(2021, 1, 1)],
        "i64Vector",
    ),
    MillType.TIME: TypeSample(
        MillType.TIME,
        [0, 43_200_000_000_000],  # midnight, 12:00:00
        [datetime.time(0, 0, 0), datetime.time(12, 0, 0)],
        "i64Vector",
    ),
    MillType.TIMESTAMP: TypeSample(
        MillType.TIMESTAMP,
        [0, 1_000],  # epoch, epoch + 1 second
        [datetime.datetime(1970, 1, 1), datetime.datetime(1970, 1, 1, 0, 0, 1)],
        "i64Vector",
    ),
    MillType.TIMESTAMP_TZ: TypeSample(
        MillType.TIMESTAMP_TZ,
        [0, 60_000],  # epoch UTC, epoch + 1 minute UTC
        [
            datetime.datetime(1970, 1, 1, tzinfo=datetime.timezone.utc),
            datetime.datetime(1970, 1, 1, 0, 1, 0, tzinfo=datetime.timezone.utc),
        ],
        "i64Vector",
    ),
    MillType.INTERVAL_DAY: TypeSample(
        MillType.INTERVAL_DAY,
        [0, 5, 30],
        [datetime.timedelta(days=0), datetime.timedelta(days=5), datetime.timedelta(days=30)],
        "i32Vector",
    ),
    MillType.INTERVAL_YEAR: TypeSample(
        MillType.INTERVAL_YEAR, [0, 12, 36], [0, 12, 36], "i32Vector",
    ),
}


# ---------------------------------------------------------------------------
# Builders
# ---------------------------------------------------------------------------

def make_block_for_type(mill_type: MillType) -> vpb.VectorBlock:
    """Build a VectorBlock with a single column of the given type."""
    sample = SAMPLES[mill_type]
    size = len(sample.raw_values)
    field = cpb.Field(
        name="col",
        fieldIdx=0,
        type=cpb.DataType(
            type=cpb.LogicalDataType(typeId=sample.proto_id),
            nullability=cpb.DataType.NULL,
        ),
    )
    vec = _build_vector(0, sample)
    return vpb.VectorBlock(
        schema=vpb.VectorBlockSchema(fields=[field]),
        vectorSize=size,
        vectors=[vec],
    )


def make_result_set_for_type(mill_type: MillType) -> ResultSet:
    """Build a ResultSet with one block of the given type."""
    block = make_block_for_type(mill_type)
    resp = svc.QueryResultResponse()
    resp.vector.CopyFrom(block)
    return ResultSet(iter([resp]))
