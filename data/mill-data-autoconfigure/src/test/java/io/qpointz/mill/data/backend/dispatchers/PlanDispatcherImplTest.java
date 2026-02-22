package io.qpointz.mill.data.backend.dispatchers;

import io.qpointz.mill.data.backend.configuration.DefaultServiceConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {PlanDispatcherImplTest.class, DefaultServiceConfiguration.class})
@ActiveProfiles("test-cmart")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@WebAppConfiguration
@ComponentScan("io.qpointz.mill")
@EnableAutoConfiguration
@Slf4j
class PlanDispatcherImplTest {


    @Test
    void trivial(@Autowired PlanDispatcher dispatcher) {
        assertNotNull(dispatcher);
    }




}