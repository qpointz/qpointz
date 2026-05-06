package io.qpointz.mill.source.format.json.export

import io.qpointz.mill.source.export.ExportFormatMetadata
import io.qpointz.mill.source.export.ExportFormatProvider
import io.qpointz.mill.source.export.StreamingExportEncoder

/** SPI: UTF-8 JSON array of row objects (streaming; not NDJSON). */
class JsonExportFormatProvider : ExportFormatProvider {
    override fun metadata(): ExportFormatMetadata =
        ExportFormatMetadata("json", "application/json; charset=utf-8", "json")

    override fun encoder(): StreamingExportEncoder =
        StreamingExportEncoder { iterator, out -> JsonArrayStreamingEncoder.writeJsonArray(iterator, out) }
}
