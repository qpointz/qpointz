package io.qpointz.mill.types.conversion;

import java.sql.Timestamp;

public class SqlTimestampToEpochMicrosConverter implements ValueConverter<java.sql.Timestamp, Long> {
    public static final SqlTimestampToEpochMicrosConverter DEFAULT = new SqlTimestampToEpochMicrosConverter();


    @Override
    public Long to(Timestamp value) {
        return value.getTime();
    }

    @Override
    public Timestamp from(Long value) {
        return new Timestamp(value);
    }
}
