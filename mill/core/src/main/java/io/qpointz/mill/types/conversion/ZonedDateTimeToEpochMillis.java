package io.qpointz.mill.types.conversion;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimeToEpochMillis implements ValueConverter<ZonedDateTime, Long> {

    private static final ZoneId REFZONE = ZoneId.of("UTC");

    @Override
    public Long to(ZonedDateTime value) {
        return value.toInstant().atZone(REFZONE).toInstant().toEpochMilli();
    }

    @Override
    public ZonedDateTime from(Long value) {
        return Instant.ofEpochMilli(value).atZone(REFZONE);
    }
}
