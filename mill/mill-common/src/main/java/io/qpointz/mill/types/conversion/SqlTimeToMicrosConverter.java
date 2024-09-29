package io.qpointz.mill.types.conversion;

import java.sql.Time;

public class SqlTimeToMicrosConverter implements ValueConverter<Time,Long> {

    public static final SqlTimeToMicrosConverter DEFAULT = new SqlTimeToMicrosConverter();

    @Override
    public Long to(Time value) {
        return value.getTime();
    }

    @Override
    public Time from(Long value) {
        return new Time(value);
    }
}
