package io.qpointz.delta.types;

import io.qpointz.delta.types.logical.*;

public record DatabaseType(LogicalType type, boolean nullable, int precision, int scale) {

    public static int PREC_SCALE_NOT_APPLICABLE = -1;

    public static DatabaseType of(LogicalType type, boolean nullable) {
        return new DatabaseType(type, nullable, PREC_SCALE_NOT_APPLICABLE, PREC_SCALE_NOT_APPLICABLE);
    }

    public static DatabaseType of(LogicalType type, boolean nullable, int size) {
        return new DatabaseType(type, nullable, size, PREC_SCALE_NOT_APPLICABLE);
    }

    public static DatabaseType of(LogicalType type, boolean nullable, int precision, int scale) {
        return new DatabaseType(type, nullable, precision, scale);
    }

    public static DatabaseType bool(boolean nullable) {
        return of(BoolLogical.INSTANCE, nullable);
    }

    public static DatabaseType string(boolean nullable, int size) {
        return of(StringLogical.INSTANCE, nullable, size);
    }

    public static DatabaseType i64(boolean nullable) {
        return of(BigIntLogical.INSTANCE, nullable);
    }

    public static DatabaseType i16(boolean nullable) {
        return of(SmallIntLogical.INSTANCE, nullable);
    }

    public static DatabaseType i32(boolean nullable) {
        return of(IntLogical.INSTANCE, nullable);
    }

    public static DatabaseType fp32(boolean nullable, int precision, int scale) {
        return of(FloatLogical.INSTANCE, nullable, precision, scale);
    }

    public static DatabaseType fp64(boolean nullable, int precision, int scale) {
        return of(DoubleLogical.INSTANCE, nullable, precision, scale);
    }

    public static DatabaseType binary(boolean nullable, int size) {
        return of(BinaryLogical.INSTANCE,nullable, size);
    }

    public static DatabaseType date(boolean nullable) {
        return of(DateLogical.INSTANCE, nullable);
    }

    public static DatabaseType time(boolean nullable) {
        return of(TimeLogical.INSTANCE, nullable);
    }

    public static DatabaseType timetz(boolean nullable) {
        return of(TimestampTZLogical.INSTANCE, nullable);
    }
}
