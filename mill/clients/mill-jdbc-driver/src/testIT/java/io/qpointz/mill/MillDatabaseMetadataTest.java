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
    void getUserNameBasicAuth() throws SQLException {
        val url = String.format("jdbc:mill://%s:%s?user=%s&password=%s&tlsKeyCertChain=%s&tlsKeyPrivateKey=%s&tlsTrustRootCert=%s",
                getMillAuthTlsHost(),
                getMillPort(),
                getMillUser(),
                getMillPassword(),
                getTlsCertChain(),
                getTlsCertPk(),
                getTlsRootCa()
        );
        try (val con = DriverManager.getConnection(url)) {
            val meta = con.getMetaData();
            assertEquals(getMillUser(), meta.getUserName());
        }
    }

    @Test
    void getUserNameBearerToken() throws SQLException {
        val url = String.format("jdbc:mill://%s:%s?bearerToken=%s&tlsKeyCertChain=%s&tlsKeyPrivateKey=%s&tlsTrustRootCert=%s",
                getMillAuthTlsHost(),
                getMillPort(),
                getMillJwtToken(),
                getTlsCertChain(),
                getTlsCertPk(),
                getTlsRootCa()
        );
        try (val con = DriverManager.getConnection(url)) {
            val meta = con.getMetaData();
            assertNotEquals("ANONYMOUS", meta.getUserName());
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