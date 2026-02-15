"""Pandas extra — ``ResultSet`` to ``pandas.DataFrame``.

Thin wrapper on top of the Arrow conversion.  Uses
``pyarrow.Table.to_pandas()`` so that Arrow-backed columns are available
with ``types_mapper=pd.ArrowDtype`` on pandas 2.x.

Requires the ``pandas`` extra: ``pip install mill-py[pandas]``.
"""
from __future__ import annotations

from typing import TYPE_CHECKING

import pandas as pd

from mill.extras.arrow import result_to_arrow

if TYPE_CHECKING:
    from mill.result import ResultSet


def result_to_pandas(result: ResultSet) -> pd.DataFrame:
    """Convert a :class:`~mill.result.ResultSet` to a ``pandas.DataFrame``.

    Internally calls :func:`~mill.extras.arrow.result_to_arrow` to build
    a ``pyarrow.Table``, then converts to pandas via ``to_pandas()``.

    Args:
        result: A ``ResultSet`` instance.

    Returns:
        A ``pandas.DataFrame``.
    """
    table = result_to_arrow(result)
    return table.to_pandas(types_mapper=pd.ArrowDtype)
