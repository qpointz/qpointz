package io.qpointz.mill.analysis.queries.web.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Wire representation of the configured SQL dialect for mill-ui Analysis.
 *
 * @param id Mill dialect identifier (e.g. {@code CALCITE}, {@code POSTGRES})
 * @param name human-readable dialect title
 * @param readOnly whether the dialect is treated as read-only by Mill
 * @param editorDialect CodeMirror {@code @codemirror/lang-sql} dialect key ({@code standard}, {@code postgresql}, {@code mysql})
 * @param identifiers quoting rules for SQL identifiers
 * @param functions function names grouped by category (strings, aggregates, …)
 */
@Schema(description = "Configured SQL dialect for the Analysis view")
data class AnalysisDialectWireDto(
    val id: String,
    val name: String,
    val readOnly: Boolean,
    val editorDialect: String,
    val identifiers: AnalysisDialectIdentifiersWireDto,
    val functions: Map<String, List<String>> = emptyMap(),
)

/**
 * Identifier quoting rules exposed to the Analysis SQL editor.
 *
 * @param quoteStart opening quote character for identifiers
 * @param quoteEnd closing quote character for identifiers
 */
data class AnalysisDialectIdentifiersWireDto(
    val quoteStart: String,
    val quoteEnd: String,
)
