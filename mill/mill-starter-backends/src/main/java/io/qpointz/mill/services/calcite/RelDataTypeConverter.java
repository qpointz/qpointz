package io.qpointz.mill.services.calcite;

import lombok.val;
import org.apache.calcite.rel.type.RelDataType;

public abstract class RelDataTypeConverter<T> {

    public T convert(RelDataType relDataType) {
        val sqlTypeName = relDataType.getSqlTypeName();
        return switch (sqlTypeName) {
            case BOOLEAN -> convertBoolean(relDataType);
            case TINYINT -> convertTinyInt(relDataType);
            case SMALLINT -> convertSmallInt(relDataType);
            case INTEGER -> convertInt(relDataType);
            case BIGINT -> convertBigInt(relDataType);
            case DECIMAL -> convertDecimal(relDataType);
            case FLOAT -> convertFloat(relDataType);
            case REAL -> convertReal(relDataType);
            case DOUBLE -> convertDouble(relDataType);
            case DATE -> convertDate(relDataType);
            case TIME -> convertTime(relDataType);
            case TIME_WITH_LOCAL_TIME_ZONE -> convertLocalTime(relDataType);
            case TIME_TZ -> convertTimeTZ(relDataType);
            case TIMESTAMP -> convertTimestamp(relDataType);
            case TIMESTAMP_WITH_LOCAL_TIME_ZONE -> convertLocalTimestamp(relDataType);
            case TIMESTAMP_TZ -> convertTimestampTZ(relDataType);
            case INTERVAL_YEAR -> convertIntervalYear(relDataType);
            case INTERVAL_YEAR_MONTH -> convertIntervalYearMonth(relDataType);
            case INTERVAL_MONTH -> convertIntervalMonth(relDataType);
            case INTERVAL_DAY -> convertIntervalDay(relDataType);
            case INTERVAL_DAY_HOUR -> convertIntervalDayHour(relDataType);
            case INTERVAL_DAY_MINUTE -> convertIntervalDayMinute(relDataType);
            case INTERVAL_DAY_SECOND -> convertIntervalDaySecond(relDataType);
            case INTERVAL_HOUR -> convertIntervalHour(relDataType);
            case INTERVAL_HOUR_MINUTE -> convertIntervalHourMinute(relDataType);
            case INTERVAL_HOUR_SECOND -> convertIntervalHourSecond(relDataType);
            case INTERVAL_MINUTE -> convertIntervalMinute(relDataType);
            case INTERVAL_MINUTE_SECOND -> convertIntervalMinuteSecond(relDataType);
            case INTERVAL_SECOND -> convertIntervalSecond(relDataType);
            case CHAR -> convertChar(relDataType);
            case VARCHAR -> convertVarchar(relDataType);
            case BINARY -> convertBinary(relDataType);
            case VARBINARY -> convertVarbinary(relDataType);
            case NULL -> convertNull(relDataType);
            case UNKNOWN -> convertNull(relDataType);
            case ANY -> convertAny(relDataType);
            case SYMBOL -> convertSymbol(relDataType);
            case MULTISET -> convertMultiSet(relDataType);
            case ARRAY -> convertArray(relDataType);
            case MAP -> convertMap(relDataType);
            case DISTINCT -> convertDistinct(relDataType);
            case STRUCTURED -> convertStructured(relDataType);
            case ROW -> convertRow(relDataType);
            case OTHER -> convertOther(relDataType);
            case CURSOR -> convertCursor(relDataType);
            case COLUMN_LIST -> convertColumnList(relDataType);
            case DYNAMIC_STAR -> convertDynamicStar(relDataType);
            case GEOMETRY -> convertGeometry(relDataType);
            case MEASURE -> convertMeasure(relDataType);
            case FUNCTION -> convertFunction(relDataType);
            case SARG -> convertSArg(relDataType);
            case UUID -> convertUUID(relDataType);
            case VARIANT -> convertVariant(relDataType);
        };
    }

    protected abstract T convertSArg(RelDataType relDataType);

    protected abstract T convertFunction(RelDataType relDataType);

    protected abstract T convertMeasure(RelDataType relDataType);

    protected abstract T convertGeometry(RelDataType relDataType);

    protected abstract T convertDynamicStar(RelDataType relDataType);

    protected abstract T convertColumnList(RelDataType relDataType);

    protected abstract T convertCursor(RelDataType relDataType);

    protected abstract T convertOther(RelDataType relDataType);

    protected abstract T convertRow(RelDataType relDataType);

    protected abstract T convertStructured(RelDataType relDataType);

    protected abstract T convertDistinct(RelDataType relDataType);

    protected abstract T convertMap(RelDataType relDataType);

    protected abstract T convertArray(RelDataType relDataType);

    protected abstract T convertMultiSet(RelDataType relDataType);

    protected abstract T convertSymbol(RelDataType relDataType);

    protected abstract T convertNull(RelDataType relDataType);

    protected abstract T convertVarbinary(RelDataType relDataType);

    protected abstract T convertBinary(RelDataType relDataType);

    protected abstract T convertVarchar(RelDataType relDataType);

    protected abstract T convertChar(RelDataType relDataType);

    protected abstract T convertIntervalSecond(RelDataType relDataType);

    protected abstract T convertIntervalMinuteSecond(RelDataType relDataType);

    protected abstract T convertIntervalMinute(RelDataType relDataType);

    protected abstract T convertIntervalHourSecond(RelDataType relDataType);

    protected abstract T convertIntervalHourMinute(RelDataType relDataType);

    protected abstract T convertIntervalHour(RelDataType relDataType);

    protected abstract T convertIntervalDaySecond(RelDataType relDataType);

    protected abstract T convertIntervalDayMinute(RelDataType relDataType);

    protected abstract T convertIntervalDayHour(RelDataType relDataType);

    protected abstract T convertIntervalDay(RelDataType relDataType);

    protected abstract T convertIntervalMonth(RelDataType relDataType);

    protected abstract T convertIntervalYearMonth(RelDataType relDataType);

    protected abstract T convertIntervalYear(RelDataType relDataType);

    protected abstract T convertTimestampTZ(RelDataType relDataType);

    protected abstract T convertLocalTimestamp(RelDataType relDataType);

    protected abstract T convertTimestamp(RelDataType relDataType);

    protected abstract T convertTimeTZ(RelDataType relDataType);

    protected abstract T convertLocalTime(RelDataType relDataType);

    protected abstract T convertTime(RelDataType relDataType);

    protected abstract T convertDate(RelDataType relDataType);

    protected abstract T convertDouble(RelDataType relDataType);

    protected abstract T convertReal(RelDataType relDataType);

    protected abstract T convertFloat(RelDataType relDataType);

    protected abstract T convertDecimal(RelDataType relDataType);

    protected abstract T convertBigInt(RelDataType relDataType);

    protected abstract T convertInt(RelDataType relDataType);

    protected abstract T convertSmallInt(RelDataType relDataType);

    protected abstract T convertTinyInt(RelDataType relDataType);

    protected abstract T convertBoolean(RelDataType relDataType);

    protected abstract T convertAny(RelDataType relDataType);

    protected abstract T convertUUID(RelDataType relDataType);

    protected abstract T convertVariant(RelDataType relDataType);


}
