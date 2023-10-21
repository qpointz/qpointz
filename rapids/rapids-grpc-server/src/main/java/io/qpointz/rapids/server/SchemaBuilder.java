package io.qpointz.rapids.server;


import io.qpointz.rapids.grpc.*;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeField;

public class SchemaBuilder {

    public static Schema build(RelDataType relDataType) {
        final var schema = Schema.newBuilder();
        for (final var field : relDataType.getFieldList()) {
            final var tableField = buildField(field);
            schema.addFields(tableField);
        }
        return schema.build();
    }

    public static Field buildField(RelDataTypeField relDataTypeField) {
        final var fieldBuilder = Field.newBuilder()
                .setName(relDataTypeField.getName())
                .setIndex(relDataTypeField.getIndex());

        final var fieldType = relDataTypeField.getType();
        final var sqlType = fieldType.getSqlTypeName();

        final DataType dataType = switch (sqlType) {
            case BOOLEAN -> onBoolean(fieldType);

            case CHAR, VARCHAR -> onString(fieldType);

            case BINARY, VARBINARY -> onBinary(fieldType);

            case BIGINT, TINYINT, SMALLINT, INTEGER  -> onInteger(fieldType);

            case DECIMAL, FLOAT, REAL, DOUBLE -> onNumeric(fieldType);

            case DATE -> onDate(fieldType);

            case TIME -> onTime(fieldType, false);
            case TIME_WITH_LOCAL_TIME_ZONE -> onTime(fieldType, true);

            case TIMESTAMP -> onTimestamp(fieldType, false);
            case TIMESTAMP_WITH_LOCAL_TIME_ZONE -> onTimestamp(fieldType, true);


            case INTERVAL_YEAR -> notSupported(fieldType);
            case INTERVAL_YEAR_MONTH -> notSupported(fieldType);
            case INTERVAL_MONTH -> notSupported(fieldType);
            case INTERVAL_DAY -> notSupported(fieldType);
            case INTERVAL_DAY_HOUR -> notSupported(fieldType);
            case INTERVAL_DAY_MINUTE -> notSupported(fieldType);
            case INTERVAL_DAY_SECOND -> notSupported(fieldType);
            case INTERVAL_HOUR -> notSupported(fieldType);
            case INTERVAL_HOUR_MINUTE -> notSupported(fieldType);
            case INTERVAL_HOUR_SECOND -> notSupported(fieldType);
            case INTERVAL_MINUTE -> notSupported(fieldType);
            case INTERVAL_MINUTE_SECOND -> notSupported(fieldType);
            case INTERVAL_SECOND -> notSupported(fieldType);

            case NULL -> notSupported(fieldType);
            case UNKNOWN -> notSupported(fieldType);

            case ANY -> notSupported(fieldType);
            case SYMBOL -> notSupported(fieldType);

            case MULTISET, ARRAY, MAP -> notSupported(fieldType);

            case DISTINCT -> notSupported(fieldType);
            case STRUCTURED -> notSupported(fieldType);
            case ROW -> notSupported(fieldType);
            case OTHER -> notSupported(fieldType);
            case CURSOR -> notSupported(fieldType);
            case COLUMN_LIST -> notSupported(fieldType);
            case DYNAMIC_STAR -> notSupported(fieldType);
            case GEOMETRY -> notSupported(fieldType);
            case MEASURE -> notSupported(fieldType);
            case SARG -> notSupported(fieldType);
        };

        return fieldBuilder
                .setFieldType(dataType)
                .build();
    }

    private static DataType onNumeric(RelDataType fieldType) {
        final var builder = switch (fieldType.getSqlTypeName()) {
            case FLOAT -> onFieldType(fieldType, ValueType.FLOAT);
            case DOUBLE -> onFieldType(fieldType, ValueType.DOUBLE);
            default -> throw new IllegalArgumentException(
                    String.format("%s non numeric type", fieldType.getSqlTypeName().getName()));
        };
        return builder
                .setPrecision(fieldType.getPrecision())
                .setScale(fieldType.getScale())
                .build();
    }

    private static DataType onDate(RelDataType fieldType) {
        return onFieldType(fieldType, ValueType.DATE)
                .build();
    }

    private static DataType onTime(RelDataType fieldType, boolean withTimeZone) {
        return onFieldType(fieldType, ValueType.TIME)
                .setWithTimezone(withTimeZone)
                .build();
    }

    private static DataType onTimestamp(RelDataType fieldType, boolean withTimeZone) {
        return onFieldType(fieldType, ValueType.TIMESTAMP)
                .setWithTimezone(withTimeZone)
                .build();
    }

    private static DataType.Builder onFieldType(RelDataType fieldType, ValueType valueType) {
        return DataType.newBuilder()
                .setNullable(fieldType.isNullable())
                .setDataType(valueType);
    }

    private static DataType onInteger(RelDataType fieldType) {
        final var builder = switch (fieldType.getSqlTypeName()) {
            case BIGINT -> onFieldType(fieldType, ValueType.INT64);
            case INTEGER -> onFieldType(fieldType,ValueType.INT32);
            default -> throw new IllegalArgumentException(
                    String.format("%s non integer type", fieldType.getSqlTypeName().getName()));
        };
        return builder.build();
    }

    private static DataType onBinary(RelDataType fieldType) {
        return onFieldType(fieldType, ValueType.BINARY)
                .setLength(fieldType.getPrecision())
                .build();
    }

    private static DataType onString(RelDataType fieldType) {
        return onFieldType(fieldType, ValueType.STRING)
                .setLength(fieldType.getPrecision())
                .build();
    }

    private static DataType onBoolean(RelDataType fieldType) {
        return onFieldType(fieldType ,ValueType.BOOLEAN)
                .build();
    }

    private static DataType notSupported(RelDataType fieldType) {
        final var message = String.format("%s SQL Type not supported", fieldType.getSqlTypeName().getName());
        throw new IllegalArgumentException(message);
    }

}
