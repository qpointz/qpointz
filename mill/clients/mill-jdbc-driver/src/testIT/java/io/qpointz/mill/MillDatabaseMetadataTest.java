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
    void getTables() throws SQLException, ClassNotFoundException {
        try (val connection = getConnection()) {
            final var md = connection.getMetaData();
            val rs = md.getTables(null, null, null, null);
            int rowId = 0;
            while (rs.next()) {
                rowId++;
            }
            assertTrue(rowId > 0);
        }
    }


    @Test
    void getSchemas() throws SQLException, ClassNotFoundException {
        try (val connection = getConnection()) {
            final var rs = connection.getMetaData().getSchemas();
            val schemas = new HashSet<String>();
            while (rs.next()) {
                schemas.add(rs.getString("TABLE_SCHEM"));
            }
            log.info("Got schemas.Assrtion");
            assertTrue(schemas.size() > 0);
            assertEquals(Set.of("airlines", "metadata"), schemas);
        } catch (Exception e) {
            log.error("getSchemas failed", e);
            throw e;
        }
    }

    @Test
    void getCatalogs() throws SQLException, ClassNotFoundException {
        try (val connection = getConnection()) {
            final var rs = connection.getMetaData().getCatalogs();
            val schemas = new HashSet<String>();
            while (rs.next()) {
                schemas.add(rs.getString("TABLE_CAT"));
            }
            assertTrue(schemas.size() == 0);
        }
    }

    @Test
    void getTableTypes() throws SQLException, ClassNotFoundException {
        try (val connection = getConnection()) {
            final var rs = connection.getMetaData().getTableTypes();
            val types = new HashSet<String>();
            while (rs.next()) {
                types.add(rs.getString("TABLE_TYPE"));
            }
            assertEquals(Set.of("TABLE", "VIEW"), types);
        }
    }



}