"""Vector readers — VectorBlock to Python native values.

Converts protobuf ``VectorBlock`` messages into Python-native row dicts
for all 16 Mill logical types.
"""
from __future__ import annotations

import datetime
import uuid
from typing import Any, Callable, Sequence

from mill._proto import common_pb2 as _cpb
from mill._proto import vector_pb2 as _vpb
from mill.types import MillField, MillType

# ---------------------------------------------------------------------------
# Epoch reference constants
# ---------------------------------------------------------------------------
_EPOCH_DATE = datetime.date(1970, 1, 1)
_EPOCH_ORDINAL = _EPOCH_DATE.toordinal()
_EPOCH_DATETIME = datetime.datetime(1970, 1, 1, tzinfo=datetime.timezone.utc)
_EPOCH_DATETIME_NAIVE = datetime.datetime(1970, 1, 1)

# ---------------------------------------------------------------------------
# Per-type reader functions  (WI 2.4)
#
# Each reader takes the raw repeated-scalar values from the appropriate
# vector field and an index, returning the native Python value.
# ---------------------------------------------------------------------------

# Type alias for a reader: (raw_values_sequence, index) -> native_value
Reader = Callable[[Any, int], Any]


def _read_int(values: Sequence[int], idx: int) -> int:
    """Read an integer value (TINY_INT, SMALL_INT, INT, INTERVAL_YEAR)."""
    return int(values[idx])


def _read_bigint(values: Sequence[int], idx: int) -> int:
    """Read a 64-bit integer value (BIG_INT)."""
    return int(values[idx])


def _read_bool(values: Sequence[bool], idx: int) -> bool:
    """Read a boolean value."""
    return bool(values[idx])


def _read_float(values: Sequence[float], idx: int) -> float:
    """Read a 32-bit floating-point value."""
    return float(values[idx])


def _read_double(values: Sequence[float], idx: int) -> float:
    """Read a 64-bit floating-point value."""
    return float(values[idx])


def _read_string(values: Sequence[str], idx: int) -> str:
    """Read a string value."""
    return str(values[idx])


def _read_binary(values: Sequence[bytes], idx: int) -> bytes:
    """Read a binary (bytes) value."""
    return bytes(values[idx])


def _read_uuid(values: Sequence[bytes], idx: int) -> uuid.UUID:
    """Read a UUID from 16-byte binary representation."""
    return uuid.UUID(bytes=bytes(values[idx]))


def _read_date(values: Sequence[int], idx: int) -> datetime.date:
    """Read a date from epoch-day count.

    Args:
        values: Sequence of ``int64`` epoch-day values.
        idx: Row index.

    Returns:
        A ``datetime.date``.
    """
    return datetime.date.fromordinal(_EPOCH_ORDINAL + int(values[idx]))


def _read_time(values: Sequence[int], idx: int) -> datetime.time:
    """Read a time from nanoseconds-since-midnight.

    Args:
        values: Sequence of ``int64`` nanosecond values.
        idx: Row index.

    Returns:
        A ``datetime.time`` (microsecond precision — Python ``time`` does not
        support nanoseconds natively; truncated to microseconds).
    """
    nanos = int(values[idx])
    total_micros = nanos // 1_000
    hours, remainder = divmod(total_micros, 3_600_000_000)
    minutes, remainder = divmod(remainder, 60_000_000)
    seconds, micros = divmod(remainder, 1_000_000)
    return datetime.time(hours, minutes, seconds, micros)


def _read_timestamp(values: Sequence[int], idx: int) -> datetime.datetime:
    """Read a naive timestamp from epoch milliseconds.

    Args:
        values: Sequence of ``int64`` epoch-millisecond values.
        idx: Row index.

    Returns:
        A naive ``datetime.datetime`` (no timezone).
    """
    millis = int(values[idx])
    seconds, remainder_ms = divmod(millis, 1_000)
    return _EPOCH_DATETIME_NAIVE + datetime.timedelta(
        seconds=seconds, milliseconds=remainder_ms,
    )


def _read_timestamp_tz(values: Sequence[int], idx: int) -> datetime.datetime:
    """Read a timezone-aware timestamp from epoch milliseconds.

    Args:
        values: Sequence of ``int64`` epoch-millisecond values.
        idx: Row index.

    Returns:
        A ``datetime.datetime`` with ``tzinfo=datetime.timezone.utc``.
    """
    millis = int(values[idx])
    seconds, remainder_ms = divmod(millis, 1_000)
    return _EPOCH_DATETIME + datetime.timedelta(
        seconds=seconds, milliseconds=remainder_ms,
    )


def _read_interval_day(values: Sequence[int], idx: int) -> datetime.timedelta:
    """Read an interval-day from a day count.

    Args:
        values: Sequence of ``int32`` day-count values.
        idx: Row index.

    Returns:
        A ``datetime.timedelta``.
    """
    return datetime.timedelta(days=int(values[idx]))


def _read_interval_year(values: Sequence[int], idx: int) -> int:
    """Read an interval-year as a plain integer (month count).

    There is no stdlib type for year intervals, so the raw integer is
    returned (representing months or years depending on the server
    encoding).

    Args:
        values: Sequence of ``int32`` values.
        idx: Row index.

    Returns:
        Integer value.
    """
    return int(values[idx])


# ---------------------------------------------------------------------------
# Reader registry  (WI 2.4)
# ---------------------------------------------------------------------------

_READERS: dict[MillType, Reader] = {
    MillType.TINY_INT:      _read_int,
    MillType.SMALL_INT:     _read_int,
    MillType.INT:           _read_int,
    MillType.BIG_INT:       _read_bigint,
    MillType.BINARY:        _read_binary,
    MillType.BOOL:          _read_bool,
    MillType.DATE:          _read_date,
    MillType.FLOAT:         _read_float,
    MillType.DOUBLE:        _read_double,
    MillType.INTERVAL_DAY:  _read_interval_day,
    MillType.INTERVAL_YEAR: _read_interval_year,
    MillType.STRING:        _read_string,
    MillType.TIMESTAMP:     _read_timestamp,
    MillType.TIMESTAMP_TZ:  _read_timestamp_tz,
    MillType.TIME:          _read_time,
    MillType.UUID:          _read_uuid,
}


def get_reader(mill_type: MillType) -> Reader:
    """Return the native-value reader for *mill_type*.

    Args:
        mill_type: A ``MillType`` member.

    Returns:
        A callable ``(values, index) -> native_value``.

    Raises:
        KeyError: If no reader is registered for *mill_type*.
    """
    return _READERS[mill_type]


# ---------------------------------------------------------------------------
# read_vector  (WI 2.4 helper)
# ---------------------------------------------------------------------------

def _get_vector_values(vector: _vpb.Vector, mill_type: MillType) -> Any:
    """Extract the raw repeated values from a ``Vector`` message.

    Args:
        vector: A proto ``Vector`` message.
        mill_type: The logical type which determines which ``oneof`` field
            to access.

    Returns:
        The ``values`` repeated field from the appropriate sub-vector.

    Raises:
        ValueError: If the expected vector field is not set.
    """
    field_name = mill_type.vector_field
    sub_vector = getattr(vector, field_name, None)
    if sub_vector is None:
        raise ValueError(
            f"Vector does not have field {field_name!r} for type {mill_type.name}"
        )
    return sub_vector.values


def read_column(
    vector: _vpb.Vector,
    mill_type: MillType,
    size: int,
) -> list[Any | None]:
    """Read an entire column from a ``Vector`` into a list of native values.

    Handles nulls via the ``Vector.nulls`` sub-message.

    Args:
        vector: A proto ``Vector`` message.
        mill_type: Logical type of this column.
        size: Number of rows in the vector block.

    Returns:
        A list of length *size* with native Python values (or ``None``
        for null entries).
    """
    raw_values = _get_vector_values(vector, mill_type)
    reader = get_reader(mill_type)

    # Nulls vector: if present and non-empty, True means the value is null
    nulls = vector.nulls.nulls if vector.HasField("nulls") else ()

    result: list[Any | None] = []
    for i in range(size):
        if nulls and i < len(nulls) and nulls[i]:
            result.append(None)
        else:
            result.append(reader(raw_values, i))
    return result


# ---------------------------------------------------------------------------
# read_vector_block  (WI 2.5)
# ---------------------------------------------------------------------------

def read_vector_block(
    block: _vpb.VectorBlock,
) -> tuple[list[MillField], list[dict[str, Any]]]:
    """Read a protobuf ``VectorBlock`` into field metadata and row dicts.

    This is the main entry point for converting wire-format data into
    Python-native structures.

    Args:
        block: A ``VectorBlock`` proto message.

    Returns:
        A 2-tuple:
          - ``fields``: list of ``MillField`` describing each column.
          - ``rows``: list of ``dict[str, Any]`` where each dict maps
            column name to native value (or ``None``).

    Example::

        >>> fields, rows = read_vector_block(proto_block)
        >>> rows[0]
        {'client_id': 42, 'name': 'Alice'}
    """
    size = block.vectorSize
    schema = block.schema

    # Build field metadata
    fields = [MillField.from_proto(f) for f in schema.fields]

    # Index vectors by fieldIdx for fast lookup.
    # Some fixtures / older encodings leave fieldIdx=0 on all vectors
    # (proto3 default).  Detect this and fall back to positional ordering.
    all_zero = all(v.fieldIdx == 0 for v in block.vectors) and len(block.vectors) > 1
    if all_zero:
        # Positional: vectors[i] corresponds to fields[i]
        vectors_by_idx: dict[int, _vpb.Vector] = {
            mf.index: block.vectors[i]
            for i, mf in enumerate(fields)
            if i < len(block.vectors)
        }
    else:
        vectors_by_idx = {v.fieldIdx: v for v in block.vectors}

    # Read each column into a list
    columns: dict[str, list[Any | None]] = {}
    for mf in fields:
        vec = vectors_by_idx.get(mf.index)
        if vec is None:
            # Missing vector → all nulls
            columns[mf.name] = [None] * size
        else:
            columns[mf.name] = read_column(vec, mf.type, size)

    # Pivot columns into rows
    rows: list[dict[str, Any]] = []
    for i in range(size):
        rows.append({name: col[i] for name, col in columns.items()})

    return fields, rows
