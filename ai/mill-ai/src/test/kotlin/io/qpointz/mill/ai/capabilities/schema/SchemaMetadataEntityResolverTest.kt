package io.qpointz.mill.ai.capabilities.schema

import io.qpointz.mill.metadata.domain.RelationCardinality
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.LogicalDataType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SchemaMetadataEntityResolverTest {

  private val catalog = object : SchemaCatalogPort {
    override fun listSchemas(): List<ListSchemasItem> =
      listOf(
        ListSchemasItem(
          schemaName = "skymill",
          description = "",
          catalogPath = "skymill",
          metadataEntityUrn = "urn:mill/model/schema:skymill",
        ),
      )

    override fun listTables(schemaName: String): List<ListTablesItem> =
      listOf(
        ListTablesItem(
          schemaName = "skymill",
          tableName = "passenger",
          description = "",
          catalogPath = "skymill.passenger",
          metadataEntityUrn = "urn:mill/model/table:skymill.passenger",
        ),
      )

    override fun listColumns(schemaName: String, tableName: String): List<ListColumnsItem> =
      listOf(
        ListColumnsItem(
          schemaName = "skymill",
          tableName = "passenger",
          columnName = "id",
          catalogPath = "skymill.passenger.id",
          metadataEntityUrn = "urn:mill/model/attribute:skymill.passenger.id",
          nullable = DataType.Nullability.NOT_NULL,
          type = LogicalDataType.LogicalDataTypeId.INT,
        ),
      )

    override fun listRelations(
      schemaName: String,
      tableName: String,
      direction: RelationDirection,
    ): List<ListRelationsItem> = emptyList()
  }

  @Test
  fun shouldResolveColumnCatalogPath_whenObjectExists() {
    val result = SchemaMetadataEntityResolver.resolve(catalog, "skymill.passenger.id")

    assertThat(result["error"]).isNull()
    assertThat(result["catalogPath"]).isEqualTo("skymill.passenger.id")
    assertThat(result["metadataEntityUrn"]).isEqualTo("urn:mill/model/attribute:skymill.passenger.id")
    assertThat(result["entityKind"]).isEqualTo("attribute")
  }

  @Test
  fun shouldRejectUnknownCatalogPath() {
    val result = SchemaMetadataEntityResolver.resolve(catalog, "skymill.missing.col")

    assertThat(result["error"]).isEqualTo("catalog object not found: skymill.missing.col")
  }
}
