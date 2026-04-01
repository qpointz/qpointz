package io.qpointz.mill.data.schema.api

import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.metadata.domain.MetadataEntityUrn
import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.Field
import io.qpointz.mill.data.metadata.SchemaModelRoot
import io.qpointz.mill.proto.LogicalDataType
import io.qpointz.mill.proto.Schema as ProtoSchema
import io.qpointz.mill.proto.Table
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(
    classes = [SchemaExplorerControllerIT.TestApp::class],
    properties = [
        "spring.autoconfigure.exclude=io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration," +
            "io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration," +
            "io.qpointz.mill.autoconfigure.data.backend.calcite.CalciteBackendAutoConfiguration," +
            "io.qpointz.mill.autoconfigure.data.backend.jdbc.JdbcBackendAutoConfiguration," +
            "io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendAutoConfiguration," +
            "io.qpointz.mill.metadata.configuration.MetadataFileRepositoryAutoConfiguration"
    ]
)
@AutoConfigureMockMvc(addFilters = false)
class SchemaExplorerControllerIT {

    @SpringBootApplication
    @Import(TestSchemaProviderConfiguration::class)
    class TestApp

    @TestConfiguration
    class TestSchemaProviderConfiguration {
        @Bean
        fun schemaProvider(): SchemaProvider = object : SchemaProvider {
            override fun getSchemaNames(): Iterable<String> = listOf("sales")
            override fun isSchemaExists(schemaName: String): Boolean = schemaName == "sales"
            override fun getSchema(schemaName: String): ProtoSchema = ProtoSchema.newBuilder()
                .addTables(customersTable())
                .build()

            override fun getTable(schemaName: String, tableName: String): Table? {
                if (schemaName != "sales" || tableName != "customers") return null
                return customersTable()
            }

            private fun customersTable(): Table = Table.newBuilder()
                .setSchemaName("sales")
                .setName("customers")
                .setTableType(Table.TableTypeId.TABLE)
                .addFields(field("customer_id", 0, LogicalDataType.LogicalDataTypeId.BIG_INT, false))
                .addFields(field("name", 1, LogicalDataType.LogicalDataTypeId.STRING, true))
                .build()
        }

        private fun field(
            name: String,
            index: Int,
            typeId: LogicalDataType.LogicalDataTypeId,
            nullable: Boolean
        ): Field = Field.newBuilder()
            .setName(name)
            .setFieldIdx(index)
            .setType(
                DataType.newBuilder()
                    .setNullability(
                        if (nullable) DataType.Nullability.NULL else DataType.Nullability.NOT_NULL
                    )
                    .setType(LogicalDataType.newBuilder().setTypeId(typeId).build())
                    .build()
            )
            .build()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `shouldListSchemasInFullContext`() {
        mockMvc.get("/api/v1/schema/schemas") {
            param("scope", "global")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].id") { value(SchemaModelRoot.ENTITY_LOCAL_ID) }
            jsonPath("$[0].entityType") { value("MODEL") }
            jsonPath("$[0].metadataEntityId") { value(MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID)) }
            jsonPath("$[1].id") { value("sales") }
            jsonPath("$[1].entityType") { value("SCHEMA") }
        }
    }

    @Test
    fun `shouldListSchemas_whenLegacyContextParamOnly`() {
        mockMvc.get("/api/v1/schema/schemas") {
            param("context", "global")
        }.andExpect {
            status { isOk() }
            jsonPath("$[1].id") { value("sales") }
        }
    }

    @Test
    fun `shouldReturnTableAndColumnDetails`() {
        mockMvc.get("/api/v1/schema/schemas/sales/tables/customers") {
            param("scope", "global")
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value("sales.customers") }
            jsonPath("$.columns[0].id") { value("sales.customers.customer_id") }
            jsonPath("$.columns[0].type.type") { value("BIG_INT") }
            jsonPath("$.columns[0].type.nullable") { value(false) }
        }
    }

    @Test
    fun `shouldReturnBadRequest_whenScopeMalformed`() {
        mockMvc.get("/api/v1/schema/schemas") {
            param("scope", ",")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value("BAD_REQUEST") }
        }
    }

    @Test
    fun `openapiIncludesSchemaEndpoints`() {
        mockMvc.get("/v3/api-docs")
            .andExpect {
                status { isOk() }
                content { string(org.hamcrest.Matchers.containsString("/api/v1/schema/schemas")) }
                content { string(org.hamcrest.Matchers.containsString("/api/v1/schema/model")) }
                content { string(org.hamcrest.Matchers.containsString("\"400\"")) }
            }
    }

    @Test
    fun `shouldReturnModelRoot`() {
        mockMvc.get("/api/v1/schema/model") {
            param("scope", "global")
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(SchemaModelRoot.ENTITY_LOCAL_ID) }
            jsonPath("$.entityType") { value("MODEL") }
            jsonPath("$.metadataEntityId") { value(MetadataEntityUrn.canonicalize(SchemaModelRoot.ENTITY_ID)) }
        }
    }
}
