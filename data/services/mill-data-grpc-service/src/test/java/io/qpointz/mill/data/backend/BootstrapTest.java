package io.qpointz.mill.data.backend;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BootstrapTest {

    @Test
    void createDb()  {
        assertDoesNotThrow(()->H2Db.createFromResource("sql-scripts/test.sql"));
    }

}
