package io.qpointz.mill.source.format.avro.export

import io.qpointz.mill.source.export.ExportFormatMetadata
import io.qpointz.mill.source.export.ExportFormatProvider
import io.qpointz.mill.source.export.StreamingExportEncoder

/**
 * SPI: Avro OCF with string-typed columns (schema derived from the first result batch).
 */
class AvroExportFormatProvider : ExportFormatProvider {

    override fun metadata(): ExportFormatMetadata =
        ExportFormatMetadata("avro", "application/avro+binary", "avro")

    override fun encoder(): StreamingExportEncoder =
        StreamingExportEncoder { iterator, out ->
            AvroVectorExportSupport.writeContainerFile(iterator, out)
        }
}
