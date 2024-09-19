package io.qpointz.mill.types.logical;

import io.qpointz.mill.proto.LogicalDataType;

public abstract class LogicalTypeIdMapper<T> {

    public T byId(LogicalDataType.LogicalDataTypeId logicalDataTypeId) {
        switch (logicalDataTypeId) {
            case NOT_SPECIFIED_TYPE -> {
                throw new RuntimeException("TypeIsNotRecognized");
            }
            case TINY_INT -> {
                return mapTinyInt();
            }
            case SMALL_INT -> {
                return mapSmallInt();
            }
            case INT -> {
                return mapInt();
            }
            case BIG_INT -> {
                return mapBigInt();
            }
            case BINARY -> {
                return mapBinary();
            }
            case BOOL -> {
                return mapBool();
            }
            case DATE -> {
                return mapDate();
            }
            case FLOAT -> {
                return mapFloat();
            }
            case DOUBLE -> {
                return mapDouble();
            }
            case INTERVAL_DAY -> {
                return mapIntervalDay();
            }
            case INTERVAL_YEAR -> {
                return mapIntervalYear();
            }
            case STRING -> {
                return mapString();
            }
            case TIMESTAMP -> {
                return mapTimestamp();
            }
            case TIMESTAMP_TZ -> {
                return mapTimestampTZ();
            }
            case TIME -> {
                return mapTime();
            }
            case UUID -> {
                return mapUUID();
            }
        }

        throw new RuntimeException("TypeIsNotRecognized");
    }

    protected abstract T mapUUID();

    protected abstract T mapTime();

    protected abstract T mapTimestampTZ();

    protected abstract T mapTimestamp();

    protected abstract T mapString();

    protected abstract T mapIntervalYear();

    protected abstract T mapIntervalDay();

    protected abstract T mapDouble();

    protected abstract T mapFloat();

    protected abstract T mapDate();

    protected abstract T mapBool();

    protected abstract T mapBinary();

    protected abstract T mapInt();

    protected abstract T mapSmallInt();

    protected abstract T mapTinyInt();

    protected abstract T mapBigInt();

}
