"""Polars extra — ``ResultSet`` to ``polars.DataFrame``.

Thin wrapper on top of the Arrow conversion.  Uses
``polars.from_arrow()`` for near-zero-copy conversion from Arrow.

Requires the ``polars`` extra: ``pip install qpointz-mill-py[polars]``.
"""
from __future__ import annotations

from typing import TYPE_CHECKING

import polars as pl

from mill.extras.arrow import result_to_arrow

if TYPE_CHECKING:
    from mill.result import ResultSet


def result_to_polars(result: ResultSet) -> pl.DataFrame:
    """Convert a :class:`~mill.result.ResultSet` to a ``polars.DataFrame``.

    Internally calls :func:`~mill.extras.arrow.result_to_arrow` to build
    a ``pyarrow.Table``, then converts to polars via ``from_arrow()``.

    Args:
        result: A ``ResultSet`` instance.

    Returns:
        A ``polars.DataFrame``.
    """
    table = result_to_arrow(result)
    return pl.from_arrow(table)
