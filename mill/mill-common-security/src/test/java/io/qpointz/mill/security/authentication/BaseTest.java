package io.qpointz.mill.security.authentication;

import io.qpointz.mill.security.configuration.PasswordAuthenticationConfiguration;
import io.qpointz.mill.security.configuration.TokenAuthenticationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {
        AuthenticationMethods.class,
        PasswordAuthenticationConfiguration.class,
        TokenAuthenticationConfiguration.class})
public abstract class BaseTest {
}
