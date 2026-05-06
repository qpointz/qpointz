package io.qpointz.mill.source.export

/**
 * Public identity of a streaming export format for HTTP catalog and content negotiation.
 *
 * @property id stable lowercase format token (for example `csv`, `json`)
 * @property mediaType primary MIME type written to HTTP `Content-Type`
 * @property fileExtension suggested filename suffix without dot (for example `csv`)
 */
data class ExportFormatMetadata(
    val id: String,
    val mediaType: String,
    val fileExtension: String,
)
