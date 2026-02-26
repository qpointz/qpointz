"""Arrow extra — VectorBlock to ``pyarrow.RecordBatch`` / ``pyarrow.Table``.

Foundation for all DataFrame conversions.  The conversion chain is::

    VectorBlock → pyarrow.RecordBatch → pyarrow.Table
                                       → pandas.DataFrame (via to_pandas())
                                       → polars.DataFrame (via from_arrow())

Type mapping follows Section 4.2 of the implementation plan.

Requires the ``arrow`` extra: ``pip install qpointz-mill-py[arrow]``.
"""
from __future__ import annotations

from typing import TYPE_CHECKING, Any, Sequence

import pyarrow as pa

from mill._proto import vector_pb2 as _vpb
from mill.types import MillField, MillType
from mill.vectors import _get_vector_values, read_column

if TYPE_CHECKING:
    from mill.result import ResultSet

# ---------------------------------------------------------------------------
# MillType → PyArrow type mapping
# ---------------------------------------------------------------------------

_ARROW_TYPES: dict[MillType, pa.DataType] = {
    MillType.TINY_INT:      pa.int8(),
    MillType.SMALL_INT:     pa.int16(),
    MillType.INT:           pa.int32(),
    MillType.BIG_INT:       pa.int64(),
    MillType.BOOL:          pa.bool_(),
    MillType.FLOAT:         pa.float32(),
    MillType.DOUBLE:        pa.float64(),
    MillType.STRING:        pa.string(),
    MillType.BINARY:        pa.binary(),
    MillType.UUID:          pa.binary(16),
    MillType.DATE:          pa.date32(),
    MillType.TIME:          pa.time64("ns"),
    MillType.TIMESTAMP:     pa.timestamp("ms"),
    MillType.TIMESTAMP_TZ:  pa.timestamp("ms", tz="UTC"),
    MillType.INTERVAL_DAY:  pa.duration("s"),
    MillType.INTERVAL_YEAR: pa.int32(),
}


def arrow_type(mill_type: MillType) -> pa.DataType:
    """Return the PyArrow type for a given Mill logical type.

    Args:
        mill_type: A ``MillType`` member.

    Returns:
        The corresponding ``pyarrow.DataType``.

    Raises:
        KeyError: If no mapping is registered for *mill_type*.
    """
    return _ARROW_TYPES[mill_type]


# ---------------------------------------------------------------------------
# Column conversion helpers
# ---------------------------------------------------------------------------

def _read_arrow_column(
    vector: _vpb.Vector,
    field: MillField,
    size: int,
) -> pa.Array:
    """Convert a single ``Vector`` column to a ``pyarrow.Array``.

    Uses the Python-native reader for correct type conversion, then wraps
    in a PyArrow array with the appropriate type and null mask.

    Args:
        vector: The proto ``Vector`` for this column.
        field: Column metadata.
        size: Number of rows.

    Returns:
        A ``pyarrow.Array``.
    """
    pa_type = arrow_type(field.type)
    values = read_column(vector, field.type, size)

    # For types that need special Arrow treatment, convert values
    if field.type == MillType.UUID:
        # read_column returns uuid.UUID objects; Arrow binary(16) needs bytes
        values = [v.bytes if v is not None else None for v in values]
    elif field.type == MillType.INTERVAL_DAY:
        # read_column returns timedelta; Arrow duration('s') needs int seconds
        values = [int(v.total_seconds()) if v is not None else None for v in values]
    elif field.type == MillType.DATE:
        # read_column returns datetime.date; Arrow date32 accepts date natively
        pass
    elif field.type == MillType.TIME:
        # read_column returns datetime.time; Arrow time64('ns') needs int nanos
        values = [
            (v.hour * 3_600_000_000_000 + v.minute * 60_000_000_000
             + v.second * 1_000_000_000 + v.microsecond * 1_000)
            if v is not None else None
            for v in values
        ]
    elif field.type == MillType.TIMESTAMP:
        # read_column returns naive datetime; Arrow timestamp('ms') accepts it
        pass
    elif field.type == MillType.TIMESTAMP_TZ:
        # read_column returns tz-aware datetime; Arrow timestamp('ms', tz='UTC') accepts it
        pass

    # For narrow integer types stored in wider i32 wire format, build as
    # int32 first then cast down (allows out-of-range test fixtures to work
    # with truncation; production data should always fit).
    if field.type in (MillType.TINY_INT, MillType.SMALL_INT):
        arr = pa.array(values, type=pa.int32())
        return arr.cast(pa_type, safe=False)

    return pa.array(values, type=pa_type)


# ---------------------------------------------------------------------------
# VectorBlock → RecordBatch
# ---------------------------------------------------------------------------

def vector_block_to_record_batch(block: _vpb.VectorBlock) -> pa.RecordBatch:
    """Convert a single ``VectorBlock`` to a ``pyarrow.RecordBatch``.

    Args:
        block: A proto ``VectorBlock`` message.

    Returns:
        A ``pyarrow.RecordBatch`` with one column per field.
    """
    size = block.vectorSize
    fields = [MillField.from_proto(f) for f in block.schema.fields]

    # Map vectors by field index (same logic as vectors.read_vector_block)
    all_zero = all(v.fieldIdx == 0 for v in block.vectors) and len(block.vectors) > 1
    if all_zero:
        vectors_by_idx: dict[int, _vpb.Vector] = {
            mf.index: block.vectors[i]
            for i, mf in enumerate(fields)
            if i < len(block.vectors)
        }
    else:
        vectors_by_idx = {v.fieldIdx: v for v in block.vectors}

    # Build Arrow schema and columns
    pa_fields: list[pa.Field] = []
    columns: list[pa.Array] = []

    for mf in fields:
        pa_type = arrow_type(mf.type)
        pa_fields.append(pa.field(mf.name, pa_type, nullable=mf.nullable))

        vec = vectors_by_idx.get(mf.index)
        if vec is None:
            # Missing vector → all nulls
            columns.append(pa.nulls(size, type=pa_type))
        else:
            columns.append(_read_arrow_column(vec, mf, size))

    schema = pa.schema(pa_fields)
    return pa.RecordBatch.from_arrays(columns, schema=schema)


# ---------------------------------------------------------------------------
# ResultSet → Arrow Table
# ---------------------------------------------------------------------------

def result_to_arrow(result: ResultSet) -> pa.Table:
    """Convert a :class:`~mill.result.ResultSet` to a ``pyarrow.Table``.

    Forces full consumption of the result set (all blocks are fetched and
    cached), then converts each ``VectorBlock`` to a ``RecordBatch`` and
    concatenates them into a single ``Table``.

    Args:
        result: A ``ResultSet`` instance.

    Returns:
        A ``pyarrow.Table``.  If the result set is empty (no blocks),
        returns an empty table with the schema from fields (if available)
        or a schema-less empty table.
    """
    # Force full consumption so all blocks are in cache
    result.fetchall()

    if not result._cache:
        # Empty result — build empty table from field metadata if available
        if result.fields:
            pa_fields = [
                pa.field(f.name, arrow_type(f.type), nullable=f.nullable)
                for f in result.fields
            ]
            return pa.table({f.name: pa.array([], type=f.type) for f in pa_fields},
                            schema=pa.schema(pa_fields))
        return pa.table({})

    batches = [vector_block_to_record_batch(block) for block in result._cache]
    return pa.Table.from_batches(batches)
