package io.qpointz.mill.source.format.text.export

import io.qpointz.mill.source.export.ExportFormatMetadata
import io.qpointz.mill.source.export.ExportFormatProvider
import io.qpointz.mill.source.export.StreamingExportEncoder

/**
 * SPI: RFC 4180-style CSV via [io.qpointz.mill.source.format.text.CsvRecordWriter].
 */
class CsvExportFormatProvider : ExportFormatProvider {

    override fun metadata(): ExportFormatMetadata =
        ExportFormatMetadata("csv", "text/csv; charset=utf-8", "csv")

    override fun encoder(): StreamingExportEncoder =
        StreamingExportEncoder { iterator, out ->
            TextVectorExportSupport.writeDelimited(iterator, out, ',')
        }
}
