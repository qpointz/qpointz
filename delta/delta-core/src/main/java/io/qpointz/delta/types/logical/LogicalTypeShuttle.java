package io.qpointz.delta.types.logical;

public interface LogicalTypeShuttle<T> {
    T visit(TinyIntLogical i64Type);

    T visit(BinaryLogical binaryType);

    T visit(BoolLogical boolType);

    T visit(DateLogical dateType);

    T visit(FloatLogical fp32Type);

    T visit(DoubleLogical fp64Type);

    T visit(SmallIntLogical i16Type);

    T visit(IntLogical i32Type);

    T visit(BigIntLogical i64Type);

    T visit(IntervalDayLogical intervalDayType);

    T visit(IntervalYearLogical intervalYearType);

    T visit(StringLogical stringType);

    T visit(TimestampLogical timestampType);

    T visit(TimestampTZLogical timestampTZType);

    T visit(TimeLogical timeType);

    T visit(UUIDLogical uuidType);
}
