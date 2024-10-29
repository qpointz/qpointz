package io.qpointz.mill.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BootstrapTest {

    @Test
    void createDb()  {
        assertDoesNotThrow(()->H2Db.createFromResource("sql-scripts/test.sql"));
    }

}
