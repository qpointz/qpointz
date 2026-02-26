"""AsyncResultSet — async lazy-with-cache result iteration.

Mirrors :class:`mill.result.ResultSet` but the source is an
``AsyncIterator`` and all consumption methods are ``async``.
"""
from __future__ import annotations

from typing import Any, AsyncIterator

from mill._proto import data_connect_svc_pb2 as _svc
from mill._proto import vector_pb2 as _vpb
from mill.types import MillField
from mill.vectors import read_vector_block


class AsyncResultSet:
    """Async lazy-with-cache result set over a stream of ``QueryResultResponse``.

    The same design contract as the sync :class:`ResultSet`:

    - VectorBlocks are pulled from the async transport on demand.
    - Once consumed, blocks are cached internally so that re-iteration
      replays cached data before continuing to pull new blocks.
    - ``fetchall()`` forces full consumption.
    - ``to_arrow()`` / ``to_pandas()`` / ``to_polars()`` call
      ``await self.fetchall()`` first if not exhausted, then delegate to
      the extras module (sync, since they operate on cached data).

    Args:
        source: An async iterator of ``QueryResultResponse`` proto messages
            (typically from ``AsyncTransport.exec_query()``).

    Example::

        >>> rs = await client.query("SELECT * FROM MONETA.CLIENTS")
        >>> async for row in rs:
        ...     print(row["FIRST_NAME"])
        >>> # Or fetch all at once:
        >>> rows = await rs.fetchall()
    """

    def __init__(self, source: AsyncIterator[_svc.QueryResultResponse]) -> None:
        self._source = source
        self._cache: list[_vpb.VectorBlock] = []
        self._exhausted: bool = False
        self._fields: list[MillField] | None = None

    # -- properties --

    @property
    def fields(self) -> list[MillField] | None:
        """Column metadata from the first block, or ``None`` if no data yet."""
        return self._fields

    # -- internal helpers --

    async def _consume_next(self) -> _vpb.VectorBlock | None:
        """Pull the next VectorBlock from the async source, cache it.

        Returns:
            The ``VectorBlock``, or ``None`` if the source is exhausted.
        """
        if self._exhausted:
            return None
        try:
            resp: _svc.QueryResultResponse = await self._source.__anext__()
        except StopAsyncIteration:
            self._exhausted = True
            return None

        if not resp.HasField("vector"):
            self._exhausted = True
            return None

        block = resp.vector
        self._cache.append(block)

        if self._fields is None and block.schema.fields:
            self._fields = [MillField.from_proto(f) for f in block.schema.fields]

        return block

    async def _iter_blocks(self) -> AsyncIterator[_vpb.VectorBlock]:
        """Iterate over all blocks: replay cache then fetch remaining."""
        idx = 0
        while idx < len(self._cache):
            yield self._cache[idx]
            idx += 1
        while not self._exhausted:
            block = await self._consume_next()
            if block is not None:
                yield block

    # -- public iteration --

    async def __aiter__(self) -> AsyncIterator[dict[str, Any]]:
        """Yield one ``dict[str, Any]`` per row asynchronously.

        Re-iterating replays cached blocks first, then continues
        fetching from the transport source.
        """
        async for block in self._iter_blocks():
            _fields, rows = read_vector_block(block)
            if self._fields is None and _fields:
                self._fields = _fields
            for row in rows:
                yield row

    async def fetchall(self) -> list[dict[str, Any]]:
        """Force full consumption and return all rows.

        Returns:
            A list of row dicts.
        """
        rows: list[dict[str, Any]] = []
        async for row in self:
            rows.append(row)
        return rows

    # -- DataFrame extras --

    async def to_arrow(self) -> Any:
        """Convert the result set to a PyArrow ``Table``.

        Ensures all blocks are consumed first, then builds the table
        from cached blocks via :func:`mill.extras.arrow.vector_block_to_record_batch`.

        Requires the ``arrow`` extra: ``pip install qpointz-mill-py[arrow]``.

        Returns:
            A ``pyarrow.Table``.
        """
        await self._ensure_exhausted()
        try:
            from mill.extras.arrow import vector_block_to_record_batch, arrow_type
            import pyarrow as pa
        except ImportError:
            raise ImportError(
                "PyArrow is required for to_arrow(). "
                "Install it with: pip install qpointz-mill-py[arrow]"
            ) from None

        if not self._cache:
            if self._fields:
                pa_fields = [
                    pa.field(f.name, arrow_type(f.type), nullable=f.nullable)
                    for f in self._fields
                ]
                return pa.table(
                    {f.name: pa.array([], type=f.type) for f in pa_fields},
                    schema=pa.schema(pa_fields),
                )
            return pa.table({})

        batches = [vector_block_to_record_batch(block) for block in self._cache]
        return pa.Table.from_batches(batches)

    async def to_pandas(self) -> Any:
        """Convert the result set to a pandas ``DataFrame``.

        Requires the ``pandas`` extra: ``pip install qpointz-mill-py[pandas]``.

        Returns:
            A ``pandas.DataFrame``.
        """
        try:
            import pandas as pd
        except ImportError:
            raise ImportError(
                "pandas is required for to_pandas(). "
                "Install it with: pip install qpointz-mill-py[pandas]"
            ) from None
        table = await self.to_arrow()
        return table.to_pandas(types_mapper=pd.ArrowDtype)

    async def to_polars(self) -> Any:
        """Convert the result set to a polars ``DataFrame``.

        Requires the ``polars`` extra: ``pip install qpointz-mill-py[polars]``.

        Returns:
            A ``polars.DataFrame``.
        """
        try:
            import polars as pl
        except ImportError:
            raise ImportError(
                "polars is required for to_polars(). "
                "Install it with: pip install qpointz-mill-py[polars]"
            ) from None
        table = await self.to_arrow()
        return pl.from_arrow(table)

    # -- helpers --

    async def _ensure_exhausted(self) -> None:
        """Consume all remaining blocks from the source into cache."""
        if not self._exhausted:
            # Force full consumption
            async for _ in self._iter_blocks():
                pass

    def __repr__(self) -> str:
        n = len(self._cache)
        status = "exhausted" if self._exhausted else "streaming"
        return f"<AsyncResultSet blocks={n} {status}>"
