package io.qpointz.mill.source

import io.qpointz.mill.proto.DataType.Nullability
import io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId
import io.qpointz.mill.types.sql.DatabaseType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SchemaFieldTest {

    @Test
    fun shouldCreateSchemaField_withCorrectProperties() {
        val field = SchemaField("age", 0, DatabaseType.i32(false))
        assertEquals("age", field.name)
        assertEquals(0, field.index)
        assertFalse(field.type.nullable())
    }

    @Test
    fun shouldConvertToProtoField_withCorrectValues() {
        val field = SchemaField("name", 2, DatabaseType.string(true, 255))
        val proto = field.toProtoField()
        assertEquals("name", proto.name)
        assertEquals(2, proto.fieldIdx)
        assertEquals(LogicalDataTypeId.STRING, proto.type.type.typeId)
        assertEquals(Nullability.NULL, proto.type.nullability)
        assertEquals(255, proto.type.type.precision)
    }

    @Test
    fun shouldConvertNonNullableField_toNotNullNullability() {
        val field = SchemaField("id", 0, DatabaseType.i64(false))
        val proto = field.toProtoField()
        assertEquals(Nullability.NOT_NULL, proto.type.nullability)
        assertEquals(LogicalDataTypeId.BIG_INT, proto.type.type.typeId)
    }

    @Test
    fun shouldSupportDataClassEquality() {
        val f1 = SchemaField("x", 0, DatabaseType.bool(true))
        val f2 = SchemaField("x", 0, DatabaseType.bool(true))
        assertEquals(f1, f2)
        assertEquals(f1.hashCode(), f2.hashCode())
    }

    @Test
    fun shouldNotBeEqual_whenNamesDiffer() {
        val f1 = SchemaField("x", 0, DatabaseType.bool(true))
        val f2 = SchemaField("y", 0, DatabaseType.bool(true))
        assertNotEquals(f1, f2)
    }
}
