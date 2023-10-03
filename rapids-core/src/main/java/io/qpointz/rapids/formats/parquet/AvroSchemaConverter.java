package io.qpointz.rapids.formats.parquet;

import io.qpointz.rapids.types.*;
import io.qpointz.rapids.util.Pair;
import org.apache.avro.Schema;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AvroSchemaConverter {

    public Map<String, RapidsType> convert(org.apache.avro.Schema avro) {
        return avro.getFields().stream()
                .map(this::asRapidsType)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private Pair<String, RapidsType> asRapidsType(Schema.Field avroField) {
        final var fieldSchema = avroField.schema();
        final var fieldName = avroField.name();
        if (fieldSchema.getType()== Schema.Type.UNION) {
            final var unionTypes = avroField.schema().getTypes();
            if (unionTypes.size()>2) {
                throw new RuntimeException("%s field union of more than 2 types. Not supported".formatted(fieldName));
            }

            final var type1 = unionTypes.get(0).getType();

            if (unionTypes.size()==1) {
                return Pair.of(fieldName, fromPrimitiveType(false, type1, avroField.defaultVal()));
            }


            final var type2 = unionTypes.get(1).getType();

            if (type1== Schema.Type.NULL) {
                return Pair.of(fieldName, fromPrimitiveType(true, type2, avroField.defaultVal()));
            }

            if (type2 == Schema.Type.NULL ) {
                return Pair.of(fieldName, fromPrimitiveType(true, type1, avroField.defaultVal()));
            }

            throw new RuntimeException("%s fied is union of non-nullable type. Not suppported".formatted(fieldName));
        }

        return Pair.of(fieldName, fromPrimitiveType(false, fieldSchema.getType(), avroField.defaultVal()));
    }

    private RapidsType fromPrimitiveType(Boolean nullable, Schema.Type avroType, Object defaultValue) {
        final var mayBeDefault = Optional.ofNullable(defaultValue);
        return switch (avroType) {
            case BOOLEAN ->  new BooleanType(nullable, mayBeDefault);
            case DOUBLE ->  new DoubleType(nullable, mayBeDefault);
            case FLOAT -> new FloatType(nullable, mayBeDefault);
            case INT -> new IntType(nullable, mayBeDefault);
            case LONG -> new LongType(nullable, mayBeDefault);
            case STRING -> new StringType(nullable, mayBeDefault);
            default -> throw new IllegalStateException("Unsupported primitive type: " + avroType);
        };
    }

}
