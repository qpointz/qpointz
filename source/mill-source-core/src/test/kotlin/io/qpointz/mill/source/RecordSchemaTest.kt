package io.qpointz.mill.source

import io.qpointz.mill.proto.DataType.Nullability
import io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId
import io.qpointz.mill.types.sql.DatabaseType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RecordSchemaTest {

    private fun sampleSchema(): RecordSchema = RecordSchema.of(
        "id" to DatabaseType.i32(false),
        "name" to DatabaseType.string(true, -1),
        "active" to DatabaseType.bool(true)
    )

    @Test
    fun shouldCreateSchema_withCorrectSize() {
        val schema = sampleSchema()
        assertEquals(3, schema.size)
    }

    @Test
    fun shouldAssignSequentialIndexes_whenUsingOfFactory() {
        val schema = sampleSchema()
        assertEquals(0, schema.fields[0].index)
        assertEquals(1, schema.fields[1].index)
        assertEquals(2, schema.fields[2].index)
    }

    @Test
    fun shouldLookupFieldByName() {
        val schema = sampleSchema()
        val field = schema.field("name")
        assertNotNull(field)
        assertEquals("name", field!!.name)
        assertEquals(1, field.index)
    }

    @Test
    fun shouldReturnNull_whenFieldNameNotFound() {
        val schema = sampleSchema()
        assertNull(schema.field("missing"))
    }

    @Test
    fun shouldLookupFieldByIndex() {
        val schema = sampleSchema()
        val field = schema.field(2)
        assertNotNull(field)
        assertEquals("active", field!!.name)
    }

    @Test
    fun shouldReturnNull_whenIndexOutOfBounds() {
        val schema = sampleSchema()
        assertNull(schema.field(-1))
        assertNull(schema.field(10))
    }

    @Test
    fun shouldReturnFieldNames_inOrder() {
        val schema = sampleSchema()
        assertEquals(listOf("id", "name", "active"), schema.fieldNames)
    }

    @Test
    fun shouldCreateEmptySchema() {
        val schema = RecordSchema.empty()
        assertEquals(0, schema.size)
        assertTrue(schema.fields.isEmpty())
        assertTrue(schema.fieldNames.isEmpty())
    }

    @Test
    fun shouldConvertToVectorBlockSchema_withCorrectFieldCount() {
        val schema = sampleSchema()
        val vbs = schema.toVectorBlockSchema()
        assertEquals(3, vbs.fieldsCount)
    }

    @Test
    fun shouldConvertToVectorBlockSchema_withCorrectFieldDetails() {
        val schema = sampleSchema()
        val vbs = schema.toVectorBlockSchema()

        // field 0: id - i32, not null
        val f0 = vbs.getFields(0)
        assertEquals("id", f0.name)
        assertEquals(0, f0.fieldIdx)
        assertEquals(LogicalDataTypeId.INT, f0.type.type.typeId)
        assertEquals(Nullability.NOT_NULL, f0.type.nullability)

        // field 1: name - string, nullable
        val f1 = vbs.getFields(1)
        assertEquals("name", f1.name)
        assertEquals(1, f1.fieldIdx)
        assertEquals(LogicalDataTypeId.STRING, f1.type.type.typeId)
        assertEquals(Nullability.NULL, f1.type.nullability)

        // field 2: active - bool, nullable
        val f2 = vbs.getFields(2)
        assertEquals("active", f2.name)
        assertEquals(2, f2.fieldIdx)
        assertEquals(LogicalDataTypeId.BOOL, f2.type.type.typeId)
        assertEquals(Nullability.NULL, f2.type.nullability)
    }

    @Test
    fun shouldConvertEmptySchema_toEmptyVectorBlockSchema() {
        val vbs = RecordSchema.empty().toVectorBlockSchema()
        assertEquals(0, vbs.fieldsCount)
    }

    @Test
    fun shouldSupportDataClassEquality() {
        val s1 = sampleSchema()
        val s2 = sampleSchema()
        assertEquals(s1, s2)
        assertEquals(s1.hashCode(), s2.hashCode())
    }

    @Test
    fun shouldNotBeEqual_whenFieldsDiffer() {
        val s1 = RecordSchema.of("a" to DatabaseType.i32(false))
        val s2 = RecordSchema.of("b" to DatabaseType.i32(false))
        assertNotEquals(s1, s2)
    }

    @Test
    fun shouldHandleAllSupportedTypes() {
        val schema = RecordSchema.of(
            "bool" to DatabaseType.bool(false),
            "i16" to DatabaseType.i16(false),
            "i32" to DatabaseType.i32(false),
            "i64" to DatabaseType.i64(false),
            "fp32" to DatabaseType.fp32(true, 10, 2),
            "fp64" to DatabaseType.fp64(true, 15, 5),
            "string" to DatabaseType.string(true, 100),
            "binary" to DatabaseType.binary(true, 256),
            "date" to DatabaseType.date(true),
            "time" to DatabaseType.time(true)
        )
        assertEquals(10, schema.size)

        val vbs = schema.toVectorBlockSchema()
        assertEquals(10, vbs.fieldsCount)

        // Spot-check a few type mappings
        assertEquals(LogicalDataTypeId.BOOL, vbs.getFields(0).type.type.typeId)
        assertEquals(LogicalDataTypeId.SMALL_INT, vbs.getFields(1).type.type.typeId)
        assertEquals(LogicalDataTypeId.INT, vbs.getFields(2).type.type.typeId)
        assertEquals(LogicalDataTypeId.BIG_INT, vbs.getFields(3).type.type.typeId)
        assertEquals(LogicalDataTypeId.FLOAT, vbs.getFields(4).type.type.typeId)
        assertEquals(LogicalDataTypeId.DOUBLE, vbs.getFields(5).type.type.typeId)
        assertEquals(LogicalDataTypeId.STRING, vbs.getFields(6).type.type.typeId)
        assertEquals(LogicalDataTypeId.BINARY, vbs.getFields(7).type.type.typeId)
        assertEquals(LogicalDataTypeId.DATE, vbs.getFields(8).type.type.typeId)
        assertEquals(LogicalDataTypeId.TIME, vbs.getFields(9).type.type.typeId)
    }

    @Test
    fun shouldPreservePrecisionAndScale_inVectorBlockSchema() {
        val schema = RecordSchema.of(
            "amount" to DatabaseType.fp64(true, 15, 5)
        )
        val vbs = schema.toVectorBlockSchema()
        val field = vbs.getFields(0)
        assertEquals(15, field.type.type.precision)
        assertEquals(5, field.type.type.scale)
    }
}
