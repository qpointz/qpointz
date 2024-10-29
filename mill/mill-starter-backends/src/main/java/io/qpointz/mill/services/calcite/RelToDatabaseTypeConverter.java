package io.qpointz.mill.services.calcite;

import io.qpointz.mill.types.logical.*;
import io.qpointz.mill.types.sql.DatabaseType;
import lombok.val;
import org.apache.calcite.rel.type.RelDataType;

import java.util.Objects;

public class RelToDatabaseTypeConverter extends RelDataTypeConverter<DatabaseType> {

    public static final RelToDatabaseTypeConverter DEFAULT = new RelToDatabaseTypeConverter();

    private RuntimeException notImplemented(RelDataType relDataType) {
        return new RuntimeException(String.format("Conversion from '%s' not implemented.", Objects.requireNonNull(relDataType.getSqlIdentifier()).toString()));
    }

    @Override
    protected DatabaseType convertSArg(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertFunction(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertMeasure(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertGeometry(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertDynamicStar(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertColumnList(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertCursor(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertOther(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertRow(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertStructured(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertDistinct(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertMap(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertArray(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertMultiSet(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertSymbol(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertNull(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    private DatabaseType from(LogicalType<?,?> logical, RelDataType relDataType) {
        val nullable = relDataType.isNullable();
        val sqlTypeName = relDataType.getSqlTypeName();
        val precision = sqlTypeName.allowsPrec()
                ? relDataType.getPrecision()
                : DatabaseType.PREC_SCALE_NOT_APPLICABLE;

        val scale = sqlTypeName.allowsScale()
                ? relDataType.getScale()
                : DatabaseType.PREC_SCALE_NOT_APPLICABLE;

        return new DatabaseType(logical, nullable, precision, scale);
    }

    @Override
    protected DatabaseType convertVarbinary(RelDataType relDataType) {
        return from(BinaryLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertBinary(RelDataType relDataType) {
        return from(BinaryLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertVarchar(RelDataType relDataType) {
        return from(StringLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertChar(RelDataType relDataType) {
        return from(StringLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertIntervalSecond(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertIntervalMinuteSecond(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertIntervalMinute(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertIntervalHourSecond(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertIntervalHourMinute(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertIntervalHour(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertIntervalDaySecond(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertIntervalDayMinute(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertIntervalDayHour(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertIntervalDay(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertIntervalMonth(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertIntervalYearMonth(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertIntervalYear(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }

    @Override
    protected DatabaseType convertTimestampTZ(RelDataType relDataType) {
        return from(TimestampTZLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertLocalTimestamp(RelDataType relDataType) {
        return from(TimestampLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertTimestamp(RelDataType relDataType) {
        return from(TimestampLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertTimeTZ(RelDataType relDataType) {
        return from(TimestampTZLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertLocalTime(RelDataType relDataType) {
        return from(TimeLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertTime(RelDataType relDataType) {
        return from(TimeLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertDate(RelDataType relDataType) {
        return from(DateLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertDouble(RelDataType relDataType) {
        return from(DoubleLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertReal(RelDataType relDataType) {
        return from(DoubleLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertFloat(RelDataType relDataType) {
        return from(FloatLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertDecimal(RelDataType relDataType) {
        return from(DoubleLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertBigInt(RelDataType relDataType) {
        return from(BigIntLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertInt(RelDataType relDataType) {
        return from(IntLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertSmallInt(RelDataType relDataType) {
        return from(SmallIntLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertTinyInt(RelDataType relDataType) {
        return from(TinyIntLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertBoolean(RelDataType relDataType) {
        return from(BoolLogical.INSTANCE, relDataType);
    }

    @Override
    protected DatabaseType convertAny(RelDataType relDataType) {
        throw notImplemented(relDataType);
    }
}
