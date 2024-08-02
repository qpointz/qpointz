package io.qpointz.mill.types.conversion;

import java.sql.Date;
import java.time.LocalDate;

public class SqlDateToEpochDayConverter implements ValueConverter<Date, Integer> {

    public static SqlDateToEpochDayConverter DEFAULT = new SqlDateToEpochDayConverter();

    @Override
    public Integer to(Date value) {
        return (int)value.toLocalDate().toEpochDay();
    }

    @Override
    public Date from(Integer value) {
       return Date.valueOf(LocalDate.ofEpochDay(value));
    }

}
