package io.qpointz.mill.services;

import lombok.*;
import org.junit.jupiter.api.Test;

public class BootstrapTest {

    @Test
    void createDb() throws ClassNotFoundException {
        val db = H2Db.createFromResource("sql-scripts/test.sql");
    }

}
