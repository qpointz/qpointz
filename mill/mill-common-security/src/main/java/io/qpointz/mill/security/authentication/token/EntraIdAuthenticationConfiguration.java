package io.qpointz.mill.security.authentication.token;

import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnSecurity
@Slf4j
public class EntraIdAuthenticationConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "mill.security.authentication.entra-id-token", name = "enable")
    public AuthenticationMethod entraIdAuthenticationMethod() {
        log.debug("Use EntraId authentication method");
        return new EntraIdAuthenticationMethod(100);
    }

}
