package io.qpointz.mill.source.format.text.export

import io.qpointz.mill.source.export.ExportFormatMetadata
import io.qpointz.mill.source.export.ExportFormatProvider
import io.qpointz.mill.source.export.StreamingExportEncoder

/**
 * SPI: Tab-separated values (same pipeline as CSV with HT delimiter).
 */
class TsvExportFormatProvider : ExportFormatProvider {

    override fun metadata(): ExportFormatMetadata =
        ExportFormatMetadata("tsv", "text/tab-separated-values; charset=utf-8", "tsv")

    override fun encoder(): StreamingExportEncoder =
        StreamingExportEncoder { iterator, out ->
            TextVectorExportSupport.writeDelimited(iterator, out, '\t')
        }
}
