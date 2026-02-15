"""Tests for mill.vectors — reader functions and read_vector_block().

Uses both synthetic proto messages and the shared binary fixture at
``test/messages/logical-types.bin`` (with its ``.ref`` companion).
"""
from __future__ import annotations

import base64
import datetime
import os
import uuid
from pathlib import Path
from typing import Any

import pytest

from mill._proto import common_pb2 as cpb
from mill._proto import vector_pb2 as vpb
from mill.types import MillType
from mill.vectors import read_column, read_vector_block

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

_FIXTURE_DIR = Path(__file__).resolve().parents[2] / ".." / ".." / "test" / "messages"
_BIN_PATH = _FIXTURE_DIR / "logical-types.bin"
_REF_PATH = _FIXTURE_DIR / "logical-types.ref"


def _load_vector_block() -> vpb.VectorBlock:
    """Parse the shared binary fixture into a VectorBlock."""
    vb = vpb.VectorBlock()
    vb.ParseFromString(_BIN_PATH.read_bytes())
    return vb


def _load_reference() -> dict[str, list[dict[str, Any]]]:
    """Parse the ``.ref`` companion file.

    Returns:
        ``{field_name: [row_dict, ...]}`` where each row_dict has keys
        ``idx``, ``type``, ``isnull``, ``value``, ``hints``.
    """
    result: dict[str, list[dict[str, Any]]] = {}
    for line in _REF_PATH.read_text().splitlines():
        if not line.strip():
            continue
        parts = line.split("|")
        name = parts[0]
        entry = {
            "idx": int(parts[1]),
            "type": parts[2],
            "isnull": parts[3] == "TRUE",
            "value": parts[4],
            "hints": parts[5:],
        }
        result.setdefault(name, []).append(entry)
    return result


def _parse_ref_value(entry: dict[str, Any]) -> Any:
    """Convert a reference entry's string value to the expected Python type."""
    if entry["isnull"]:
        return None
    v = entry["value"]
    t = entry["type"]
    if t == "BIG_INT":
        return int(v)
    if t in ("INT", "SMALL_INT", "TINY_INT"):
        return int(v)
    if t == "BOOL":
        return v.upper() == "TRUE"
    if t in ("DOUBLE", "FLOAT"):
        return float(v)
    if t == "STRING":
        return v
    if t == "BINARY":
        return base64.b64decode(v) if v else b""
    if t == "UUID":
        return uuid.UUID(v)
    if t == "DATE":
        return datetime.date.fromisoformat(v)
    if t == "TIME":
        # format: HH:MM:SS.nnnnnnnnn
        time_parts, nano_str = v.rsplit(".", 1)
        h, m, s = (int(x) for x in time_parts.split(":"))
        nanos = int(nano_str)
        micros = nanos // 1_000
        return datetime.time(h, m, s, micros)
    if t == "TIMESTAMP":
        # format: YYYY-MM-DD HH:MM:SS.nnnnnnnnn
        dt_str, nano_str = v.rsplit(".", 1)
        dt = datetime.datetime.strptime(dt_str, "%Y-%m-%d %H:%M:%S")
        millis = int(nano_str) // 1_000_000
        return dt + datetime.timedelta(milliseconds=millis)
    if t == "TIMESTAMP_TZ":
        dt_str, nano_str = v.rsplit(".", 1)
        dt = datetime.datetime.strptime(dt_str, "%Y-%m-%d %H:%M:%S")
        dt = dt.replace(tzinfo=datetime.timezone.utc)
        millis = int(nano_str) // 1_000_000
        return dt + datetime.timedelta(milliseconds=millis)
    raise ValueError(f"Unknown ref type: {t}")


# ---------------------------------------------------------------------------
# Synthetic vector builder helpers
# ---------------------------------------------------------------------------

def _make_vector_block(
    fields: list[tuple[str, int, int]],  # (name, idx, typeId)
    vectors: list[vpb.Vector],
    size: int = 3,
) -> vpb.VectorBlock:
    """Build a VectorBlock from field specs and Vector messages."""
    schema_fields = []
    for name, idx, type_id in fields:
        schema_fields.append(cpb.Field(
            name=name,
            fieldIdx=idx,
            type=cpb.DataType(
                type=cpb.LogicalDataType(typeId=type_id),
                nullability=cpb.DataType.NULL,
            ),
        ))
    return vpb.VectorBlock(
        schema=vpb.VectorBlockSchema(fields=schema_fields),
        vectorSize=size,
        vectors=vectors,
    )


# ---------------------------------------------------------------------------
# Per-type reader tests (synthetic)
# ---------------------------------------------------------------------------

class TestIntReaders:
    """Test integer type readers with synthetic data."""

    @pytest.mark.parametrize("mill_type,type_id", [
        (MillType.TINY_INT, 1),
        (MillType.SMALL_INT, 2),
        (MillType.INT, 3),
    ])
    def test_i32_types(self, mill_type: MillType, type_id: int) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            i32Vector=vpb.Vector.I32Vector(values=[10, -20, 0]),
        )
        result = read_column(vec, mill_type, 3)
        assert result == [10, -20, 0]

    def test_big_int(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            i64Vector=vpb.Vector.I64Vector(values=[2**62, -1, 0]),
        )
        result = read_column(vec, MillType.BIG_INT, 3)
        assert result == [2**62, -1, 0]


class TestFloatReaders:
    def test_float(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            fp32Vector=vpb.Vector.FP32Vector(values=[1.5, -2.5, 0.0]),
        )
        result = read_column(vec, MillType.FLOAT, 3)
        assert len(result) == 3
        assert pytest.approx(result[0], rel=1e-5) == 1.5

    def test_double(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            fp64Vector=vpb.Vector.FP64Vector(values=[3.14, -2.71, 0.0]),
        )
        result = read_column(vec, MillType.DOUBLE, 3)
        assert result[0] == pytest.approx(3.14)


class TestBoolReader:
    def test_bool(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            boolVector=vpb.Vector.BoolVector(values=[True, False, True]),
        )
        result = read_column(vec, MillType.BOOL, 3)
        assert result == [True, False, True]


class TestStringReader:
    def test_string(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            stringVector=vpb.Vector.StringVector(values=["hello", "", "world"]),
        )
        result = read_column(vec, MillType.STRING, 3)
        assert result == ["hello", "", "world"]


class TestBinaryReader:
    def test_binary(self) -> None:
        data = [b"\x00\x01", b"", b"\xff"]
        vec = vpb.Vector(
            fieldIdx=0,
            byteVector=vpb.Vector.BytesVector(values=data),
        )
        result = read_column(vec, MillType.BINARY, 3)
        assert result == data


class TestUuidReader:
    def test_uuid(self) -> None:
        u1 = uuid.uuid4()
        u2 = uuid.uuid4()
        vec = vpb.Vector(
            fieldIdx=0,
            byteVector=vpb.Vector.BytesVector(values=[u1.bytes, u2.bytes]),
        )
        result = read_column(vec, MillType.UUID, 2)
        assert result == [u1, u2]


class TestDateReader:
    def test_epoch(self) -> None:
        # epoch day 0 = 1970-01-01
        vec = vpb.Vector(
            fieldIdx=0,
            i64Vector=vpb.Vector.I64Vector(values=[0, 1, -1]),
        )
        result = read_column(vec, MillType.DATE, 3)
        assert result[0] == datetime.date(1970, 1, 1)
        assert result[1] == datetime.date(1970, 1, 2)
        assert result[2] == datetime.date(1969, 12, 31)


class TestTimeReader:
    def test_nanos(self) -> None:
        # 13:10:08.238049834
        nanos = (13 * 3600 + 10 * 60 + 8) * 1_000_000_000 + 238_049_834
        vec = vpb.Vector(
            fieldIdx=0,
            i64Vector=vpb.Vector.I64Vector(values=[nanos]),
        )
        result = read_column(vec, MillType.TIME, 1)
        # Python truncates to microseconds
        assert result[0] == datetime.time(13, 10, 8, 238_049)

    def test_midnight(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            i64Vector=vpb.Vector.I64Vector(values=[0]),
        )
        result = read_column(vec, MillType.TIME, 1)
        assert result[0] == datetime.time(0, 0, 0, 0)


class TestTimestampReader:
    def test_epoch(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            i64Vector=vpb.Vector.I64Vector(values=[0]),
        )
        result = read_column(vec, MillType.TIMESTAMP, 1)
        assert result[0] == datetime.datetime(1970, 1, 1)
        assert result[0].tzinfo is None

    def test_positive_millis(self) -> None:
        # 2012-10-03 14:12:45.345 = specific epoch millis
        dt = datetime.datetime(2012, 10, 3, 14, 12, 45, 345_000)
        millis = int((dt - datetime.datetime(1970, 1, 1)).total_seconds() * 1000)
        vec = vpb.Vector(
            fieldIdx=0,
            i64Vector=vpb.Vector.I64Vector(values=[millis]),
        )
        result = read_column(vec, MillType.TIMESTAMP, 1)
        assert result[0] == dt


class TestTimestampTzReader:
    def test_epoch(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            i64Vector=vpb.Vector.I64Vector(values=[0]),
        )
        result = read_column(vec, MillType.TIMESTAMP_TZ, 1)
        assert result[0] == datetime.datetime(1970, 1, 1, tzinfo=datetime.timezone.utc)
        assert result[0].tzinfo is datetime.timezone.utc


class TestIntervalDayReader:
    def test_days(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            i32Vector=vpb.Vector.I32Vector(values=[0, 7, -30]),
        )
        result = read_column(vec, MillType.INTERVAL_DAY, 3)
        assert result == [
            datetime.timedelta(days=0),
            datetime.timedelta(days=7),
            datetime.timedelta(days=-30),
        ]


class TestIntervalYearReader:
    def test_years(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            i32Vector=vpb.Vector.I32Vector(values=[12, 0, -6]),
        )
        result = read_column(vec, MillType.INTERVAL_YEAR, 3)
        assert result == [12, 0, -6]


# ---------------------------------------------------------------------------
# Null handling
# ---------------------------------------------------------------------------

class TestNullHandling:
    def test_nulls_vector_marks_none(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            i32Vector=vpb.Vector.I32Vector(values=[10, 0, 30]),
            nulls=vpb.Vector.NullsVector(nulls=[False, True, False]),
        )
        result = read_column(vec, MillType.INT, 3)
        assert result == [10, None, 30]

    def test_no_nulls_vector(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            i32Vector=vpb.Vector.I32Vector(values=[1, 2, 3]),
        )
        result = read_column(vec, MillType.INT, 3)
        assert result == [1, 2, 3]

    def test_all_nulls(self) -> None:
        vec = vpb.Vector(
            fieldIdx=0,
            i32Vector=vpb.Vector.I32Vector(values=[0, 0, 0]),
            nulls=vpb.Vector.NullsVector(nulls=[True, True, True]),
        )
        result = read_column(vec, MillType.INT, 3)
        assert result == [None, None, None]


# ---------------------------------------------------------------------------
# read_vector_block with synthetic data
# ---------------------------------------------------------------------------

class TestReadVectorBlock:
    def test_simple_two_column_block(self) -> None:
        block = _make_vector_block(
            fields=[("id", 0, 3), ("name", 1, 12)],
            vectors=[
                vpb.Vector(
                    fieldIdx=0,
                    i32Vector=vpb.Vector.I32Vector(values=[1, 2, 3]),
                ),
                vpb.Vector(
                    fieldIdx=1,
                    stringVector=vpb.Vector.StringVector(
                        values=["Alice", "Bob", "Charlie"]
                    ),
                ),
            ],
            size=3,
        )
        fields, rows = read_vector_block(block)
        assert len(fields) == 2
        assert fields[0].name == "id"
        assert fields[1].name == "name"
        assert len(rows) == 3
        assert rows[0] == {"id": 1, "name": "Alice"}
        assert rows[2] == {"id": 3, "name": "Charlie"}

    def test_empty_block(self) -> None:
        block = _make_vector_block(
            fields=[("id", 0, 3)],
            vectors=[
                vpb.Vector(
                    fieldIdx=0,
                    i32Vector=vpb.Vector.I32Vector(values=[]),
                ),
            ],
            size=0,
        )
        fields, rows = read_vector_block(block)
        assert len(fields) == 1
        assert rows == []

    def test_nulls_in_block(self) -> None:
        block = _make_vector_block(
            fields=[("val", 0, 3)],
            vectors=[
                vpb.Vector(
                    fieldIdx=0,
                    i32Vector=vpb.Vector.I32Vector(values=[10, 0]),
                    nulls=vpb.Vector.NullsVector(nulls=[False, True]),
                ),
            ],
            size=2,
        )
        _, rows = read_vector_block(block)
        assert rows[0]["val"] == 10
        assert rows[1]["val"] is None

    def test_missing_vector_produces_nulls(self) -> None:
        """A field with no corresponding vector produces all None values."""
        block = _make_vector_block(
            fields=[("id", 0, 3), ("ghost", 1, 12)],
            vectors=[
                vpb.Vector(
                    fieldIdx=0,
                    i32Vector=vpb.Vector.I32Vector(values=[1]),
                ),
                # No vector for fieldIdx=1
            ],
            size=1,
        )
        # Manually adjust so only the first vector exists for field 0
        # (the helper creates 2 vectors but we want to test missing)
        block2 = vpb.VectorBlock(
            schema=block.schema,
            vectorSize=1,
            vectors=[block.vectors[0]],
        )
        _, rows = read_vector_block(block2)
        assert rows[0] == {"id": 1, "ghost": None}


# ---------------------------------------------------------------------------
# Binary fixture integration (test/messages/logical-types.bin + .ref)
# ---------------------------------------------------------------------------

@pytest.mark.skipif(
    not _BIN_PATH.exists(), reason="Binary fixture not found"
)
class TestBinaryFixture:
    """End-to-end test against the shared logical-types binary fixture.

    Compares ``read_vector_block()`` output against the ``.ref`` companion.
    """

    @pytest.fixture(scope="class")
    def fixture_data(self) -> tuple[list[Any], list[dict[str, Any]]]:
        vb = _load_vector_block()
        return read_vector_block(vb)

    @pytest.fixture(scope="class")
    def reference(self) -> dict[str, list[dict[str, Any]]]:
        return _load_reference()

    _FIELD_NAMES = [
        "BigInt", "Binary", "Boolean", "Date", "Double", "Float",
        "Int", "SmallInt", "String", "Time", "Timestamp", "TimestampTZ",
        "TinyInt", "GUId",
    ]

    @pytest.mark.parametrize("field_name", _FIELD_NAMES)
    def test_field_values_match_ref(
        self,
        field_name: str,
        fixture_data: tuple[list[Any], list[dict[str, Any]]],
        reference: dict[str, list[dict[str, Any]]],
    ) -> None:
        _, rows = fixture_data
        refs = reference[field_name]

        for ref_entry in refs:
            idx = ref_entry["idx"]
            expected = _parse_ref_value(ref_entry)
            actual = rows[idx][field_name]

            if expected is None:
                assert actual is None, (
                    f"{field_name}[{idx}]: expected None, got {actual!r}"
                )
            elif isinstance(expected, float):
                assert actual == pytest.approx(expected, rel=1e-5), (
                    f"{field_name}[{idx}]: {expected!r} != {actual!r}"
                )
            else:
                assert actual == expected, (
                    f"{field_name}[{idx}]: {expected!r} != {actual!r}"
                )

    def test_row_count(
        self,
        fixture_data: tuple[list[Any], list[dict[str, Any]]],
    ) -> None:
        _, rows = fixture_data
        assert len(rows) == 5

    def test_field_count(
        self,
        fixture_data: tuple[list[Any], list[dict[str, Any]]],
    ) -> None:
        fields, _ = fixture_data
        assert len(fields) == 14
