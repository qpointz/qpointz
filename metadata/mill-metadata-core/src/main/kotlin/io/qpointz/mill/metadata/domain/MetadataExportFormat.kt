package io.qpointz.mill.metadata.domain

/**
 * Wire representation for [io.qpointz.mill.metadata.service.MetadataImportService.export].
 */
enum class MetadataExportFormat {
    /** Multi-document YAML (`---` separators), `text/yaml`. */
    YAML,

    /** Same ordered document sequence as YAML, encoded as a JSON array. */
    JSON
}
