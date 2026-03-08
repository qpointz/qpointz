"""Mill-to-ibis type conversions."""
from __future__ import annotations

from mill.types import MillField, MillType

try:
    import ibis.expr.datatypes as dt
except ImportError as exc:  # pragma: no cover - exercised when optional dep missing
    raise ImportError(
        "ibis integration requires optional dependency 'ibis-framework'. "
        "Install with: pip install qpointz-mill-py[ibis]"
    ) from exc


_MILL_TO_IBIS: dict[MillType, dt.DataType] = {
    MillType.TINY_INT: dt.int8,
    MillType.SMALL_INT: dt.int16,
    MillType.INT: dt.int32,
    MillType.BIG_INT: dt.int64,
    MillType.BINARY: dt.binary,
    MillType.BOOL: dt.boolean,
    MillType.DATE: dt.date,
    MillType.FLOAT: dt.float32,
    MillType.DOUBLE: dt.float64,
    MillType.INTERVAL_DAY: dt.Interval(unit="D"),
    MillType.INTERVAL_YEAR: dt.Interval(unit="Y"),
    MillType.STRING: dt.string,
    MillType.TIMESTAMP: dt.timestamp,
    MillType.TIMESTAMP_TZ: dt.Timestamp(timezone="UTC"),
    MillType.TIME: dt.time,
    MillType.UUID: dt.uuid,
}


def to_ibis_dtype(field: MillField) -> dt.DataType:
    """Convert a Mill field definition to an ibis datatype."""
    dtype = _MILL_TO_IBIS.get(field.type, dt.string)
    if field.nullable:
        return dtype.copy(nullable=True)
    return dtype.copy(nullable=False)
