package io.qpointz.mill.types;

import io.qpointz.mill.proto.DataType;
import io.qpointz.mill.types.logical.IntervalDayLogical;
import io.qpointz.mill.types.logical.LogicalTypeIdMapper;
import io.substrait.type.Type;
import io.substrait.type.TypeCreator;
import lombok.val;

import java.util.function.Function;

public class DataTypeToSubstrait extends LogicalTypeIdMapper<Function<TypeCreator, Type>> {

    private static final TypeCreator nullable = Type.withNullability(true);

    private static final TypeCreator nonNull = Type.withNullability(false);


    public Type toSubstrait(DataType dataType) {
        val creator =  dataType.getNullability() == DataType.Nullability.NOT_NULL
                ? nonNull
                : nullable;

        return this.byId(dataType.getType().getTypeId())
                .apply(creator);
    }

    @Override
    protected Function<TypeCreator, Type> mapUUID() {
        return t -> t.UUID;
    }

    @Override
    protected Function<TypeCreator, Type> mapTime() {
        return t -> t.TIME;
    }

    @Override
    protected Function<TypeCreator, Type> mapTimestampTZ() {
        return t -> t.TIMESTAMP_TZ;
    }

    @Override
    protected Function<TypeCreator, Type> mapTimestamp() {
        return t -> t.TIMESTAMP;
    }

    @Override
    protected Function<TypeCreator, Type> mapString() {
        return t -> t.STRING;
    }

    @Override
    protected Function<TypeCreator, Type> mapIntervalYear() {
        return t -> t.INTERVAL_YEAR;
    }

    @Override
    protected Function<TypeCreator, Type> mapIntervalDay() {
        return t -> t.intervalDay(IntervalDayLogical.DEFAULT_PRECISION);
    }

    @Override
    protected Function<TypeCreator, Type> mapDouble() {
        return t -> t.FP64;
    }

    @Override
    protected Function<TypeCreator, Type> mapFloat() {
        return t -> t.FP32;
    }

    @Override
    protected Function<TypeCreator, Type> mapDate() {
        return t -> t.DATE;
    }

    @Override
    protected Function<TypeCreator, Type> mapBool() {
        return t -> t.BOOLEAN;
    }

    @Override
    protected Function<TypeCreator, Type> mapBinary() {
        return t -> t.BINARY;
    }

    @Override
    protected Function<TypeCreator, Type> mapInt() {
        return t -> t.I32;
    }

    @Override
    protected Function<TypeCreator, Type> mapSmallInt() {
        return t -> t.I16;
    }

    @Override
    protected Function<TypeCreator, Type> mapTinyInt() {
        return t -> t.I16;
    }

    @Override
    protected Function<TypeCreator, Type> mapBigInt() {
        return t -> t.I64;
    }
}
