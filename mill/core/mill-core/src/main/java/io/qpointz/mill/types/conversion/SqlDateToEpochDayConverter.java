package io.qpointz.mill.types.conversion;

import java.sql.Date;
import java.time.LocalDate;

public class SqlDateToEpochDayConverter implements ValueConverter<Date, Long> {

    public static final SqlDateToEpochDayConverter DEFAULT = new SqlDateToEpochDayConverter();

    @Override
    public Long to(Date value) {
        return value.toLocalDate().toEpochDay();
    }

    @Override
    public Date from(Long value) {
       return Date.valueOf(LocalDate.ofEpochDay(value));
    }

}
