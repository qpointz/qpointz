package io.qpointz.mill.source.format.arrow.export

import io.qpointz.mill.source.export.ExportFormatMetadata
import io.qpointz.mill.source.export.ExportFormatProvider
import io.qpointz.mill.source.export.StreamingExportEncoder

/**
 * SPI: Apache Arrow IPC **stream** format (record batches; column types inferred as UTF-8 for export).
 */
class ArrowExportFormatProvider : ExportFormatProvider {

    override fun metadata(): ExportFormatMetadata =
        ExportFormatMetadata(
            "arrow",
            "application/vnd.apache.arrow.stream",
            "arrow",
        )

    override fun encoder(): StreamingExportEncoder =
        StreamingExportEncoder { iterator, out ->
            ArrowVectorExportSupport.writeIpcStream(iterator, out)
        }
}
