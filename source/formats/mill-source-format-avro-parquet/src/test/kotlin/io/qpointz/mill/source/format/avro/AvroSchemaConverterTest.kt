package io.qpointz.mill.source.format.avro

import io.qpointz.mill.proto.LogicalDataType.LogicalDataTypeId
import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AvroSchemaConverterTest {

    @Test
    fun shouldConvertBooleanField() {
        val avro = recordSchema("f" to Schema.create(Schema.Type.BOOLEAN))
        val mill = AvroSchemaConverter.convert(avro)
        assertEquals(1, mill.size)
        assertEquals("f", mill.fields[0].name)
        assertEquals(LogicalDataTypeId.BOOL, mill.fields[0].type.asLogicalDataType().typeId)
        assertFalse(mill.fields[0].type.nullable)
    }

    @Test
    fun shouldConvertIntField() {
        val avro = recordSchema("count" to Schema.create(Schema.Type.INT))
        val mill = AvroSchemaConverter.convert(avro)
        assertEquals(LogicalDataTypeId.INT, mill.fields[0].type.asLogicalDataType().typeId)
        assertFalse(mill.fields[0].type.nullable)
    }

    @Test
    fun shouldConvertLongField() {
        val avro = recordSchema("id" to Schema.create(Schema.Type.LONG))
        val mill = AvroSchemaConverter.convert(avro)
        assertEquals(LogicalDataTypeId.BIG_INT, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertFloatField() {
        val avro = recordSchema("score" to Schema.create(Schema.Type.FLOAT))
        val mill = AvroSchemaConverter.convert(avro)
        assertEquals(LogicalDataTypeId.FLOAT, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertDoubleField() {
        val avro = recordSchema("price" to Schema.create(Schema.Type.DOUBLE))
        val mill = AvroSchemaConverter.convert(avro)
        assertEquals(LogicalDataTypeId.DOUBLE, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertStringField() {
        val avro = recordSchema("name" to Schema.create(Schema.Type.STRING))
        val mill = AvroSchemaConverter.convert(avro)
        assertEquals(LogicalDataTypeId.STRING, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertBytesField() {
        val avro = recordSchema("data" to Schema.create(Schema.Type.BYTES))
        val mill = AvroSchemaConverter.convert(avro)
        assertEquals(LogicalDataTypeId.BINARY, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertFixedField() {
        val fixed = Schema.createFixed("hash", null, null, 16)
        val avro = recordSchema("checksum" to fixed)
        val mill = AvroSchemaConverter.convert(avro)
        assertEquals(LogicalDataTypeId.BINARY, mill.fields[0].type.asLogicalDataType().typeId)
        assertEquals(16, mill.fields[0].type.precision)
    }

    @Test
    fun shouldConvertEnumAsString() {
        val enum = Schema.createEnum("Color", null, null, listOf("RED", "GREEN", "BLUE"))
        val avro = recordSchema("color" to enum)
        val mill = AvroSchemaConverter.convert(avro)
        assertEquals(LogicalDataTypeId.STRING, mill.fields[0].type.asLogicalDataType().typeId)
    }

    @Test
    fun shouldConvertNullableUnion() {
        val union = Schema.createUnion(Schema.create(Schema.Type.NULL), Schema.create(Schema.Type.STRING))
        val avro = recordSchema("name" to union)
        val mill = AvroSchemaConverter.convert(avro)
        assertEquals(LogicalDataTypeId.STRING, mill.fields[0].type.asLogicalDataType().typeId)
        assertTrue(mill.fields[0].type.nullable)
    }

    @Test
    fun shouldConvertNullableIntUnion() {
        val union = Schema.createUnion(Schema.create(Schema.Type.INT), Schema.create(Schema.Type.NULL))
        val avro = recordSchema("age" to union)
        val mill = AvroSchemaConverter.convert(avro)
        assertEquals(LogicalDataTypeId.INT, mill.fields[0].type.asLogicalDataType().typeId)
        assertTrue(mill.fields[0].type.nullable)
    }

    @Test
    fun shouldConvertMultipleFields_withCorrectIndexes() {
        val avro = recordSchema(
            "id" to Schema.create(Schema.Type.LONG),
            "name" to Schema.create(Schema.Type.STRING),
            "active" to Schema.create(Schema.Type.BOOLEAN)
        )
        val mill = AvroSchemaConverter.convert(avro)
        assertEquals(3, mill.size)
        assertEquals(0, mill.fields[0].index)
        assertEquals(1, mill.fields[1].index)
        assertEquals(2, mill.fields[2].index)
        assertEquals("id", mill.fields[0].name)
        assertEquals("name", mill.fields[1].name)
        assertEquals("active", mill.fields[2].name)
    }

    @Test
    fun shouldThrow_whenSchemaIsNotRecord() {
        val schema = Schema.create(Schema.Type.STRING)
        val ex = assertThrows<IllegalArgumentException> {
            AvroSchemaConverter.convert(schema)
        }
        assertTrue(ex.message!!.contains("RECORD"))
    }

    @Test
    fun shouldThrow_whenComplexUnion() {
        val union = Schema.createUnion(
            Schema.create(Schema.Type.STRING),
            Schema.create(Schema.Type.INT)
        )
        val avro = recordSchema("ambiguous" to union)
        val ex = assertThrows<IllegalArgumentException> {
            AvroSchemaConverter.convert(avro)
        }
        assertTrue(ex.message!!.contains("Complex unions"))
    }

    @Test
    fun shouldThrow_whenUnsupportedAvroType() {
        val array = Schema.createArray(Schema.create(Schema.Type.STRING))
        val avro = recordSchema("tags" to array)
        val ex = assertThrows<IllegalArgumentException> {
            AvroSchemaConverter.convert(avro)
        }
        assertTrue(ex.message!!.contains("ARRAY"))
    }

    // --- helpers ---

    private fun recordSchema(vararg fields: Pair<String, Schema>): Schema {
        var builder = SchemaBuilder.record("TestRecord").namespace("test").fields()
        for ((name, schema) in fields) {
            builder = builder.name(name).type(schema).noDefault()
        }
        return builder.endRecord()
    }
}
