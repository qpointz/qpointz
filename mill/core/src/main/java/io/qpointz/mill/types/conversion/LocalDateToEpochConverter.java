package io.qpointz.mill.types.conversion;

import java.time.LocalDate;

public class LocalDateToEpochConverter implements ValueConverter<LocalDate, Long> {
    @Override
    public Long to(LocalDate value) {
        return value.toEpochDay();
    }

    @Override
    public LocalDate from(Long value) {
        return LocalDate.ofEpochDay(value);
    }
}
