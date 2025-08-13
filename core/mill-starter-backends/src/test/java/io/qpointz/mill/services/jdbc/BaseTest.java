package io.qpointz.mill.services.jdbc;

import io.qpointz.mill.services.ExecutionProvider;
import io.qpointz.mill.services.SchemaProvider;
import io.qpointz.mill.services.MillGrpcService;
import io.qpointz.mill.services.configuration.BackendConfiguration;
import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.autoconfigure.GrpcAdviceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = {
        MillGrpcService.class,
        GrpcAdviceAutoConfiguration.class,
        DefaultServiceConfiguration.class,
        BackendConfiguration.class
}
)
@ActiveProfiles("test-jdbc")
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseTest {

    @Autowired
    @Getter(AccessLevel.PROTECTED)
    private ExecutionProvider executionProvider;


    @Autowired
    @Getter(AccessLevel.PROTECTED)
    private SchemaProvider schemaProvider;


    @Test
    void basicCheck() {
        assertNotNull(executionProvider);
        assertNotNull(schemaProvider);
    }

}