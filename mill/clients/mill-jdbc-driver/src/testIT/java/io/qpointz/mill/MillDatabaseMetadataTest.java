package io.qpointz.mill;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MillDatabaseMetadataTest extends BaseTest {


    @Test
    void getUserName() throws SQLException {
        val url = String.format("jdbc:mill://%s:%s", getMillAuthHost(), getMillAuthPort());
        try (val con = DriverManager.getConnection(url, getMillUser(), getMillPassword() )) {
            val meta = con.getMetaData();
            assertEquals(getMillUser(), meta.getUserName());
        }
    }

    @Test
    void getUserAnonymousName() throws SQLException, ClassNotFoundException {
        try (val con = getConnection()) {
            val meta = con.getMetaData();
            assertEquals("", meta.getUserName());
        }
    }



}