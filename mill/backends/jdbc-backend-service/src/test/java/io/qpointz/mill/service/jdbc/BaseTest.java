package io.qpointz.mill.service.jdbc;

import io.qpointz.mill.service.ExecutionProvider;
import io.qpointz.mill.service.MetadataProvider;
import io.qpointz.mill.service.MillService;
import io.qpointz.mill.service.calcite.configuration.CalciteServiceProperties;
import io.qpointz.mill.service.jdbc.configuration.JdbcCalciteConfiguration;
import io.qpointz.mill.service.jdbc.configuration.JdbcServiceProvidersConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        JdbcServiceProvidersConfiguration.class
} )
@ContextConfiguration(classes = {
        CalciteServiceProperties.class,
        JdbcCalciteConfiguration.class,
        MillService.class,
        GrpcAdviceAutoConfiguration.class
}
)
@ActiveProfiles("test-jdbc")
@Slf4j
public class BaseTest {

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    private ExecutionProvider executionProvider;


    @Autowired
    @Getter(AccessLevel.PROTECTED)
    private MetadataProvider metadataProvider;


    @Test
    void basicCheck() {
        assertNotNull(executionProvider);
        assertNotNull(metadataProvider);
    }

}