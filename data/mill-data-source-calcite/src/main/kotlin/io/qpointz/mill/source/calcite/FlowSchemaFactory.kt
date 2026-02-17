package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.ResolvedSource
import io.qpointz.mill.source.SourceResolver
import io.qpointz.mill.source.descriptor.SourceDescriptor
import io.qpointz.mill.source.descriptor.SourceObjectMapper
import io.qpointz.mill.source.factory.SourceMaterializer
import org.apache.calcite.schema.Schema
import org.apache.calcite.schema.SchemaFactory
import org.apache.calcite.schema.SchemaPlus
import java.io.File

/**
 * Calcite [SchemaFactory] that creates a [FlowSchema] from a source
 * descriptor YAML file.
 *
 * ### Calcite model operand (file reference)
 *
 * ```json
 * {
 *   "schemas": [{
 *     "name": "MY_DATA",
 *     "type": "custom",
 *     "factory": "io.qpointz.mill.source.calcite.FlowSchemaFactory",
 *     "operand": {
 *       "descriptorFile": "/path/to/source-descriptor.yaml"
 *     }
 *   }]
 * }
 * ```
 *
 * The `descriptorFile` operand points to a YAML file containing a full
 * [SourceDescriptor]. The factory deserializes it, materializes runtime
 * components, resolves tables, and returns a [FlowSchema].
 *
 * ### Programmatic API
 *
 * For embedding without a descriptor file, use the companion builder:
 *
 * ```kotlin
 * val schema = FlowSchemaFactory.createSchema(descriptor)
 * // or
 * val schema = FlowSchemaFactory.createSchema(resolvedSource)
 * ```
 */
class FlowSchemaFactory : SchemaFactory {

    /**
     * Creates a [FlowSchema] from the Calcite model operand map.
     *
     * Expected operand keys:
     * - `descriptorFile` (required) — path to source descriptor YAML file
     *
     * @throws IllegalArgumentException if the operand is missing required keys
     * @throws IllegalStateException if the descriptor file cannot be read or parsed
     */
    override fun create(
        parentSchema: SchemaPlus,
        name: String,
        operand: MutableMap<String, Any>
    ): Schema {
        val descriptorPath = operand["descriptorFile"]?.toString()
            ?: throw IllegalArgumentException(
                "FlowSchemaFactory requires 'descriptorFile' in the operand map"
            )

        val file = File(descriptorPath)
        require(file.exists()) {
            "Descriptor file does not exist: ${file.absolutePath}"
        }
        require(file.isFile) {
            "Descriptor path is not a file: ${file.absolutePath}"
        }

        val descriptor = try {
            SourceObjectMapper.yaml.readValue(file, SourceDescriptor::class.java)
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to parse descriptor file '${file.absolutePath}': ${e.message}", e
            )
        }

        return createSchema(descriptor)
    }

    companion object {

        /**
         * Creates a [FlowSchema] programmatically from a [SourceDescriptor].
         *
         * Materializes all runtime components (storage, format handlers,
         * table mappers) via SPI-based [SourceMaterializer], then resolves
         * tables into a [ResolvedSource] and wraps it in a [FlowSchema].
         *
         * @param descriptor the source descriptor
         * @param classLoader optional class loader for SPI discovery
         * @return a ready-to-use [FlowSchema]
         */
        @JvmStatic
        @JvmOverloads
        fun createSchema(
            descriptor: SourceDescriptor,
            classLoader: ClassLoader = Thread.currentThread().contextClassLoader
        ): FlowSchema {
            val materializer = SourceMaterializer(classLoader)
            val materialized = materializer.materialize(descriptor)
            val resolved = SourceResolver.resolve(materialized).let { tables ->
                ResolvedSource(materialized, tables)
            }
            return FlowSchema(resolved)
        }

        /**
         * Creates a [FlowSchema] programmatically from a pre-resolved source.
         *
         * Use this when you have already materialized and resolved the source
         * yourself and just need the Calcite adapter layer.
         *
         * @param resolvedSource the pre-resolved source
         * @return a ready-to-use [FlowSchema]
         */
        @JvmStatic
        fun createSchema(resolvedSource: ResolvedSource): FlowSchema {
            return FlowSchema(resolvedSource)
        }
    }
}
