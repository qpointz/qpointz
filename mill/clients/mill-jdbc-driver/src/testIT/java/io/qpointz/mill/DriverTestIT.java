package io.qpointz.mill;

import lombok.extern.java.Log;
import lombok.val;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Log
class DriverTestIT extends BaseTest {

    @Test
    public void getMillDriver() throws SQLException, ClassNotFoundException {
        Class.forName("io.qpointz.mill.Driver");
        val driver = DriverManager.getDriver(getMillUrl());
        assertNotNull(driver);
    }

    @Test()
    public void createConnectionAndStatement() throws ClassNotFoundException, SQLException {
        Class.forName("io.qpointz.mill.Driver");
        val url = getMillUrl();
        val connection = DriverManager.getConnection(url);
        assertNotNull(connection);
        val statement = connection.createStatement();
        assertNotNull(statement);
    }

    @Test
    void executeStatement() throws ClassNotFoundException, SQLException {
        Class.forName("io.qpointz.mill.Driver");
        val url = getMillUrl();
        val connection = DriverManager.getConnection(url);
        val stmt = connection.createStatement();
        stmt.setFetchSize(20);
        val rs = stmt.executeQuery("select * from `airlines`.`passenger`");
        var rowId = 0;
        val md = rs.getMetaData();
        assertEquals(6, md.getColumnCount());
        while (rs.next()) {
            rowId++;
        }
        assertTrue(rowId>20);
    }


    @Test
    void connectUsingString() throws SQLException, ClassNotFoundException {
        Class.forName("io.qpointz.mill.Driver");
        val url = String.format("jdbc:mill://%s:%s?user=%s&password=%s&bearerToken=%s&tlsKeyCertChain=%s&tlsKeyPrivateKey=%s&tlsTrustRootCert=%s",
                getMillAuthTlsHost(),
                getMillAuthTlsPort(),
                getMillUser(),
                getMillPassword(),
                getMillJwtToken(),
                getTlsCertChain(),
                getTlsCertPk(),
                getTlsRootCa()
        );
        log.info(String.format("Url:%s", url));
        val connection = DriverManager.getConnection(url);
        assertNotNull(connection);
        val statement = connection.createStatement();
        assertNotNull(statement);
        val rs = statement.executeQuery("select * from `airlines`.`passenger`");
        var rowId=0;
        while (rs.next()) {
            rowId++;
        }
        assertTrue(rowId>0);
    }

}