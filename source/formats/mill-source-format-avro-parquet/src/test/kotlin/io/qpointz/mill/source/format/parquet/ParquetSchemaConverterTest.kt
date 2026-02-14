package io.qpointz.mill.source.format.parquet

import io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId
import org.apache.parquet.schema.LogicalTypeAnnotation
import org.apache.parquet.schema.MessageType
import org.apache.parquet.schema.PrimitiveType
import org.apache.parquet.schema.Type
import org.apache.parquet.schema.Types
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParquetSchemaConverterTest {

    @Test
    fun shouldConvertBooleanField() {
        val schema = messageType(
            primitiveField("flag", PrimitiveType.PrimitiveTypeName.BOOLEAN)
        )
        val mill = ParquetSchemaConverter.convert(schema)
        assertEquals(LogicalDataTypeId.BOOL, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertInt32Field() {
        val schema = messageType(
            primitiveField("count", PrimitiveType.PrimitiveTypeName.INT32)
        )
        val mill = ParquetSchemaConverter.convert(schema)
        assertEquals(LogicalDataTypeId.INT, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertInt64Field() {
        val schema = messageType(
            primitiveField("id", PrimitiveType.PrimitiveTypeName.INT64)
        )
        val mill = ParquetSchemaConverter.convert(schema)
        assertEquals(LogicalDataTypeId.BIG_INT, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertFloatField() {
        val schema = messageType(
            primitiveField("score", PrimitiveType.PrimitiveTypeName.FLOAT)
        )
        val mill = ParquetSchemaConverter.convert(schema)
        assertEquals(LogicalDataTypeId.FLOAT, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertDoubleField() {
        val schema = messageType(
            primitiveField("price", PrimitiveType.PrimitiveTypeName.DOUBLE)
        )
        val mill = ParquetSchemaConverter.convert(schema)
        assertEquals(LogicalDataTypeId.DOUBLE, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertBinaryStringField() {
        val schema = messageType(
            Types.optional(PrimitiveType.PrimitiveTypeName.BINARY)
                .`as`(LogicalTypeAnnotation.stringType())
                .named("name")
        )
        val mill = ParquetSchemaConverter.convert(schema)
        assertEquals(LogicalDataTypeId.STRING, mill.fields[0].type.asLogicalDataType().typeId)
        assertTrue(mill.fields[0].type.nullable)
    }

    @Test
    fun shouldConvertBinaryWithoutAnnotation_asBinary() {
        val schema = messageType(
            primitiveField("data", PrimitiveType.PrimitiveTypeName.BINARY)
        )
        val mill = ParquetSchemaConverter.convert(schema)
        assertEquals(LogicalDataTypeId.BINARY, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertDateAnnotation() {
        val schema = messageType(
            Types.optional(PrimitiveType.PrimitiveTypeName.INT32)
                .`as`(LogicalTypeAnnotation.dateType())
                .named("birthdate")
        )
        val mill = ParquetSchemaConverter.convert(schema)
        assertEquals(LogicalDataTypeId.DATE, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertRequiredField_asNotNullable() {
        val schema = messageType(
            Types.required(PrimitiveType.PrimitiveTypeName.INT32).named("required_id")
        )
        val mill = ParquetSchemaConverter.convert(schema)
        assertFalse(mill.fields[0].type.nullable)
    }

    @Test
    fun shouldConvertOptionalField_asNullable() {
        val schema = messageType(
            Types.optional(PrimitiveType.PrimitiveTypeName.INT32).named("optional_id")
        )
        val mill = ParquetSchemaConverter.convert(schema)
        assertTrue(mill.fields[0].type.nullable)
    }

    @Test
    fun shouldConvertMultipleFields_withCorrectIndexes() {
        val schema = messageType(
            primitiveField("a", PrimitiveType.PrimitiveTypeName.INT32),
            primitiveField("b", PrimitiveType.PrimitiveTypeName.DOUBLE),
            primitiveField("c", PrimitiveType.PrimitiveTypeName.BOOLEAN)
        )
        val mill = ParquetSchemaConverter.convert(schema)
        assertEquals(3, mill.size)
        assertEquals(0, mill.fields[0].index)
        assertEquals(1, mill.fields[1].index)
        assertEquals(2, mill.fields[2].index)
    }

    @Test
    fun shouldConvertFixedLenByteArray() {
        val schema = messageType(
            Types.optional(PrimitiveType.PrimitiveTypeName.FIXED_LEN_BYTE_ARRAY)
                .length(16)
                .named("uuid")
        )
        val mill = ParquetSchemaConverter.convert(schema)
        assertEquals(LogicalDataTypeId.BINARY, mill.fields[0].type.asLogicalDataType().typeId)
        assertEquals(16, mill.fields[0].type.precision)
    }

    // --- helpers ---

    private fun primitiveField(name: String, type: PrimitiveType.PrimitiveTypeName): Type {
        return Types.optional(type).named(name)
    }

    private fun messageType(vararg fields: Type): MessageType {
        return MessageType("TestMessage", *fields)
    }
}
