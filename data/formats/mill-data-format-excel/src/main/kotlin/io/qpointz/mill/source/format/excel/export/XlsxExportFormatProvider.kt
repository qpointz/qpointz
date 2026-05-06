package io.qpointz.mill.source.format.excel.export

import io.qpointz.mill.source.export.ExportFormatMetadata
import io.qpointz.mill.source.export.ExportFormatProvider
import io.qpointz.mill.source.export.StreamingExportEncoder

/**
 * SPI: Excel Office Open XML workbook (single sheet **Export**; streamed via SXSSF windowing).
 */
class XlsxExportFormatProvider : ExportFormatProvider {

    override fun metadata(): ExportFormatMetadata =
        ExportFormatMetadata(
            "xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "xlsx",
        )

    override fun encoder(): StreamingExportEncoder =
        StreamingExportEncoder { iterator, out ->
            ExcelVectorExportSupport.writeXlsx(iterator, out)
        }
}
