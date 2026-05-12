package io.qpointz.mill.data.query.engine

import io.qpointz.mill.proto.DataType
import io.qpointz.mill.proto.Field
import io.qpointz.mill.proto.LogicalDataType
import io.qpointz.mill.proto.VectorBlockSchema
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class QueryResultColumnSchemaTest {

    @Test
    fun `should map fields to envelope column descriptors`() {
        val schema = VectorBlockSchema.newBuilder()
            .addFields(
                Field.newBuilder()
                    .setName("id")
                    .setFieldIdx(0)
                    .setType(
                        DataType.newBuilder()
                            .setNullability(DataType.Nullability.NOT_NULL)
                            .setType(
                                LogicalDataType.newBuilder()
                                    .setTypeId(LogicalDataType.LogicalDataTypeId.INT)
                                    .setPrecision(32)
                                    .build(),
                            )
                            .build(),
                    )
                    .build(),
            )
            .addFields(
                Field.newBuilder()
                    .setName("label")
                    .setFieldIdx(1)
                    .setType(
                        DataType.newBuilder()
                            .setNullability(DataType.Nullability.NULL)
                            .setType(
                                LogicalDataType.newBuilder()
                                    .setTypeId(LogicalDataType.LogicalDataTypeId.STRING)
                                    .setPrecision(255)
                                    .build(),
                            )
                            .build(),
                    )
                    .build(),
            )
            .build()

        val cols = schema.toQueryResultSchemaColumns()
        assertThat(cols).hasSize(2)
        assertThat(cols[0]["name"]).isEqualTo("id")
        assertThat(cols[0]["type"]).isEqualTo("INT")
        assertThat(cols[0]["idx"]).isEqualTo(0)
        assertThat(cols[0]["nullable"]).isEqualTo(false)
        assertThat(cols[0]["precision"]).isEqualTo(32)
        assertThat(cols[0]["scale"]).isNull()
        assertThat(cols[0]["length"]).isNull()

        assertThat(cols[1]["name"]).isEqualTo("label")
        assertThat(cols[1]["type"]).isEqualTo("STRING")
        assertThat(cols[1]["nullable"]).isEqualTo(true)
        assertThat(cols[1]["length"]).isEqualTo(255)
        assertThat(cols[1]["precision"]).isEqualTo(255)
    }
}
