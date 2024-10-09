package io.qpointz.mill;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public abstract class BaseTest {

    protected String getMillHost() {
        return envOr("MILL_HOST", "backend.local");
    }

    protected Integer getMillPort() {
        return envOr("MILL_PORT", 9099, Integer::parseInt);
    }

    protected  String getMillUrl() {
        val url = String.format("jdbc:mill://%s:%s", getMillHost(), getMillPort());
        log.info("Mill url:{}",url);
        System.out.println(url);
        return url;
    }

    protected Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("io.qpointz.mill.Driver");
        val url = getMillUrl();
        return DriverManager.getConnection(url);
    }

    protected String getMillAuthTlsHost() {
        return envOr("MILL_AUTH_TLS_HOST", "backend.local");
    }

    protected Integer getMillAuthTlsPort() {
        return envOr("MILL_PORT", 9098, Integer::parseInt);
    }

    protected String getMillAuthHost() {
        return envOr("MILL_AUTH_HOST", "backend.local");
    }

    protected Integer getMillAuthPort() {
        return envOr("MILL_PORT", 9097, Integer::parseInt);
    }

    protected String getMillTlsHost() {
        return envOr("MILL_TLS_HOST", "backend.local");
    }

    protected Integer getMillTlsPort() {
        return envOr("MILL_PORT", 9096, Integer::parseInt);
    }

    protected String getMillUser() {
        return envOr("MILL_USER", "reader");
    }

    protected String getMillPassword() {
        return envOr("MILL_USER", "reader");
    }

    protected String getMillJwtToken() {
        return envOr("MILL_JWT_TOKEN", "");
    }

    protected String getTlsCertChain() {
        return envOr("TLS_CERT_CHAIN", "../etc/mill-test/config/ssl/client/client.pem");
    }

    protected String getTlsCertPk() {
        return envOr("TLS_CERT_PK", "../etc/mill-test/config/ssl/client/client.key");
    }

    protected String getTlsRootCa() {
        return envOr("TLS_ROOT_CA", "../etc/mill-test/config/ssl/ca.pem");
    }

    private static String envOr(String key, String or) {
        return envOr(key, or, k->k);
    }

    private static <T> T envOr(String key, T or, Function<String, T> map) {
        val mayBeEnv = Optional.ofNullable(System.getenv(key));
        return mayBeEnv.isPresent()
                ? map.apply(mayBeEnv.get())
                : or;
    }

}
