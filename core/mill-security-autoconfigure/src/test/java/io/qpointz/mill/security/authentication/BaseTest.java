package io.qpointz.mill.security.authentication;

import io.qpointz.mill.security.authentication.basic.PasswordAuthenticationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {
        AuthenticationMethods.class,
        PasswordAuthenticationConfiguration.class})
public abstract class BaseTest {
}
