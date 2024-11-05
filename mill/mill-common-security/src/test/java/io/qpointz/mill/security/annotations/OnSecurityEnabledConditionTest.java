package io.qpointz.mill.security.annotations;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = AnnotationsTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class OnSecurityEnabledConditionTest {

    @Nested
    @SpringBootTest(classes = AnnotationsTestConfiguration.class)
    @EnableAutoConfiguration
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
    @TestPropertySource(properties = {"mill.security.enable=true"})
    class ExplicitEnableTest {

        @Test
        void hallo(@Autowired @Qualifier("SecurityEnabled") Boolean securityEnabled) {
            assertTrue(securityEnabled);
        }

    }

    @Nested
    @SpringBootTest(classes = AnnotationsTestConfiguration.class)
    @EnableAutoConfiguration
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
    @TestPropertySource(properties = {"mill.security.enable=false"})
    class ExplicitDisableTest {

        @Test
        void hallo(@Autowired @Qualifier("SecurityEnabled") Boolean securityEnabled) {
            assertFalse(securityEnabled);
        }

    }


    @Nested
    @SpringBootTest(classes = AnnotationsTestConfiguration.class)
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
    class MatchMissing {

        @Test
        void hallo(@Autowired @Qualifier("SecurityEnabled") Boolean securityEnabled) {
            assertFalse(securityEnabled);
        }

    }




}