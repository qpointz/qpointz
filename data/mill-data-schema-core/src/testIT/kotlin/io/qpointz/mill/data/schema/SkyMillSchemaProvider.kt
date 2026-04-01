package io.qpointz.mill.data.schema

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.Schema
import io.qpointz.mill.proto.Table

/**
 * Test-only SchemaProvider that returns a representative subset of the Skymill physical schema,
 * matching the entities seeded from skymill-meta-seed-canonical.yaml (test datasets).
 *
 * Also includes [TABLE_NO_METADATA] which has no corresponding entry in the metadata repository,
 * used to verify that missing metadata is represented as absence rather than missing structure.
 */
class SkyMillSchemaProvider : SchemaProvider {

    companion object {
        const val SCHEMA_NAME = "skymill"
        const val TABLE_NO_METADATA = "no_meta_table"
    }

    override fun getSchemaNames(): Iterable<String> = listOf(SCHEMA_NAME)

    override fun isSchemaExists(schemaName: String): Boolean = schemaName == SCHEMA_NAME

    override fun getSchema(schemaName: String): Schema {
        require(schemaName == SCHEMA_NAME) { "Unknown schema: $schemaName" }
        return Schema.newBuilder()
            .addTables(citiesTable())
            .addTables(segmentsTable())
            .addTables(noMetaTable())
            .build()
    }

    private fun citiesTable(): Table = Table.newBuilder()
        .setSchemaName(SCHEMA_NAME)
        .setName("cities")
        .setTableType(Table.TableTypeId.TABLE)
        .addFields(field("id", 0))
        .addFields(field("city", 1))
        .addFields(field("state", 2))
        .addFields(field("population", 3))
        .addFields(field("airport", 4))
        .addFields(field("airport_iata", 5))
        .build()

    private fun segmentsTable(): Table = Table.newBuilder()
        .setSchemaName(SCHEMA_NAME)
        .setName("segments")
        .setTableType(Table.TableTypeId.TABLE)
        .addFields(field("id", 0))
        .addFields(field("origin", 1))
        .addFields(field("destination", 2))
        .addFields(field("distance", 3))
        .build()

    private fun noMetaTable(): Table = Table.newBuilder()
        .setSchemaName(SCHEMA_NAME)
        .setName(TABLE_NO_METADATA)
        .setTableType(Table.TableTypeId.TABLE)
        .addFields(field("id", 0))
        .build()

    private fun field(name: String, idx: Int): Field = Field.newBuilder()
        .setName(name)
        .setFieldIdx(idx)
        .setType(DataType.newBuilder().setNullability(DataType.Nullability.NOT_NULL).build())
        .build()
}
