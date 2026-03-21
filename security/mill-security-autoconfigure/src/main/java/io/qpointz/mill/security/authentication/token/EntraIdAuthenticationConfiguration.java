package io.qpointz.mill.security.authentication.token;

import io.qpointz.mill.annotations.security.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for Microsoft Entra ID (Azure AD) token-based authentication.
 *
 * <p>Only active when {@code mill.security.enable=true}. Registers an
 * {@link AuthenticationMethod} that validates Entra ID bearer tokens when
 * {@code mill.security.authentication.entra-id-token.enable=true}.
 */
@Configuration
@ConditionalOnSecurity
@Slf4j
public class EntraIdAuthenticationConfiguration {

    /**
     * Creates the Entra ID {@link AuthenticationMethod} when
     * {@code mill.security.authentication.entra-id-token.enable=true}.
     *
     * @return the configured Entra ID {@link AuthenticationMethod}
     */
    @Bean
    @ConditionalOnProperty(prefix = "mill.security.authentication.entra-id-token", name = "enable")
    public AuthenticationMethod entraIdAuthenticationMethod() {
        log.debug("Use EntraId authentication method");
        return new EntraIdAuthenticationMethod(100);
    }

}
