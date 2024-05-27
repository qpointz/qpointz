package io.qpointz.delta.calcite;

import io.qpointz.delta.service.ServiceSecurityConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.calcite.jdbc.CalciteConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@SpringBootTest(classes = {
        CalciteDeltaService.class,
        CalciteDataServiceConfiguration.class,
        CalciteDeltaServiceCtx.class,
        ServiceSecurityConfig.class
} )
@ActiveProfiles("test")
@Slf4j
public class BaseTest {

    @Autowired
    @Getter
    private CalciteConnection connection;


}
