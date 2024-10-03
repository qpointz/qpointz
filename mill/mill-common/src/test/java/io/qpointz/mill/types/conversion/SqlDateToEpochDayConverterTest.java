package io.qpointz.mill.types.conversion;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SqlDateToEpochDayConverterTest {

    @Test
    void fromTest() {
        val fct = SqlDateToEpochDayConverter.DEFAULT.from(1L);
        val exp = java.sql.Date.valueOf(LocalDate.of(1970, 1,2));
        assertEquals(exp, fct);
    }

    @Test
    void toTest() {
        val td = java.sql.Date.valueOf(LocalDate.of(1970, 1,5));
        assertEquals(4, SqlDateToEpochDayConverter.DEFAULT.to(td));
    }

}