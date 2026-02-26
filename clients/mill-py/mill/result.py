"""ResultSet — lazy-with-cache result iteration.

A :class:`ResultSet` wraps the server's streaming response and provides
row-dict iteration, ``fetchall()``, and optional DataFrame conversion.
"""
from __future__ import annotations

from typing import Any, Iterator

from mill._proto import data_connect_svc_pb2 as _svc
from mill._proto import vector_pb2 as _vpb
from mill.types import MillField
from mill.vectors import read_vector_block


class ResultSet:
    """Lazy-with-cache result set over a stream of ``QueryResultResponse``.

    **Design contract** (from the implementation plan):

    - VectorBlocks are pulled from the transport on demand.
    - Once consumed, blocks are cached internally so that re-iteration
      replays cached data before continuing to pull new blocks.
    - ``fetchall()`` forces full consumption.
    - ``to_arrow()`` / ``to_pandas()`` / ``to_polars()`` delegate to extras
      via lazy imports.

    Args:
        source: An iterator of ``QueryResultResponse`` proto messages
            (typically from ``Transport.exec_query()``).

    Example::

        >>> rs = client.query("SELECT * FROM MONETA.CLIENTS")
        >>> for row in rs:
        ...     print(row["FIRST_NAME"])
        >>> # Re-iterate (replays cache + fetches remaining):
        >>> all_rows = rs.fetchall()
    """

    def __init__(self, source: Iterator[_svc.QueryResultResponse]) -> None:
        self._source = source
        self._cache: list[_vpb.VectorBlock] = []
        self._exhausted: bool = False
        self._fields: list[MillField] | None = None

    # -- properties --

    @property
    def fields(self) -> list[MillField] | None:
        """Column metadata from the first block, or ``None`` if no data yet.

        Accessing this property does **not** force a fetch — it returns
        whatever is known from blocks already consumed.
        """
        return self._fields

    # -- internal helpers --

    def _consume_next(self) -> _vpb.VectorBlock | None:
        """Pull the next VectorBlock from the source, cache it.

        Returns:
            The ``VectorBlock``, or ``None`` if the source is exhausted.
        """
        if self._exhausted:
            return None
        try:
            resp = next(self._source)
        except StopIteration:
            self._exhausted = True
            return None

        if not resp.HasField("vector"):
            # Response without a vector block (e.g. final empty page)
            self._exhausted = True
            return None

        block = resp.vector
        self._cache.append(block)

        # Populate fields from first block's schema
        if self._fields is None and block.schema.fields:
            self._fields = [MillField.from_proto(f) for f in block.schema.fields]

        return block

    def _iter_blocks(self) -> Iterator[_vpb.VectorBlock]:
        """Iterate over all blocks: replay cache then fetch remaining."""
        # Replay cached blocks
        idx = 0
        while idx < len(self._cache):
            yield self._cache[idx]
            idx += 1
        # Fetch remaining from source
        while not self._exhausted:
            block = self._consume_next()
            if block is not None:
                yield block

    # -- public iteration --

    def __iter__(self) -> Iterator[dict[str, Any]]:
        """Yield one ``dict[str, Any]`` per row.

        Column names are the keys; values are Python-native types
        (or ``None`` for nulls).

        Re-iterating a ``ResultSet`` replays cached blocks first, then
        continues fetching from the transport source.
        """
        for block in self._iter_blocks():
            _fields, rows = read_vector_block(block)
            # Set fields if not yet set
            if self._fields is None and _fields:
                self._fields = _fields
            yield from rows

    def fetchall(self) -> list[dict[str, Any]]:
        """Force full consumption and return all rows.

        Returns:
            A list of row dicts.
        """
        return list(self)

    # -- DataFrame extras (lazy imports) --

    def to_arrow(self) -> Any:
        """Convert the result set to a PyArrow ``Table``.

        Requires the ``arrow`` extra: ``pip install qpointz-mill-py[arrow]``.

        Returns:
            A ``pyarrow.Table``.

        Raises:
            ImportError: If ``pyarrow`` is not installed.
        """
        try:
            from mill.extras.arrow import result_to_arrow
        except ImportError:
            raise ImportError(
                "PyArrow is required for to_arrow(). "
                "Install it with: pip install qpointz-mill-py[arrow]"
            ) from None
        return result_to_arrow(self)

    def to_pandas(self) -> Any:
        """Convert the result set to a pandas ``DataFrame``.

        Requires the ``pandas`` extra: ``pip install qpointz-mill-py[pandas]``.

        Returns:
            A ``pandas.DataFrame``.

        Raises:
            ImportError: If ``pandas`` is not installed.
        """
        try:
            from mill.extras.pandas import result_to_pandas
        except ImportError:
            raise ImportError(
                "pandas is required for to_pandas(). "
                "Install it with: pip install qpointz-mill-py[pandas]"
            ) from None
        return result_to_pandas(self)

    def to_polars(self) -> Any:
        """Convert the result set to a polars ``DataFrame``.

        Requires the ``polars`` extra: ``pip install qpointz-mill-py[polars]``.

        Returns:
            A ``polars.DataFrame``.

        Raises:
            ImportError: If ``polars`` is not installed.
        """
        try:
            from mill.extras.polars import result_to_polars
        except ImportError:
            raise ImportError(
                "polars is required for to_polars(). "
                "Install it with: pip install qpointz-mill-py[polars]"
            ) from None
        return result_to_polars(self)

    def __repr__(self) -> str:
        n = len(self._cache)
        status = "exhausted" if self._exhausted else "streaming"
        return f"<ResultSet blocks={n} {status}>"
