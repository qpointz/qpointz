package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.types.logical.*
import io.qpointz.mill.types.sql.DatabaseType
import org.apache.calcite.jdbc.JavaTypeFactoryImpl
import org.apache.calcite.sql.type.SqlTypeName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CalciteTypeMapperTest {

    private lateinit var typeFactory: JavaTypeFactoryImpl

    @BeforeEach
    fun setUp() {
        typeFactory = JavaTypeFactoryImpl()
    }

    // --- single DatabaseType mappings ---

    @Test
    fun shouldMapBoolToBoolean() {
        val dbType = DatabaseType.bool(false)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.BOOLEAN, relType.sqlTypeName)
        assertFalse(relType.isNullable)
    }

    @Test
    fun shouldMapNullableBoolToNullableBoolean() {
        val dbType = DatabaseType.bool(true)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.BOOLEAN, relType.sqlTypeName)
        assertTrue(relType.isNullable)
    }

    @Test
    fun shouldMapStringWithPrecision() {
        val dbType = DatabaseType.string(true, 255)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.VARCHAR, relType.sqlTypeName)
        assertEquals(255, relType.precision)
        assertTrue(relType.isNullable)
    }

    @Test
    fun shouldMapStringWithoutPrecision() {
        val dbType = DatabaseType.of(StringLogical.INSTANCE, false)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.VARCHAR, relType.sqlTypeName)
        assertFalse(relType.isNullable)
    }

    @Test
    fun shouldMapI32ToInteger() {
        val dbType = DatabaseType.i32(false)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.INTEGER, relType.sqlTypeName)
        assertFalse(relType.isNullable)
    }

    @Test
    fun shouldMapI16ToSmallInt() {
        val dbType = DatabaseType.i16(true)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.SMALLINT, relType.sqlTypeName)
        assertTrue(relType.isNullable)
    }

    @Test
    fun shouldMapI64ToBigInt() {
        val dbType = DatabaseType.i64(false)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.BIGINT, relType.sqlTypeName)
        assertFalse(relType.isNullable)
    }

    @Test
    fun shouldMapTinyIntToTinyInt() {
        val dbType = DatabaseType.of(TinyIntLogical.INSTANCE, false)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.TINYINT, relType.sqlTypeName)
    }

    @Test
    fun shouldMapFp32ToFloat() {
        val dbType = DatabaseType.of(FloatLogical.INSTANCE, true)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.FLOAT, relType.sqlTypeName)
        assertTrue(relType.isNullable)
    }

    @Test
    fun shouldMapFp32ToFloat_withPrecisionScale() {
        // FLOAT does not support prec/scale in Calcite — they should be ignored
        val dbType = DatabaseType.fp32(true, 10, 2)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.FLOAT, relType.sqlTypeName)
        assertTrue(relType.isNullable)
    }

    @Test
    fun shouldMapFp64ToDouble() {
        val dbType = DatabaseType.of(DoubleLogical.INSTANCE, false)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.DOUBLE, relType.sqlTypeName)
        assertFalse(relType.isNullable)
    }

    @Test
    fun shouldMapFp64ToDouble_withPrecisionScale() {
        // DOUBLE does not support prec/scale in Calcite — they should be ignored
        val dbType = DatabaseType.fp64(false, 15, 5)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.DOUBLE, relType.sqlTypeName)
        assertFalse(relType.isNullable)
    }

    @Test
    fun shouldMapDateToDate() {
        val dbType = DatabaseType.date(true)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.DATE, relType.sqlTypeName)
        assertTrue(relType.isNullable)
    }

    @Test
    fun shouldMapTimeToTime() {
        val dbType = DatabaseType.time(false)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.TIME, relType.sqlTypeName)
        assertFalse(relType.isNullable)
    }

    @Test
    fun shouldMapTimestampToTimestamp() {
        val dbType = DatabaseType.of(TimestampLogical.INSTANCE, true)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.TIMESTAMP, relType.sqlTypeName)
        assertTrue(relType.isNullable)
    }

    @Test
    fun shouldMapTimestampTZToTimestampWithLocalTZ() {
        val dbType = DatabaseType.of(TimestampTZLogical.INSTANCE, false)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.TIMESTAMP_WITH_LOCAL_TIME_ZONE, relType.sqlTypeName)
        assertFalse(relType.isNullable)
    }

    @Test
    fun shouldMapBinaryToVarbinary() {
        val dbType = DatabaseType.binary(true, 1024)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.VARBINARY, relType.sqlTypeName)
        assertEquals(1024, relType.precision)
        assertTrue(relType.isNullable)
    }

    @Test
    fun shouldMapIntervalDayToIntervalDay() {
        val dbType = DatabaseType.of(IntervalDayLogical.INSTANCE, false)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        // Calcite uses INTERVAL_DAY SqlTypeName for day intervals
        assertEquals(SqlTypeName.INTERVAL_DAY, relType.sqlTypeName)
        assertFalse(relType.isNullable)
    }

    @Test
    fun shouldMapIntervalYearToIntervalYear() {
        val dbType = DatabaseType.of(IntervalYearLogical.INSTANCE, false)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.INTERVAL_YEAR, relType.sqlTypeName)
        assertFalse(relType.isNullable)
    }

    @Test
    fun shouldMapUUIDToVarchar() {
        val dbType = DatabaseType.of(UUIDLogical.INSTANCE, true)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.VARCHAR, relType.sqlTypeName)
        assertTrue(relType.isNullable)
    }

    // --- RecordSchema to row type ---

    @Test
    fun shouldMapRecordSchemaToStructType() {
        val schema = RecordSchema.of(
            "id" to DatabaseType.i32(false),
            "name" to DatabaseType.string(true, 100),
            "active" to DatabaseType.bool(false)
        )

        val rowType = CalciteTypeMapper.toRelDataType(schema, typeFactory)

        assertEquals(3, rowType.fieldCount)
        assertEquals("id", rowType.fieldList[0].name)
        assertEquals(SqlTypeName.INTEGER, rowType.fieldList[0].type.sqlTypeName)
        assertFalse(rowType.fieldList[0].type.isNullable)

        assertEquals("name", rowType.fieldList[1].name)
        assertEquals(SqlTypeName.VARCHAR, rowType.fieldList[1].type.sqlTypeName)
        assertTrue(rowType.fieldList[1].type.isNullable)

        assertEquals("active", rowType.fieldList[2].name)
        assertEquals(SqlTypeName.BOOLEAN, rowType.fieldList[2].type.sqlTypeName)
        assertFalse(rowType.fieldList[2].type.isNullable)
    }

    @Test
    fun shouldMapEmptySchemaToEmptyStruct() {
        val schema = RecordSchema.empty()
        val rowType = CalciteTypeMapper.toRelDataType(schema, typeFactory)
        assertEquals(0, rowType.fieldCount)
    }

    @Test
    fun shouldPreservePrecisionAndScale_whenBothSet() {
        // VARCHAR supports precision (= length)
        val dbType = DatabaseType.string(false, 50)
        val relType = CalciteTypeMapper.toRelDataType(dbType, typeFactory)
        assertEquals(SqlTypeName.VARCHAR, relType.sqlTypeName)
        assertEquals(50, relType.precision)
        assertFalse(relType.isNullable)
    }
}
