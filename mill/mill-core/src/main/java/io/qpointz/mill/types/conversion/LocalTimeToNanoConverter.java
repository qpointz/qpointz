package io.qpointz.mill.types.conversion;

import java.time.LocalTime;

public class LocalTimeToNanoConverter implements ValueConverter<LocalTime, Long> {
    @Override
    public Long to(LocalTime value) {
        return value.toNanoOfDay();
    }

    @Override
    public LocalTime from(Long value) {
        return LocalTime.ofNanoOfDay(value);
    }
}
