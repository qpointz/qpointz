package io.qpointz.mill.analysis.queries.web

import io.qpointz.mill.analysis.queries.web.dto.AnalysisDialectIdentifiersWireDto
import io.qpointz.mill.analysis.queries.web.dto.AnalysisDialectWireDto
import io.qpointz.mill.sql.v2.dialect.SqlDialectSpec
import java.util.Locale

/**
 * Maps {@link SqlDialectSpec} to Analysis dialect wire DTOs.
 */
object AnalysisDialectWireMapper {

    /**
     * @param spec configured Mill SQL dialect
     * @return wire DTO for {@code GET /api/v1/analysis/dialect}
     */
    fun toWire(spec: SqlDialectSpec): AnalysisDialectWireDto = AnalysisDialectWireDto(
        id = spec.id,
        name = spec.name,
        readOnly = spec.readOnly,
        editorDialect = toEditorDialectId(spec.id),
        identifiers = AnalysisDialectIdentifiersWireDto(
            quoteStart = spec.identifiers.quote.start,
            quoteEnd = spec.identifiers.quote.end,
        ),
        functions = spec.functions.mapValues { (_, entries) ->
            entries.flatMap { entry ->
                listOf(entry.name) + entry.synonyms
            }.distinct()
        },
    )

    /**
     * @param millDialectId Mill dialect id from {@code mill.data.sql.dialect}
     * @return CodeMirror dialect key understood by mill-ui
     */
    fun toEditorDialectId(millDialectId: String): String =
        when (millDialectId.uppercase(Locale.ROOT)) {
            "POSTGRES" -> "postgresql"
            "MYSQL" -> "mysql"
            else -> "standard"
        }
}
