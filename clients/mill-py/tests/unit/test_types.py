"""Tests for mill.types — MillType enum, MillField, MillTable, MillSchema."""
from __future__ import annotations

import datetime
import uuid

import pytest

from mill._proto import common_pb2 as pb
from mill.types import MillField, MillSchema, MillTable, MillType, TableType


# ---------------------------------------------------------------------------
# MillType enum
# ---------------------------------------------------------------------------

class TestMillType:
    """Verify MillType enum members, metadata, and from_proto conversion."""

    EXPECTED_MEMBERS = [
        ("TINY_INT",      1,  "i32Vector",    int),
        ("SMALL_INT",     2,  "i32Vector",    int),
        ("INT",           3,  "i32Vector",    int),
        ("BIG_INT",       4,  "i64Vector",    int),
        ("BINARY",        5,  "byteVector",   bytes),
        ("BOOL",          6,  "boolVector",   bool),
        ("DATE",          7,  "i64Vector",    datetime.date),
        ("FLOAT",         8,  "fp32Vector",   float),
        ("DOUBLE",        9,  "fp64Vector",   float),
        ("INTERVAL_DAY",  10, "i32Vector",    datetime.timedelta),
        ("INTERVAL_YEAR", 11, "i32Vector",    int),
        ("STRING",        12, "stringVector", str),
        ("TIMESTAMP",     13, "i64Vector",    datetime.datetime),
        ("TIMESTAMP_TZ",  14, "i64Vector",    datetime.datetime),
        ("TIME",          15, "i64Vector",    datetime.time),
        ("UUID",          16, "byteVector",   uuid.UUID),
    ]

    def test_has_16_members(self) -> None:
        assert len(MillType) == 16

    @pytest.mark.parametrize(
        "name, proto_id, vector_field, python_type",
        EXPECTED_MEMBERS,
        ids=[m[0] for m in EXPECTED_MEMBERS],
    )
    def test_member_metadata(
        self, name: str, proto_id: int, vector_field: str, python_type: type,
    ) -> None:
        member = MillType[name]
        assert member.proto_id == proto_id
        assert member.vector_field == vector_field
        assert member.python_type is python_type

    @pytest.mark.parametrize(
        "name, proto_id, vector_field, python_type",
        EXPECTED_MEMBERS,
        ids=[m[0] for m in EXPECTED_MEMBERS],
    )
    def test_from_proto_int(
        self, name: str, proto_id: int, vector_field: str, python_type: type,
    ) -> None:
        assert MillType.from_proto(proto_id) is MillType[name]

    @pytest.mark.parametrize(
        "name, proto_id, vector_field, python_type",
        EXPECTED_MEMBERS,
        ids=[m[0] for m in EXPECTED_MEMBERS],
    )
    def test_from_proto_enum_value(
        self, name: str, proto_id: int, vector_field: str, python_type: type,
    ) -> None:
        proto_enum_val = pb.LogicalDataType.LogicalDataTypeId.Value(name)
        assert MillType.from_proto(proto_enum_val) is MillType[name]

    def test_from_proto_zero_raises(self) -> None:
        with pytest.raises(ValueError, match="Unknown LogicalDataTypeId"):
            MillType.from_proto(0)

    def test_from_proto_unknown_raises(self) -> None:
        with pytest.raises(ValueError, match="Unknown LogicalDataTypeId"):
            MillType.from_proto(999)

    def test_value_equals_proto_id(self) -> None:
        for t in MillType:
            assert t.value == t.proto_id


# ---------------------------------------------------------------------------
# MillField
# ---------------------------------------------------------------------------

class TestMillField:
    """Verify MillField creation and from_proto() factory."""

    def _make_proto_field(
        self,
        name: str = "col",
        idx: int = 0,
        type_id: int = 3,  # INT
        nullability: int = 1,  # NULL
        precision: int = 0,
        scale: int = 0,
    ) -> pb.Field:
        return pb.Field(
            name=name,
            fieldIdx=idx,
            type=pb.DataType(
                type=pb.LogicalDataType(
                    typeId=type_id,
                    precision=precision,
                    scale=scale,
                ),
                nullability=nullability,
            ),
        )

    def test_from_proto_basic(self) -> None:
        f = MillField.from_proto(self._make_proto_field("age", 2, 3))
        assert f.name == "age"
        assert f.index == 2
        assert f.type is MillType.INT
        assert f.nullable is True
        assert f.precision == 0
        assert f.scale == 0

    def test_from_proto_not_null(self) -> None:
        f = MillField.from_proto(
            self._make_proto_field("id", 0, 4, nullability=2)  # NOT_NULL
        )
        assert f.nullable is False

    def test_from_proto_precision_scale(self) -> None:
        f = MillField.from_proto(
            self._make_proto_field("price", 1, 9, precision=10, scale=2)
        )
        assert f.precision == 10
        assert f.scale == 2

    def test_frozen(self) -> None:
        f = MillField.from_proto(self._make_proto_field())
        with pytest.raises(AttributeError):
            f.name = "other"  # type: ignore[misc]

    @pytest.mark.parametrize("type_id", range(1, 17))
    def test_all_type_ids_convertible(self, type_id: int) -> None:
        f = MillField.from_proto(self._make_proto_field(type_id=type_id))
        assert f.type.proto_id == type_id


# ---------------------------------------------------------------------------
# TableType
# ---------------------------------------------------------------------------

class TestTableType:
    def test_from_proto_table(self) -> None:
        assert TableType.from_proto(1) is TableType.TABLE

    def test_from_proto_view(self) -> None:
        assert TableType.from_proto(2) is TableType.VIEW

    def test_from_proto_not_specified(self) -> None:
        assert TableType.from_proto(0) is TableType.NOT_SPECIFIED

    def test_from_proto_unknown_falls_back(self) -> None:
        assert TableType.from_proto(99) is TableType.NOT_SPECIFIED


# ---------------------------------------------------------------------------
# MillTable
# ---------------------------------------------------------------------------

class TestMillTable:
    def _make_proto_table(self) -> pb.Table:
        return pb.Table(
            schemaName="MONETA",
            name="CLIENTS",
            tableType=pb.Table.TABLE,
            fields=[
                pb.Field(
                    name="id",
                    fieldIdx=0,
                    type=pb.DataType(
                        type=pb.LogicalDataType(typeId=3),
                        nullability=pb.DataType.NOT_NULL,
                    ),
                ),
                pb.Field(
                    name="name",
                    fieldIdx=1,
                    type=pb.DataType(
                        type=pb.LogicalDataType(typeId=12),
                        nullability=pb.DataType.NULL,
                    ),
                ),
            ],
        )

    def test_from_proto(self) -> None:
        tbl = MillTable.from_proto(self._make_proto_table())
        assert tbl.name == "CLIENTS"
        assert tbl.schema_name == "MONETA"
        assert tbl.table_type is TableType.TABLE
        assert len(tbl.fields) == 2
        assert tbl.fields[0].name == "id"
        assert tbl.fields[1].type is MillType.STRING

    def test_frozen(self) -> None:
        tbl = MillTable.from_proto(self._make_proto_table())
        with pytest.raises(AttributeError):
            tbl.name = "X"  # type: ignore[misc]


# ---------------------------------------------------------------------------
# MillSchema
# ---------------------------------------------------------------------------

class TestMillSchema:
    def test_from_proto_empty(self) -> None:
        schema = MillSchema.from_proto(pb.Schema())
        assert schema.tables == ()

    def test_from_proto_with_tables(self) -> None:
        proto = pb.Schema(
            tables=[
                pb.Table(schemaName="S", name="T1", tableType=pb.Table.TABLE),
                pb.Table(schemaName="S", name="T2", tableType=pb.Table.VIEW),
            ],
        )
        schema = MillSchema.from_proto(proto)
        assert len(schema.tables) == 2
        assert schema.tables[0].name == "T1"
        assert schema.tables[1].table_type is TableType.VIEW
