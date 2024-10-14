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
        val rs = statement.executeQuery("select * from `ts`.`TEST`");
        var rowId=0;
        while (rs.next()) {
            rowId++;
        }
        assertTrue(rowId>0);
    }

}