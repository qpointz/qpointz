package io.qpointz.mill.ai.capabilities.valuemapping

import org.slf4j.LoggerFactory

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

/**
 * Pure stateless implementations of the two value-mapping tool handlers.
 *
 * Each function maps a [ValueMappingResolver] call to a flat, JSON-serializable result type.
 * The result types mirror the output schemas declared in `capabilities/value-mapping.yaml`
 * exactly — any field added here must also be reflected in the YAML output schema and vice versa.
 *
 * **Diagnostics:** set `logging.level.io.qpointz.mill.ai.capabilities.valuemapping=DEBUG` to log
 * attribute discovery and each requested value with its resolved canonical value (and [ValueResolution.similarityScore]
 * when the resolver supplies it).
 */
object ValueMappingToolHandlers {

    private val log = LoggerFactory.getLogger("io.qpointz.mill.ai.capabilities.valuemapping")

    /**
     * Response payload for the `get_value_mapping_attributes` tool.
     *
     * @property table The fully qualified table id that was queried.
     * @property attributes All attributes for the table, each annotated with a mapped flag.
     */
    data class MappedAttributesResult(
        val table: String,
        val attributes: List<MappedAttribute>,
    )

    /**
     * Response payload for the `get_value_mapping` tool.
     *
     * @property table The fully qualified table id that was queried.
     * @property attribute The attribute (column) name whose values were resolved.
     * @property results One [ValueResolution] entry per requested value.
     */
    data class ValueMappingResult(
        val table: String,
        val attribute: String,
        val results: List<ValueResolution>,
    )

    /**
     * Returns all attributes for [tableId] together with their mapped flags.
     *
     * Delegates entirely to [resolver]; returns an empty attribute list when [tableId] is
     * unknown. The result wraps the table id so the planner can correlate the response with
     * the request without re-inspecting its context.
     */
    fun getMappedAttributes(resolver: ValueMappingResolver, tableId: String): MappedAttributesResult {
        val attributes = resolver.getMappedAttributes(tableId)
        if (log.isDebugEnabled) {
            val mappedNames = attributes.filter { it.mapped }.map { it.attribute }
            log.debug(
                "value-mapping get_value_mapping_attributes: table={} totalAttributes={} mappedAttributeCount={} mappedAttributes={}",
                tableId,
                attributes.size,
                mappedNames.size,
                mappedNames,
            )
        }
        return MappedAttributesResult(
            table = tableId,
            attributes = attributes,
        )
    }

    /**
     * Resolves each of [requestedValues] to its canonical database value for the given
     * [tableId] and [attributeName].
     *
     * When the resolver cannot match a term (e.g. vector store miss), [ValueMappingResolver]
     * may return [ValueResolution.mappedValue] as null. The tool **echoes [ValueResolution.requestedValue]**
     * as [ValueResolution.mappedValue] in that case: the term was not normalized via the mapping
     * index, but it may still exist as a literal in the database — callers should use this value in
     * SQL rather than treating absence of mapping as “unknown data”.
     */
    fun resolveValues(
        resolver: ValueMappingResolver,
        tableId: String,
        attributeName: String,
        requestedValues: List<String>,
    ): ValueMappingResult {
        val raw = resolver.resolveValues(tableId, attributeName, requestedValues)
        val results =
            raw.map { r ->
                ValueResolution(
                    requestedValue = r.requestedValue,
                    mappedValue = r.mappedValue ?: r.requestedValue,
                    similarityScore = r.similarityScore,
                )
            }
        if (log.isDebugEnabled) {
            val detail = results.zip(raw).joinToString("; ") { (out, inn) ->
                buildString {
                    append('"').append(out.requestedValue).append("\"->")
                    append('"').append(out.mappedValue).append('"')
                    if (inn.mappedValue == null) {
                        append(" (pass-through; resolver had no mapping)")
                    }
                    if (out.similarityScore != null) {
                        append(" similarity=").append(out.similarityScore)
                    }
                }
            }
            log.debug(
                "value-mapping get_value_mapping: table={} attribute={} requestedCount={} requested={} resolutions=[{}]",
                tableId,
                attributeName,
                requestedValues.size,
                requestedValues,
                detail,
            )
        }
        return ValueMappingResult(
            table = tableId,
            attribute = attributeName,
            results = results,
        )
    }
}

