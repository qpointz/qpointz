package io.qpointz.mill.ai.cli

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.Schema
import io.qpointz.mill.proto.Table
import java.nio.file.Files
import java.nio.file.Path

/**
 * Lightweight [SchemaProvider] backed directly by the checked-in qsynth fixture at `test/skymill.yaml`.
 *
 * The provider extracts the physical schema surface needed by `SchemaFacetService`:
 * - model name -> schema name
 * - `schemas[]` entries -> tables
 * - `attributes[]` or static `data[]` keys -> fields
 *
 * This keeps the CLI grounded in a real repo fixture rather than the older in-memory demo schema.
 */
class QSynthYamlSchemaProvider(
    fixturePath: Path,
) : SchemaProvider {

    private val mapper = ObjectMapper(YAMLFactory())
    private val models: Map<String, JsonNode>

    init {
        Files.newInputStream(fixturePath).use { input ->
            val root = mapper.readTree(input)
            val modelNodes = root.path("models")
            require(modelNodes.isArray) { "Expected 'models' array in qsynth fixture: $fixturePath" }
            models = modelNodes.associateBy { it.path("name").asText() }
        }
    }

    override fun getSchemaNames(): Iterable<String> = models.keys.sorted()

    override fun isSchemaExists(schemaName: String): Boolean = models.containsKey(schemaName)

    override fun getSchema(schemaName: String): Schema {
        val model = requireNotNull(models[schemaName]) { "Unknown schema: $schemaName" }
        val builder = Schema.newBuilder()
        model.path("schemas").forEachIndexed { _, tableNode ->
            builder.addTables(toTable(schemaName, tableNode))
        }
        return builder.build()
    }

    private fun toTable(schemaName: String, node: JsonNode): Table {
        val builder = Table.newBuilder()
            .setSchemaName(schemaName)
            .setName(node.path("name").asText())
            .setTableType(Table.TableTypeId.TABLE)

        val fieldNames = when {
            node.path("attributes").isArray -> node.path("attributes").map { it.path("name").asText() }
            node.path("data").isArray && node.path("data").firstOrNull()?.isObject == true ->
                node.path("data").first().fieldNames().asSequence().toList()
            else -> emptyList()
        }

        fieldNames.forEachIndexed { index, name ->
            builder.addFields(field(name, index))
        }

        return builder.build()
    }

    private fun field(name: String, idx: Int): Field = Field.newBuilder()
        .setName(name)
        .setFieldIdx(idx)
        .setType(DataType.newBuilder().setNullability(DataType.Nullability.NOT_SPECIFIED_NULL).build())
        .build()
}
