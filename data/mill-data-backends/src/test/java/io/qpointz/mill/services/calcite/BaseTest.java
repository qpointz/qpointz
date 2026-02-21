package io.qpointz.mill.services.calcite;

import io.qpointz.mill.autoconfigure.data.backend.calcite.CalciteBackendAutoConfiguration;
import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {
        CalciteBackendAutoConfiguration.class,
        DefaultServiceConfiguration.class
    }
)
@ActiveProfiles("test-calcite")
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EnableAutoConfiguration
public abstract class BaseTest {

    @Autowired
    @Getter
    protected CalciteContextFactory ctxFactory;


}
