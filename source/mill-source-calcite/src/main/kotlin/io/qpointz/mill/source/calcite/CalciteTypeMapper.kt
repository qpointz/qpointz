package io.qpointz.mill.source.calcite

import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.types.logical.*
import io.qpointz.mill.types.sql.DatabaseType
import org.apache.calcite.avatica.util.TimeUnit
import org.apache.calcite.rel.type.RelDataType
import org.apache.calcite.rel.type.RelDataTypeFactory
import org.apache.calcite.sql.SqlIntervalQualifier
import org.apache.calcite.sql.parser.SqlParserPos
import org.apache.calcite.sql.type.SqlTypeName

/**
 * Maps Mill [DatabaseType] / [RecordSchema] to Calcite [RelDataType].
 *
 * Uses [LogicalTypeShuttle] to dispatch each Mill logical type to the
 * corresponding Calcite [SqlTypeName], then applies precision, scale,
 * and nullability from [DatabaseType].
 *
 * TODO: Going forward this should be consolidated into a shared `mill-calcite`
 *  project so both `mill-source-calcite` and `mill-data-backends` can reuse
 *  the same type mapping. We keep it in flow (source) for the time being.
 */
object CalciteTypeMapper {

    /**
     * Converts a single Mill [DatabaseType] to a Calcite [RelDataType].
     */
    fun toRelDataType(dbType: DatabaseType, typeFactory: RelDataTypeFactory): RelDataType {
        val sqlTypeName = dbType.type().accept(SqlTypeNameShuttle)
        val precision = dbType.precision()
        val scale = dbType.scale()
        val nullable = dbType.nullable()
        val na = DatabaseType.PREC_SCALE_NOT_APPLICABLE

        // Interval types require createSqlIntervalType
        val intervalQualifier = INTERVAL_QUALIFIERS[sqlTypeName]
        if (intervalQualifier != null) {
            val baseType = typeFactory.createSqlIntervalType(intervalQualifier)
            return typeFactory.createTypeWithNullability(baseType, nullable)
        }

        // Check whether the SqlTypeName actually supports prec/scale
        val allowsPrec = sqlTypeName.allowsPrec()
        val allowsScale = sqlTypeName.allowsScale()

        val baseType = when {
            precision != na && scale != na && allowsPrec && allowsScale ->
                typeFactory.createSqlType(sqlTypeName, precision, scale)
            precision != na && allowsPrec ->
                typeFactory.createSqlType(sqlTypeName, precision)
            else ->
                typeFactory.createSqlType(sqlTypeName)
        }

        return typeFactory.createTypeWithNullability(baseType, nullable)
    }

    /** Pre-built interval qualifiers for the two Mill interval types. */
    private val INTERVAL_QUALIFIERS = mapOf(
        SqlTypeName.INTERVAL_DAY to SqlIntervalQualifier(
            TimeUnit.DAY, null, SqlParserPos.ZERO
        ),
        SqlTypeName.INTERVAL_YEAR to SqlIntervalQualifier(
            TimeUnit.YEAR, null, SqlParserPos.ZERO
        )
    )

    /**
     * Converts a Mill [RecordSchema] to a Calcite row type.
     */
    fun toRelDataType(schema: RecordSchema, typeFactory: RelDataTypeFactory): RelDataType {
        val fieldNames = schema.fields.map { it.name }
        val fieldTypes = schema.fields.map { toRelDataType(it.type, typeFactory) }
        return typeFactory.createStructType(fieldTypes, fieldNames)
    }

    /**
     * Shuttle that maps each Mill [LogicalType] to the corresponding
     * Calcite [SqlTypeName].
     */
    private object SqlTypeNameShuttle : LogicalTypeShuttle<SqlTypeName> {
        override fun visit(i64Type: TinyIntLogical): SqlTypeName = SqlTypeName.TINYINT
        override fun visit(binaryType: BinaryLogical): SqlTypeName = SqlTypeName.VARBINARY
        override fun visit(boolType: BoolLogical): SqlTypeName = SqlTypeName.BOOLEAN
        override fun visit(dateType: DateLogical): SqlTypeName = SqlTypeName.DATE
        override fun visit(fp32Type: FloatLogical): SqlTypeName = SqlTypeName.FLOAT
        override fun visit(fp64Type: DoubleLogical): SqlTypeName = SqlTypeName.DOUBLE
        override fun visit(i16Type: SmallIntLogical): SqlTypeName = SqlTypeName.SMALLINT
        override fun visit(i32Type: IntLogical): SqlTypeName = SqlTypeName.INTEGER
        override fun visit(i64Type: BigIntLogical): SqlTypeName = SqlTypeName.BIGINT
        override fun visit(intervalDayType: IntervalDayLogical): SqlTypeName = SqlTypeName.INTERVAL_DAY
        override fun visit(intervalYearType: IntervalYearLogical): SqlTypeName = SqlTypeName.INTERVAL_YEAR
        override fun visit(stringType: StringLogical): SqlTypeName = SqlTypeName.VARCHAR
        override fun visit(timestampType: TimestampLogical): SqlTypeName = SqlTypeName.TIMESTAMP
        override fun visit(timestampTZType: TimestampTZLogical): SqlTypeName = SqlTypeName.TIMESTAMP_WITH_LOCAL_TIME_ZONE
        override fun visit(timeType: TimeLogical): SqlTypeName = SqlTypeName.TIME
        override fun visit(uuidType: UUIDLogical): SqlTypeName = SqlTypeName.VARCHAR
    }
}
