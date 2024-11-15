package io.qpointz.mill.services.dispatchers;

import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {PlanDispatcherImplTest.class, DefaultServiceConfiguration.class})
@ComponentScan("io.qpointz")
@ActiveProfiles("test-cmart")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PlanDispatcherImplTest {


    @Test
    void trivial(@Autowired PlanDispatcher dispatcher) {
        assertNotNull(dispatcher);
    }




}