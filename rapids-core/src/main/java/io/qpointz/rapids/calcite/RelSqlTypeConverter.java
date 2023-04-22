package io.qpointz.rapids.calcite;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;

public abstract class RelSqlTypeConverter<T> {

    private final RelDataTypeFactory factory;

    protected RelSqlTypeConverter(RelDataTypeFactory factory) {
        this.factory = factory;
    }

    public T fromCalcite(RelDataType relDataType) {
        final var targetType =  switch (relDataType.getSqlTypeName()) {
            case CHAR  -> this.fromChar(relDataType, this.factory);
            case VARCHAR -> this.fromVarchar(relDataType, this.factory);
            case BOOLEAN -> this.fromBoolean(relDataType, this.factory);
            case TINYINT -> this.fromTinyInt(relDataType, this.factory);
            case SMALLINT -> this.fromSmallInt(relDataType, this.factory);
            case INTEGER -> this.fromInteger(relDataType, this.factory);
            case BIGINT -> this.fromBigInt(relDataType, this.factory);
            case DECIMAL -> this.fromDecimal(relDataType, this.factory);
            case FLOAT -> this.fromFloat(relDataType, this.factory);
            case REAL -> this.fromReal(relDataType, this.factory);
            case DOUBLE -> this.fromDouble(relDataType, this.factory);

            case DATE -> this.fromDate(relDataType, this.factory);
            case TIME -> this.fromTime(relDataType, this.factory);
            case TIME_WITH_LOCAL_TIME_ZONE -> this.fromTimeWithLocalTimeZone(relDataType, this.factory);
            case TIMESTAMP -> this.fromTimestamp(relDataType, this.factory);
            case TIMESTAMP_WITH_LOCAL_TIME_ZONE -> this.fromTimestampWithLocalTimeZone(relDataType, this.factory);
            case INTERVAL_YEAR -> this.fromIntervalYear(relDataType, this.factory);
            case INTERVAL_YEAR_MONTH -> this.fromIntervalYearMonth(relDataType, this.factory);
            case INTERVAL_MONTH -> this.fromIntervalMonth(relDataType, this.factory);
            case INTERVAL_DAY -> this.fromIntervalDay(relDataType, this.factory);
            case INTERVAL_DAY_HOUR -> this.fromIntervalDayHour(relDataType, this.factory);
            case INTERVAL_DAY_MINUTE -> this.fromIntervalDayMinute(relDataType, this.factory);
            case INTERVAL_DAY_SECOND -> this.fromIntervalDaySecond(relDataType, this.factory);
            case INTERVAL_HOUR -> this.fromIntervalHour(relDataType, this.factory);
            case INTERVAL_HOUR_MINUTE -> this.fromIntervalHourMinute(relDataType, this.factory);
            case INTERVAL_HOUR_SECOND -> this.fromIntervalHourSecond(relDataType, this.factory);
            case INTERVAL_MINUTE -> this.fromIntervalMinute(relDataType, this.factory);
            case INTERVAL_MINUTE_SECOND -> this.fromIntervalMinuteSecond(relDataType, this.factory);
            case INTERVAL_SECOND -> this.fromIntervalSecond(relDataType, this.factory);

            case BINARY -> this.fromBinary(relDataType, this.factory);
            case VARBINARY -> this.fromVarbinary(relDataType, this.factory);
            case NULL -> this.fromNull(relDataType, this.factory);
            case UNKNOWN -> this.fromUnknown(relDataType, this.factory);
            case ANY -> this.fromAny(relDataType, this.factory);
            case SYMBOL -> this.fromSymbol(relDataType, this.factory);
            case MULTISET -> this.fromMultiset(relDataType, this.factory);
            case ARRAY -> this.fromArray(relDataType, this.factory);
            case MAP -> this.fromMap(relDataType, this.factory);
            case DISTINCT -> this.fromDistinct(relDataType, this.factory);
            case STRUCTURED -> this.fromStructured(relDataType, this.factory);
            case ROW -> this.fromRow(relDataType, this.factory);
            case OTHER -> this.fromOther(relDataType, this.factory);
            case CURSOR -> this.fromCursor(relDataType, this.factory);
            case COLUMN_LIST -> this.fromColumnList(relDataType, this.factory);
            case DYNAMIC_STAR -> this.fromDynamicStar(relDataType, this.factory);
            case GEOMETRY -> this.fromGeometry(relDataType, this.factory);
            case MEASURE -> this.fromMeasure(relDataType, this.factory);
            case SARG -> this.fromSarg(relDataType, this.factory);
        };
        if (targetType!=null) {
            return targetType;
        }
        throw new IllegalArgumentException("Unknown conversion to type '%s'".formatted(relDataType.getSqlTypeName()));
    }

    protected abstract T fromSarg(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromMeasure(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromGeometry(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromDynamicStar(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromColumnList(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromCursor(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromOther(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromRow(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromStructured(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromDistinct(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromMap(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromArray(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromMultiset(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromSymbol(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromAny(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromUnknown(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromNull(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromVarbinary(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromBinary(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalSecond(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalMinuteSecond(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalMinute(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalHourSecond(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalHourMinute(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalHour(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalDaySecond(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalDayMinute(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalDayHour(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalDay(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalMonth(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalYearMonth(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromIntervalYear(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromTimestampWithLocalTimeZone(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromTimestamp(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromTimeWithLocalTimeZone(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromTime(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromDate(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromDouble(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromReal(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromFloat(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromDecimal(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromBigInt(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromInteger(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromSmallInt(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromTinyInt(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromBoolean(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromVarchar(RelDataType relDataType, RelDataTypeFactory factory);

    protected abstract T fromChar(RelDataType relDataType, RelDataTypeFactory factory);

}
