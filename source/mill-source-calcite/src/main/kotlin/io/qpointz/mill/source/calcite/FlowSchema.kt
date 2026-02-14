package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.ResolvedSource
import org.apache.calcite.schema.Table
import org.apache.calcite.schema.impl.AbstractSchema

/**
 * Calcite [AbstractSchema] backed by a Mill [ResolvedSource].
 *
 * Each [io.qpointz.mill.source.SourceTable] in the resolved source
 * is exposed as a [FlowTable] within this schema.
 *
 * **One resolved source = one Calcite schema.**
 *
 * @property resolvedSource the resolved source whose tables form this schema
 */
class FlowSchema(
    private val resolvedSource: ResolvedSource
) : AbstractSchema() {

    override fun getTableMap(): Map<String, Table> {
        return resolvedSource.tables.mapValues { (_, sourceTable) ->
            FlowTable(sourceTable)
        }
    }

    /**
     * Public accessor for the table map — delegates to [getTableMap].
     *
     * `AbstractSchema.getTableMap()` is `protected` in Java, so this
     * convenience method exposes it for Kotlin callers and tests.
     */
    fun flowTables(): Map<String, Table> = tableMap

    /**
     * Returns the underlying [ResolvedSource].
     * Useful for diagnostics or programmatic access.
     */
    fun resolvedSource(): ResolvedSource = resolvedSource
}
