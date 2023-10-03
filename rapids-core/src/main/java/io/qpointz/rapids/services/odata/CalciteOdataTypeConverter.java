package io.qpointz.rapids.services.odata;

import io.qpointz.rapids.calcite.RelSqlTypeConverter;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

public class CalciteOdataTypeConverter extends RelSqlTypeConverter<FullQualifiedName> {
    protected CalciteOdataTypeConverter(RelDataTypeFactory factory) {
        super(factory);
    }

    @Override
    protected FullQualifiedName fromSarg(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromMeasure(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromGeometry(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromDynamicStar(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromColumnList(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromCursor(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromOther(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromRow(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromStructured(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromDistinct(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromMap(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromArray(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromMultiset(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromSymbol(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromAny(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromUnknown(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromNull(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromVarbinary(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromBinary(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalSecond(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalMinuteSecond(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalMinute(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalHourSecond(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalHourMinute(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalHour(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalDaySecond(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalDayMinute(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalDayHour(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalDay(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalMonth(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalYearMonth(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromIntervalYear(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromTimestampWithLocalTimeZone(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromTimestamp(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.Int64.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromTimeWithLocalTimeZone(RelDataType relDataType, RelDataTypeFactory factory) {
        return null;
    }

    @Override
    protected FullQualifiedName fromTime(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromDate(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.Date.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromDouble(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.Double.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromReal(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.Double.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromFloat(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.Double.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromDecimal(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.Double.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromBigInt(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.Int64.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromInteger(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromSmallInt(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.Int16.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromTinyInt(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.Int16.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromBoolean(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromVarchar(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.String.getFullQualifiedName();
    }

    @Override
    protected FullQualifiedName fromChar(RelDataType relDataType, RelDataTypeFactory factory) {
        return EdmPrimitiveTypeKind.String.getFullQualifiedName();
    }
}
