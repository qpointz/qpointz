"""Mill type system — MillType enum, MillField, MillTable, MillSchema.

Pythonic wrappers around protobuf schema types. Users interact with these
classes instead of raw proto messages.
"""
from __future__ import annotations

import datetime
import enum
import uuid
from dataclasses import dataclass, field
from typing import Any, Sequence

from mill._proto import common_pb2 as _pb


# ---------------------------------------------------------------------------
# MillType enum  (WI 2.1)
# ---------------------------------------------------------------------------

class MillType(enum.Enum):
    """Enumeration of the 16 Mill logical types.

    Each member carries metadata about its physical vector representation
    and the corresponding Python native type.

    Attributes:
        proto_id: The ``LogicalDataTypeId`` protobuf enum value.
        vector_field: Name of the ``oneof`` field on ``Vector`` that carries
            this type's data (e.g. ``"i32Vector"``).
        python_type: The Python type that native values are converted to.

    Example::

        >>> MillType.INT.python_type
        <class 'int'>
        >>> MillType.from_proto(3)  # LogicalDataTypeId.INT == 3
        <MillType.INT: 3>
    """

    # (proto_id, vector_field, python_type)
    TINY_INT     = (1,  "i32Vector",    int)
    SMALL_INT    = (2,  "i32Vector",    int)
    INT          = (3,  "i32Vector",    int)
    BIG_INT      = (4,  "i64Vector",    int)
    BINARY       = (5,  "byteVector",   bytes)
    BOOL         = (6,  "boolVector",   bool)
    DATE         = (7,  "i64Vector",    datetime.date)
    FLOAT        = (8,  "fp32Vector",   float)
    DOUBLE       = (9,  "fp64Vector",   float)
    INTERVAL_DAY = (10, "i32Vector",    datetime.timedelta)
    INTERVAL_YEAR = (11, "i32Vector",   int)
    STRING       = (12, "stringVector", str)
    TIMESTAMP    = (13, "i64Vector",    datetime.datetime)
    TIMESTAMP_TZ = (14, "i64Vector",    datetime.datetime)
    TIME         = (15, "i64Vector",    datetime.time)
    UUID         = (16, "byteVector",   uuid.UUID)

    def __new__(cls, proto_id: int, vector_field: str, python_type: type) -> MillType:
        obj = object.__new__(cls)
        obj._value_ = proto_id
        return obj

    def __init__(self, proto_id: int, vector_field: str, python_type: type) -> None:
        self.proto_id: int = proto_id
        self.vector_field: str = vector_field
        self.python_type: type = python_type

    @classmethod
    def from_proto(cls, type_id: int | _pb.LogicalDataType.LogicalDataTypeId) -> MillType:
        """Convert a protobuf ``LogicalDataTypeId`` value to a ``MillType``.

        Args:
            type_id: Integer or proto enum member.

        Returns:
            The corresponding ``MillType`` member.

        Raises:
            ValueError: If *type_id* is ``0`` (``NOT_SPECIFIED_TYPE``) or
                does not map to any known member.
        """
        int_val = int(type_id)
        try:
            return cls(int_val)
        except ValueError:
            raise ValueError(
                f"Unknown LogicalDataTypeId: {type_id!r}"
            ) from None


# Pre-computed reverse lookup {proto_id -> MillType} for fast access
_PROTO_ID_TO_TYPE: dict[int, MillType] = {t.proto_id: t for t in MillType}


# ---------------------------------------------------------------------------
# MillField  (WI 2.2)
# ---------------------------------------------------------------------------

@dataclass(frozen=True, slots=True)
class MillField:
    """A single field (column) in a Mill schema.

    Attributes:
        name: Column name.
        index: Zero-based ordinal position in the schema.
        type: The Mill logical type of this field.
        nullable: Whether the field may contain ``None`` values.
        precision: Type precision (e.g. for ``DECIMAL``). ``0`` when unused.
        scale: Type scale. ``0`` when unused.

    Example::

        >>> f = MillField.from_proto(proto_field)
        >>> f.name
        'client_id'
        >>> f.type
        <MillType.INT: 3>
    """

    name: str
    index: int
    type: MillType
    nullable: bool = True
    precision: int = 0
    scale: int = 0

    @classmethod
    def from_proto(cls, proto: _pb.Field) -> MillField:
        """Create a ``MillField`` from a protobuf ``Field`` message.

        Args:
            proto: A ``Field`` proto message.

        Returns:
            A new ``MillField`` instance.
        """
        dt = proto.type  # DataType message
        nullable = dt.nullability != _pb.DataType.NOT_NULL
        logical = dt.type  # LogicalDataType message
        return cls(
            name=proto.name,
            index=proto.fieldIdx,
            type=MillType.from_proto(logical.typeId),
            nullable=nullable,
            precision=logical.precision,
            scale=logical.scale,
        )


# ---------------------------------------------------------------------------
# MillTable / MillSchema  (WI 2.3)
# ---------------------------------------------------------------------------

class TableType(enum.Enum):
    """Type of a database table object.

    Maps to ``Table.TableTypeId`` in the protobuf definition.
    """

    NOT_SPECIFIED = 0
    TABLE = 1
    VIEW = 2

    @classmethod
    def from_proto(cls, proto_val: int | _pb.Table.TableTypeId) -> TableType:
        """Convert a proto ``TableTypeId`` to a ``TableType``.

        Args:
            proto_val: Integer or proto enum member.

        Returns:
            The corresponding ``TableType``.
        """
        try:
            return cls(int(proto_val))
        except ValueError:
            return cls.NOT_SPECIFIED


@dataclass(frozen=True, slots=True)
class MillTable:
    """A table (or view) in a Mill schema.

    Attributes:
        name: Table name.
        schema_name: Owning schema name.
        table_type: Whether this is a ``TABLE`` or ``VIEW``.
        fields: Ordered list of columns.

    Example::

        >>> tbl = MillTable.from_proto(proto_table)
        >>> [f.name for f in tbl.fields]
        ['client_id', 'first_name', 'last_name']
    """

    name: str
    schema_name: str
    table_type: TableType = TableType.NOT_SPECIFIED
    fields: tuple[MillField, ...] = ()

    @classmethod
    def from_proto(cls, proto: _pb.Table) -> MillTable:
        """Create a ``MillTable`` from a protobuf ``Table`` message.

        Args:
            proto: A ``Table`` proto message.

        Returns:
            A new ``MillTable`` instance.
        """
        return cls(
            name=proto.name,
            schema_name=proto.schemaName,
            table_type=TableType.from_proto(proto.tableType),
            fields=tuple(MillField.from_proto(f) for f in proto.fields),
        )


@dataclass(frozen=True, slots=True)
class MillSchema:
    """A collection of tables returned by the Mill service.

    Attributes:
        tables: Ordered sequence of tables in this schema.

    Example::

        >>> schema = MillSchema.from_proto(proto_schema)
        >>> schema.tables[0].name
        'CLIENTS'
    """

    tables: tuple[MillTable, ...] = ()

    @classmethod
    def from_proto(cls, proto: _pb.Schema) -> MillSchema:
        """Create a ``MillSchema`` from a protobuf ``Schema`` message.

        Args:
            proto: A ``Schema`` proto message.

        Returns:
            A new ``MillSchema`` instance.
        """
        return cls(
            tables=tuple(MillTable.from_proto(t) for t in proto.tables),
        )
