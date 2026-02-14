package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.ResolvedSource
import io.qpointz.mill.source.SourceResolver
import io.qpointz.mill.source.descriptor.SourceDescriptor
import io.qpointz.mill.source.factory.SourceMaterializer
import org.apache.calcite.schema.SchemaPlus
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages multiple named [FlowSchema] instances and their lifecycle.
 *
 * Each schema is backed by a [ResolvedSource] that holds open resources
 * (blob source, readers). The manager is [AutoCloseable] — closing it
 * closes all managed resolved sources.
 *
 * Thread-safe: internal state is protected by a [ConcurrentHashMap].
 *
 * ```kotlin
 * val manager = SourceSchemaManager()
 * manager.add(airlineDescriptor)
 * manager.add(warehouseDescriptor)
 *
 * // Register into Calcite
 * manager.registerAll(rootSchema)
 *
 * // Query via JDBC...
 *
 * manager.close()
 * ```
 *
 * @param materializer the materializer used to create runtime components from descriptors
 */
class SourceSchemaManager(
    private val materializer: SourceMaterializer = SourceMaterializer()
) : AutoCloseable {

    private val schemas = ConcurrentHashMap<String, ManagedEntry>()

    private data class ManagedEntry(
        val schema: FlowSchema,
        val resolvedSource: ResolvedSource
    )

    /**
     * Creates and registers a [FlowSchema] from a [SourceDescriptor].
     *
     * The descriptor's [SourceDescriptor.name] becomes the schema name.
     * If a schema with that name already exists, it is replaced and the
     * previous resolved source is closed.
     *
     * @param descriptor the source descriptor
     * @return the created [FlowSchema]
     */
    fun add(descriptor: SourceDescriptor): FlowSchema {
        val materialized = materializer.materialize(descriptor)
        val tables = SourceResolver.resolve(materialized)
        val resolved = ResolvedSource(materialized, tables)
        return add(descriptor.name, resolved)
    }

    /**
     * Registers a pre-resolved source as a [FlowSchema].
     *
     * If a schema with that name already exists, it is replaced and the
     * previous resolved source is closed.
     *
     * @param name           the schema name
     * @param resolvedSource the resolved source
     * @return the created [FlowSchema]
     */
    fun add(name: String, resolvedSource: ResolvedSource): FlowSchema {
        val schema = FlowSchema(resolvedSource)
        val entry = ManagedEntry(schema, resolvedSource)
        val previous = schemas.put(name, entry)
        if (previous != null) {
            try {
                previous.resolvedSource.close()
            } catch (_: Exception) {
                // ignore close failures on replaced entry
            }
        }
        return schema
    }

    /**
     * Removes a schema by name.
     *
     * Closes the underlying resolved source.
     *
     * @param name the schema name
     * @return `true` if a schema was removed, `false` if no schema with that name existed
     */
    fun remove(name: String): Boolean {
        val entry = schemas.remove(name) ?: return false
        try {
            entry.resolvedSource.close()
        } catch (_: Exception) {
            // ignore close failures
        }
        return true
    }

    /**
     * Returns the [FlowSchema] for the given name, or `null` if not found.
     */
    fun get(name: String): FlowSchema? = schemas[name]?.schema

    /**
     * Returns the set of all managed schema names.
     */
    fun names(): Set<String> = schemas.keys.toSet()

    /**
     * Returns the number of managed schemas.
     */
    val size: Int get() = schemas.size

    /**
     * Returns `true` if no schemas are managed.
     */
    fun isEmpty(): Boolean = schemas.isEmpty()

    /**
     * Registers all managed schemas into a Calcite [SchemaPlus] root schema.
     *
     * Each schema is added as a sub-schema under its name.
     *
     * @param rootSchema the Calcite root schema to register into
     */
    fun registerAll(rootSchema: SchemaPlus) {
        for ((name, entry) in schemas) {
            rootSchema.add(name, entry.schema)
        }
    }

    /**
     * Closes all managed resolved sources and clears the manager.
     */
    override fun close() {
        val entries = schemas.values.toList()
        schemas.clear()
        for (entry in entries) {
            try {
                entry.resolvedSource.close()
            } catch (_: Exception) {
                // ignore close failures
            }
        }
    }
}
