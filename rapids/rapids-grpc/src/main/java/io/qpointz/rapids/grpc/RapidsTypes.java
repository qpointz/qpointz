package io.qpointz.rapids.grpc;

import com.google.protobuf.ByteString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;

public final class RapidsTypes {

    public static RapidsType<Boolean, Boolean, BoolVector, BoolVector.Builder> BOOLEAN
            = physicalType(ValueType.BOOLEAN, false, VectorHandlers.BOOLEAN
            , (rs, idx) -> { try {return rs.getBoolean(idx);} catch (SQLException e) {throw new RuntimeException(e);}}
    );

    public static RapidsType<String, String, StringVector, StringVector.Builder> STRING
            = physicalType(ValueType.STRING, "", VectorHandlers.STRING
            , (rs, idx) -> { try {return rs.getString(idx);} catch (SQLException e) {throw new RuntimeException(e);}}
    );

    public static RapidsType<Integer, Integer, Int32Vector, Int32Vector.Builder> INT32
            = physicalType(ValueType.INT32, 0, VectorHandlers.INT32
            , (rs, idx) -> { try {return rs.getInt(idx);} catch (SQLException e) {throw new RuntimeException(e);}}
    );

    public static RapidsType<Long, Long, Int64Vector, Int64Vector.Builder> INT64
            = physicalType(ValueType.INT64, 0L, VectorHandlers.INT64
            , (rs, idx) -> { try {return rs.getLong(idx);} catch (SQLException e) {throw new RuntimeException(e);}}
    );

    public static RapidsType<Double, Double, DoubleVector, DoubleVector.Builder> DOUBLE
            = physicalType(ValueType.DOUBLE, 0D, VectorHandlers.DOUBLE
            , (rs, idx) -> { try {return rs.getDouble(idx);} catch (SQLException e) {throw new RuntimeException(e);}}
    );

    public static RapidsType<Float, Float, FloatVector, FloatVector.Builder> FLOAT
            = physicalType(ValueType.FLOAT, 0F, VectorHandlers.FLOAT
            , (rs, idx) -> { try {return rs.getFloat(idx);} catch (SQLException e) {throw new RuntimeException(e);}}
    );

    public static RapidsType<ByteString, ByteString, ByteVector, ByteVector.Builder> BYTES
            = physicalType(ValueType.BINARY, ByteString.empty(), VectorHandlers.BYTES, RapidsTypes::readBytes);

    private static ByteString readBytes(ResultSet rs, int columnIdx) {
        try {
            final byte[] blob = rs.getBytes(columnIdx);
            if (blob==null || blob.length ==0) {
                return ByteString.empty();
            }
            return ByteString.copyFrom(blob);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static <TPhysType, TPhysVector, TPhysBuilder> RapidsType<TPhysType, TPhysType, TPhysVector, TPhysBuilder> physicalType(
            ValueType valueType, TPhysType nullValue,
            VectorHandler<TPhysType, TPhysVector, TPhysBuilder> vectorHandler,
            BiFunction<ResultSet, Integer, TPhysType> fromResultSet)
    {
      return new RapidsType<>(valueType, valueType, nullValue, vectorHandler, t-> t, t->t, fromResultSet);
    }


    private final static LocalDate DATE_NULL = LocalDate.EPOCH;
    public static RapidsType<LocalDate, Long, Int64Vector, Int64Vector.Builder> DATE =
        new RapidsType<>(
                ValueType.INT64, ValueType.DATE, DATE_NULL, VectorHandlers.INT64,
                ld -> ChronoUnit.DAYS.between(LocalDate.EPOCH, ld), LocalDate.EPOCH::plusDays,
                RapidsTypes::readLocalDateOrNull
        );

    private static LocalDate readLocalDateOrNull(ResultSet rs, int idx) {
        try {
            final var ld = rs.getDate(idx);
            return rs.wasNull()
                    ? DATE_NULL
                    : ld.toLocalDate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final LocalTime TIME_NULL = LocalTime.MIN;
    public static RapidsType<LocalTime, Long, Int64Vector, Int64Vector.Builder> TIME =
            new RapidsType<>(
                    ValueType.INT64, ValueType.TIME, TIME_NULL, VectorHandlers.INT64,
                    ld -> ChronoUnit.NANOS.between(LocalTime.MIN, ld), LocalTime.MIN::plusNanos,
                    RapidsTypes::readLocalTimeOrNull
            );

    private static LocalTime readLocalTimeOrNull(ResultSet rs, int idx) {
        try {
            final var ld = rs.getTimestamp(idx);
            return rs.wasNull()
                    ? TIME_NULL
                    : ld.toLocalDateTime().toLocalTime();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final LocalDateTime TIMESTAMP_NULL = LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    public static RapidsType<LocalDateTime, Long, Int64Vector, Int64Vector.Builder> DATETIME =
            new RapidsType<>(
                    ValueType.INT64, ValueType.DATETIME, TIMESTAMP_NULL, VectorHandlers.INT64,
                    ld -> ChronoUnit.NANOS.between(TIMESTAMP_NULL, ld.atOffset(ZoneOffset.UTC)),
                    k-> TIMESTAMP_NULL.plusNanos(k),
                    RapidsTypes::readInstantOrNull
            );

    private static LocalDateTime readInstantOrNull(ResultSet rs, int idx) {
        try {
            final var ld = rs.getTimestamp(idx);
            return rs.wasNull()
                    ? TIMESTAMP_NULL
                    : ld.toLocalDateTime();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
