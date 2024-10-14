package io.qpointz.mill;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
abstract class BaseDriverTestIT extends BaseTest {

    abstract String getConnectionUrl();

    @BeforeEach
    void beforeEach() throws ClassNotFoundException {
        val url = getConnectionUrl();
        Assumptions.assumeFalse(url == null || url.isEmpty());
        Class.forName("io.qpointz.mill.Driver");
    }

    private Connection createConnection() throws SQLException {
        val url = getConnectionUrl();
        val connection = DriverManager.getConnection(url);
        assertNotNull(connection);
        return connection;
    }

    @Test
    public void getMillDriver() throws SQLException, ClassNotFoundException {
        Class.forName("io.qpointz.mill.Driver");
        val driver = DriverManager.getDriver(this.getConnectionUrl());
        assertNotNull(driver);
    }

    @Test()
    public void createConnectionAndStatement() throws ClassNotFoundException, SQLException {
        val connection = createConnection();
        assertNotNull(connection);
        val statement = connection.createStatement();
        assertNotNull(statement);
    }


    @Test
    void executeStatement() throws ClassNotFoundException, SQLException {
        val connection = createConnection();
        val stmt = connection.createStatement();
        stmt.setFetchSize(20);
        val rs = stmt.executeQuery("select * from `ts`.`TEST`");
        var rowId = 0;
        val md = rs.getMetaData();
        assertEquals(7, md.getColumnCount());
        while (rs.next()) {
            rowId++;
        }
        assertTrue(rowId>20);
    }

    @Test
    public void createConnectionAndPreparedStatement() throws ClassNotFoundException, SQLException {
        val con = createConnection();
        val stmt = con.prepareStatement("select * from `ts`.`TEST`");
        val rs = stmt.executeQuery();
        var rowId=0;
        while (rs.next()) {
            rowId++;
        }
        assertTrue(rowId>0);
    }

    @Test
    void metadataGetTables() throws SQLException, ClassNotFoundException {
        try (val connection = createConnection()) {
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
    void metadataGetSchemas() throws SQLException, ClassNotFoundException {
        try (val connection = createConnection()) {
            final var rs = connection.getMetaData().getSchemas();
            val schemas = new HashSet<String>();
            while (rs.next()) {
                schemas.add(rs.getString("TABLE_SCHEM"));
            }
            log.info("Got schemas.Assrtion");
            assertTrue(schemas.size() > 0);
            assertEquals(Set.of("ts", "metadata"), schemas);
        } catch (Exception e) {
            log.error("getSchemas failed", e);
            throw e;
        }
    }

    @Test
    void metadataGetCatalogs() throws SQLException, ClassNotFoundException {
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
    void metadataGetTableTypes() throws SQLException, ClassNotFoundException {
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
