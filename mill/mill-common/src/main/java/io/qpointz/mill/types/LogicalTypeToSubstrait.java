package io.qpointz.mill.types;

import io.qpointz.mill.types.logical.*;
import io.substrait.type.Type;
import io.substrait.type.TypeCreator;

public class LogicalTypeToSubstrait implements LogicalTypeShuttle<Type> {

    private final TypeCreator typeCreator;

    public LogicalTypeToSubstrait(boolean nullable) {
        this(Type.withNullability(nullable));
    }

    public LogicalTypeToSubstrait(TypeCreator typeCreator) {
        this.typeCreator = typeCreator;
    }

    @Override
    public Type visit(TinyIntLogical i64Type) {
        return this.typeCreator.I8;
    }

    @Override
    public Type visit(BinaryLogical binaryType) {
        return this.typeCreator.BINARY;
    }

    @Override
    public Type visit(BoolLogical boolType) {
        return this.typeCreator.BOOLEAN;
    }

    @Override
    public Type visit(DateLogical dateType) {
        return this.typeCreator.DATE;
    }

    @Override
    public Type visit(FloatLogical fp32Type) {
        return this.typeCreator.FP32;
    }

    @Override
    public Type visit(DoubleLogical fp64Type) {
        return this.typeCreator.FP64;
    }

    @Override
    public Type visit(SmallIntLogical i16Type) {
        return this.typeCreator.I16;
    }

    @Override
    public Type visit(IntLogical i32Type) {
        return this.typeCreator.I32;
    }

    @Override
    public Type visit(BigIntLogical i64Type) {
        return this.typeCreator.I64;
    }

    @Override
    public Type visit(IntervalDayLogical intervalDayType) {
        return this.typeCreator.intervalDay(intervalDayType.precision());
    }

    @Override
    public Type visit(IntervalYearLogical intervalYearType) {
        return this.typeCreator.INTERVAL_YEAR;
    }

    @Override
    public Type visit(StringLogical stringType) {
        return this.typeCreator.STRING;
    }

    @Override
    public Type visit(TimestampLogical timestampType) {
        return this.typeCreator.TIMESTAMP;
    }

    @Override
    public Type visit(TimestampTZLogical timestampTZType) {
        return this.typeCreator.TIMESTAMP_TZ;
    }

    @Override
    public Type visit(TimeLogical timeType) {
        return this.typeCreator.TIME;
    }

    @Override
    public Type visit(UUIDLogical uuidType) {
        return this.typeCreator.UUID;
    }
}
