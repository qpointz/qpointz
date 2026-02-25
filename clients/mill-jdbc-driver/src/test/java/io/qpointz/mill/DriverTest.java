package io.qpointz.mill;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class DriverTest {

    @Test
    void hasversion() throws IOException, SQLException, ClassNotFoundException {
        Class.forName("io.qpointz.mill.Driver");
        val driver = DriverManager.getDriver("jdbc:mill://host:9090");
        assertTrue(driver.getMajorVersion()>=0);
        assertTrue(driver.getMinorVersion()>=0);
        log.info("Driver version {}.{}", driver.getMajorVersion(), driver.getMinorVersion());
    }
}