package io.qpointz.mill.types.conversion;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LocalDateTimeToEpochMilli implements ValueConverter<LocalDateTime, Long> {

    private static final ZoneOffset REFOFFSET = ZoneOffset.ofHours(0);

    @Override
    public Long to(LocalDateTime value) {
        return value.toInstant(REFOFFSET).toEpochMilli();
    }

    @Override
    public LocalDateTime from(Long value) {
        return Instant.ofEpochMilli(value).atOffset(REFOFFSET).toLocalDateTime();
    }
}
