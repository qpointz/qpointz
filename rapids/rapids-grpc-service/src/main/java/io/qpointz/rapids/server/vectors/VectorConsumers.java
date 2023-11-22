package io.qpointz.rapids.server.vectors;

import io.qpointz.rapids.grpc.*;

public class VectorConsumers<T> {
    public static VectorConsumer<?> of(Field field) {
        return switch (field.getFieldType().getDataType()) {
            case BOOLEAN -> RapidsTypes.BOOLEAN.createConsumer();
            case STRING -> RapidsTypes.STRING.createConsumer();
            case INT32 -> RapidsTypes.INT32.createConsumer();
            case INT64 -> RapidsTypes.INT64.createConsumer();
            case FLOAT -> RapidsTypes.FLOAT.createConsumer();
            case DOUBLE -> RapidsTypes.DOUBLE.createConsumer();
            case BINARY -> RapidsTypes.BYTES.createConsumer();

            case DATE -> RapidsTypes.DATE.createConsumer();
            case TIME -> RapidsTypes.TIME.createConsumer();
            case DATETIME -> RapidsTypes.DATETIME.createConsumer();

            case UNRECOGNIZED -> missingConsumer(field);
            case UNKNOWN_VALUE_TYPE -> missingConsumer(field);
        };
    }


    static VectorConsumer<?> missingConsumer(Field field) {
        throw new RuntimeException(String.format("Missing vector consumer for type '%s'", field.getFieldType().getDataType().name()));
    }
}
