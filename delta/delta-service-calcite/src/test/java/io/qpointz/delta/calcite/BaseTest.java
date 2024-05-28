package io.qpointz.delta.calcite;


import io.qpointz.delta.calcite.configuration.CalciteDataServiceConfiguration;
import io.qpointz.delta.calcite.configuration.CalciteProvidersConfiguration;
import io.qpointz.delta.service.configuration.DeltaServiceConfiguration;
import io.qpointz.delta.service.configuration.ProvidersConfig;
import io.qpointz.delta.service.configuration.ServiceSecurityConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.jdbc.CalciteConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {
        CalciteDataServiceConfiguration.class,
        CalciteProvidersConfiguration.class
} )
@ActiveProfiles("test")
@Slf4j
public class BaseTest {

    @Autowired
    @Getter
    private CalciteConnection connection;


}
