package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.grpc.Field;

public class VectorConsumers<T> {
    public static VectorConsumer<?> of(Field field) {
        return switch (field.getFieldType().getDataType()) {
            case BOOLEAN -> new BooleanVectorConsumer();
            case STRING -> new StringVectorConsumer();

            case INT32 -> new Int32VectorConsumer();
            case INT64 -> new Int64VectorConsumer();

            case FLOAT -> new FloatVectorConsumer();
            case DOUBLE -> new DoubleVectorConsumer();

            case BINARY -> new BytesVectorConsumer();

            case DATE -> new DateVectorConsumer();
            case TIME -> new TimeVectorConsumer();
            case TIMESTAMP -> new TimestampVectorConsumer();

            case UNRECOGNIZED -> missingConsumer(field);
            case UNKNOWN_VALUE_TYPE -> missingConsumer(field);
        };
    }


    static VectorConsumer<?> missingConsumer(Field field) {
        throw new RuntimeException(String.format("Missing vector consumer for type '%s'", field.getFieldType().getDataType().name()));
    }
}
