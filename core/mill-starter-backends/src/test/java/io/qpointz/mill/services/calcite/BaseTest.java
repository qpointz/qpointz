package io.qpointz.mill.services.calcite;

import io.qpointz.mill.services.calcite.configuration.CalciteServiceConfiguration;
import io.qpointz.mill.services.configuration.BackendConfiguration;
import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {
        CalciteServiceConfiguration.class,
        DefaultServiceConfiguration.class,
        BackendConfiguration.class,
    }
)
@ActiveProfiles("test-calcite")
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseTest {

    @Autowired
    @Getter
    protected CalciteContextFactory ctxFactory;


}
