package io.qpointz.mill.vectors.sql;

import io.qpointz.mill.types.conversion.*;
import io.qpointz.mill.types.logical.*;
import io.qpointz.mill.types.sql.DatabaseType;
import io.qpointz.mill.types.sql.JdbcDatabaseTypeMapper;
import io.qpointz.mill.types.sql.JdbcTypeInfo;
import io.qpointz.mill.vectors.MappingVectorProducer;
import io.qpointz.mill.vectors.VectorProducer;
import lombok.val;

import java.util.UUID;
import java.util.function.Function;

import static io.qpointz.mill.vectors.MappingVectorProducer.createProducer;

public class ResultSetVectorProducerFactory implements LogicalTypeShuttle<MappingVectorProducer<ResultSetColumnReader,?>> {

    private ResultSetVectorProducerFactory() {
    }

    public static ResultSetVectorProducerFactory DEFAULT = new ResultSetVectorProducerFactory();

    public MappingVectorProducer<ResultSetColumnReader,?> fromJdbcType(JdbcTypeInfo jdbcTypeInfo) {
        val databaseType = JdbcDatabaseTypeMapper.DEFAULT.jdbc(jdbcTypeInfo);
        return fromDatabaseType(databaseType);
    }

    public MappingVectorProducer<ResultSetColumnReader,?> fromDatabaseType(DatabaseType databaseType) {
        return (MappingVectorProducer<ResultSetColumnReader,?>)databaseType.type().accept(this);
    }

    private <E> MappingVectorProducer<ResultSetColumnReader, E> producerOf(VectorProducer<E> producer,
                                                                           Function<ResultSetColumnReader, E> map) {
        return createProducer(producer, map, ResultSetColumnReader::isNull);
    }

    private <E,F> MappingVectorProducer<ResultSetColumnReader, E> convertingProducerOf(VectorProducer<E> producer,
                                                                                       Function<ResultSetColumnReader, F> map,
                                                                                       ValueConverter<F,E> converter) {
        return createProducer(producer, r-> converter.to(map.apply(r)), ResultSetColumnReader::isNull);
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, Integer> visit(TinyIntLogical tinyIntLogicalType) {
        return producerOf(tinyIntLogicalType.getVectorProducer(),
                        rs -> tinyIntLogicalType.valueFrom(rs.getShort()));
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, Integer> visit(SmallIntLogical smallIntLogical) {
        return producerOf(smallIntLogical.getVectorProducer(),
                            ResultSetColumnReader::getInt);
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, Integer> visit(IntLogical intLogicalType) {
        return producerOf(intLogicalType.getVectorProducer(),
                ResultSetColumnReader::getInt);
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, Long> visit(BigIntLogical i64Type) {
        return producerOf(i64Type.getVectorProducer(), ResultSetColumnReader::getLong);
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, byte[]> visit(BinaryLogical binaryType) {
        return producerOf(binaryType.getVectorProducer(),
                ResultSetColumnReader::getBytes
                );
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, Boolean> visit(BoolLogical boolType) {
        return producerOf(boolType.getVectorProducer()
                , ResultSetColumnReader::getBoolean);
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, ?> visit(DateLogical dateType) {
        return convertingProducerOf(dateType.getVectorProducer(),
                                    ResultSetColumnReader::getDate,
                                    SqlDateToEpochDayConverter.DEFAULT
        );
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, ?> visit(FloatLogical fp32Type) {
        return producerOf(fp32Type.getVectorProducer(),
                          ResultSetColumnReader::getFloat);
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, ?> visit(DoubleLogical fp64Type) {
        return producerOf(fp64Type.getVectorProducer(),
                ResultSetColumnReader::getDouble);
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, ?> visit(IntervalDayLogical intervalDayType) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, ?> visit(IntervalYearLogical intervalYearType) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, String> visit(StringLogical stringType) {
        return producerOf(stringType.getVectorProducer(),
                ResultSetColumnReader::getString);
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, ?> visit(TimestampLogical timestampType) {
        return convertingProducerOf(timestampType.getVectorProducer(),
                ResultSetColumnReader::getTimestamp,
                SqlTimestampToEpochMicrosConverter.DEFAULT
        );
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, ?> visit(TimestampTZLogical timestampTZType) {
        return convertingProducerOf(timestampTZType.getVectorProducer(),
                ResultSetColumnReader::getTimestamp,
                SqlTimestampToEpochMicrosConverter.DEFAULT
        );
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, ?> visit(TimeLogical timeType) {
        return convertingProducerOf(timeType.getVectorProducer(),
                ResultSetColumnReader::getTime,
                SqlTimeToMicrosConverter.DEFAULT
        );
    }

    @Override
    public MappingVectorProducer<ResultSetColumnReader, ?> visit(UUIDLogical uuidType) {
        return producerOf(uuidType.getVectorProducer(),
                ResultSetColumnReader::getBytes
        );
    }
}
