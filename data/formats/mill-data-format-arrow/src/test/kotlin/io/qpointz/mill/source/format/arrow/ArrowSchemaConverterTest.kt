package io.qpointz.mill.source.format.arrow

import io.qpointz.mill.proto.LogicalDataType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ArrowSchemaConverterTest {
    @Test
    fun shouldMapTimestampTzToArrowTimestampWithTimezone() {
        val arrowSchema = ArrowSchemaConverter.toArrowSchema(
            ArrowTestUtils.testSchema,
            timezoneByColumn = mapOf("event_ts" to "UTC")
        )
        val field = arrowSchema.fields.first { it.name == "event_ts" }
        val type = field.type as org.apache.arrow.vector.types.pojo.ArrowType.Timestamp
        assertEquals("UTC", type.timezone)
    }

    @Test
    fun shouldConvertBackToRecordSchema() {
        val arrowSchema = ArrowSchemaConverter.toArrowSchema(
            ArrowTestUtils.testSchema,
            timezoneByColumn = mapOf("event_ts" to "UTC")
        )
        val schema = ArrowSchemaConverter.toRecordSchema(arrowSchema)
        assertEquals(4, schema.size)
        val tsType = schema.fields.first { it.name == "event_ts" }.type.asLogicalDataType().typeId
        assertEquals(LogicalDataType.LogicalDataTypeId.TIMESTAMP_TZ, tsType)
        assertTrue(schema.fields.first { it.name == "name" }.type.nullable)
    }
}
