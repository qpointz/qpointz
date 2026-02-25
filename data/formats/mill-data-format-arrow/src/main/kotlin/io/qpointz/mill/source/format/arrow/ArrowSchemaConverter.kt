package io.qpointz.mill.source.format.arrow

import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.SchemaField
import io.qpointz.mill.types.logical.*
import io.qpointz.mill.types.sql.DatabaseType
import org.apache.arrow.vector.types.FloatingPointPrecision
import org.apache.arrow.vector.types.IntervalUnit
import org.apache.arrow.vector.types.TimeUnit
import org.apache.arrow.vector.types.DateUnit
import org.apache.arrow.vector.types.pojo.ArrowType
import org.apache.arrow.vector.types.pojo.Field
import org.apache.arrow.vector.types.pojo.FieldType
import org.apache.arrow.vector.types.pojo.Schema

/**
 * Arrow <-> Mill schema conversion utilities for Arrow IPC formats.
 */
object ArrowSchemaConverter {

    fun toRecordSchema(schema: Schema): RecordSchema {
        val fields = schema.fields.mapIndexed { idx, field ->
            SchemaField(field.name, idx, toDatabaseType(field))
        }
        return RecordSchema(fields)
    }

    fun toArrowSchema(recordSchema: RecordSchema, timezoneByColumn: Map<String, String> = emptyMap()): Schema {
        val fields = recordSchema.fields.map { field ->
            val arrowType = toArrowType(field.type, timezoneByColumn[field.name])
            Field(field.name, FieldType(field.type.nullable(), arrowType, null), emptyList())
        }
        return Schema(fields)
    }

    private fun toDatabaseType(field: Field): DatabaseType {
        val type = field.type
        val nullable = field.isNullable

        return when (type) {
            is ArrowType.Bool -> DatabaseType.bool(nullable)
            is ArrowType.Int -> toIntType(type, nullable)
            is ArrowType.FloatingPoint -> when (type.precision) {
                FloatingPointPrecision.SINGLE -> DatabaseType.fp32(nullable, -1, -1)
                FloatingPointPrecision.DOUBLE -> DatabaseType.fp64(nullable, -1, -1)
                else -> throw IllegalArgumentException("Unsupported Arrow floating precision: ${type.precision}")
            }
            is ArrowType.Utf8, is ArrowType.LargeUtf8 -> DatabaseType.string(nullable, -1)
            is ArrowType.Binary, is ArrowType.LargeBinary -> DatabaseType.binary(nullable, -1)
            is ArrowType.FixedSizeBinary -> {
                if (type.byteWidth == 16) DatabaseType.of(UUIDLogical.INSTANCE, nullable)
                else DatabaseType.binary(nullable, type.byteWidth)
            }
            is ArrowType.Date -> DatabaseType.date(nullable)
            is ArrowType.Time -> DatabaseType.time(nullable)
            is ArrowType.Timestamp -> {
                if (type.timezone.isNullOrBlank()) DatabaseType.of(TimestampLogical.INSTANCE, nullable)
                else DatabaseType.timetz(nullable)
            }
            is ArrowType.Duration -> DatabaseType.of(IntervalDayLogical.INSTANCE, nullable)
            is ArrowType.Interval -> {
                when (type.unit) {
                    IntervalUnit.YEAR_MONTH -> DatabaseType.of(IntervalYearLogical.INSTANCE, nullable)
                    IntervalUnit.DAY_TIME, IntervalUnit.MONTH_DAY_NANO ->
                        DatabaseType.of(IntervalDayLogical.INSTANCE, nullable)
                    else -> throw IllegalArgumentException("Unsupported Arrow interval unit: ${type.unit}")
                }
            }
            else -> throw IllegalArgumentException("Unsupported Arrow type: ${type::class.java.simpleName}")
        }
    }

    private fun toIntType(type: ArrowType.Int, nullable: Boolean): DatabaseType {
        if (!type.isSigned) {
            return when (type.bitWidth) {
                8, 16 -> DatabaseType.i32(nullable)
                32, 64 -> DatabaseType.i64(nullable)
                else -> throw IllegalArgumentException("Unsupported Arrow int width: ${type.bitWidth}")
            }
        }

        return when (type.bitWidth) {
            8 -> DatabaseType.of(TinyIntLogical.INSTANCE, nullable)
            16 -> DatabaseType.i16(nullable)
            32 -> DatabaseType.i32(nullable)
            64 -> DatabaseType.i64(nullable)
            else -> throw IllegalArgumentException("Unsupported Arrow int width: ${type.bitWidth}")
        }
    }

    private fun toArrowType(type: DatabaseType, timezone: String?): ArrowType {
        return when (type.type()) {
            is TinyIntLogical -> ArrowType.Int(8, true)
            is SmallIntLogical -> ArrowType.Int(16, true)
            is IntLogical -> ArrowType.Int(32, true)
            is BigIntLogical -> ArrowType.Int(64, true)
            is BoolLogical -> ArrowType.Bool()
            is FloatLogical -> ArrowType.FloatingPoint(FloatingPointPrecision.SINGLE)
            is DoubleLogical -> ArrowType.FloatingPoint(FloatingPointPrecision.DOUBLE)
            is StringLogical -> ArrowType.Utf8()
            is BinaryLogical -> {
                if (type.precision() > 0) ArrowType.FixedSizeBinary(type.precision())
                else ArrowType.Binary()
            }
            is UUIDLogical -> ArrowType.FixedSizeBinary(16)
            is DateLogical -> ArrowType.Date(DateUnit.DAY)
            is TimeLogical -> ArrowType.Time(TimeUnit.NANOSECOND, 64)
            is TimestampTZLogical -> ArrowType.Timestamp(TimeUnit.MILLISECOND, timezone ?: "UTC")
            is TimestampLogical -> ArrowType.Timestamp(TimeUnit.MILLISECOND, null)
            is IntervalDayLogical -> ArrowType.Duration(TimeUnit.SECOND)
            is IntervalYearLogical -> ArrowType.Interval(IntervalUnit.YEAR_MONTH)
            else -> throw IllegalArgumentException("Unsupported Mill logical type for Arrow: ${type.type().javaClass.simpleName}")
        }
    }
}
