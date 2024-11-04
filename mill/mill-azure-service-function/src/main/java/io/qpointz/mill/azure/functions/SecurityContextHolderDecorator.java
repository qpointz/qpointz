package io.qpointz.mill.azure.functions;

import com.microsoft.azure.functions.HttpRequestMessage;
import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import io.qpointz.mill.security.authentication.AuthenticationReader;
import io.qpointz.mill.security.authentication.AuthenticationType;
import io.qpointz.mill.security.authentication.CompositeAuthenticationReader;
import io.qpointz.mill.security.authentication.basic.BasicAuthenticationReader;
import io.qpointz.mill.security.authentication.token.BearerTokenAuthenticationReader;
import io.qpointz.mill.services.SecurityContextSecurityProvider;
import io.qpointz.mill.services.SecurityProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Component
@ConditionalOnSecurity
public class SecurityContextHolderDecorator {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationMethods authenticationMethods;


    @Bean
    SecurityProvider functionSecurityProvider() {
        return new SecurityContextSecurityProvider();
    }

    public SecurityContextHolderDecorator(@Autowired AuthenticationManager authenticationManager,
                                          @Autowired AuthenticationMethods authenticationMethods

    ) {
        this.authenticationManager = authenticationManager;
        this.authenticationMethods = authenticationMethods;
    }


    public <T> void decorate(HttpRequestMessage<Optional<T>> httpRequestMessage) {
        if (authenticationMethods==null || authenticationManager==null) {
            setAnonymous();
        }
        log.info("Begin authentication");
        val authenticationReader = getAuthenticationReader(httpRequestMessage);
        Authentication authentication = null;
        try {
            authentication = authenticationReader.readAuthentication();
        } catch (AuthenticationException e) {
            log.error(e.getMessage(), e);
            setAnonymous();
        }

        if (authentication==null) {
            log.info("Authentication failed");
            setAnonymous();
        }

        try {
            val finalAuth = authenticationManager.authenticate(authentication);
            log.info("Authentication successful {}", finalAuth);
            if (finalAuth != null) {
                SecurityContextHolder.getContext().setAuthentication(finalAuth);
            } else {
                log.info("Authentication failed. Request not authenticated");
                setAnonymous();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            setAnonymous();
        }
    }

    private void setAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private <T> AuthenticationReader getAuthenticationReader(HttpRequestMessage<Optional<T>> httpRequestMessage) {
        val readers = new ArrayList<AuthenticationReader>();
        log.info("Providers {}", authenticationMethods.getProviders().size());
        log.info("Providers {}", authenticationMethods.getProviders());
        log.info("AuthTypes {}", authenticationMethods.getAuthenticationTypes().size());
        log.info("AuthTypes {}", authenticationMethods.getAuthenticationTypes());
        for (val authType : authenticationMethods.getAuthenticationTypes()) {
            log.info("Detected authentication type {}", authType.name());
            if (authType == AuthenticationType.BASIC) {
                log.info("Configure 'Basic' authentication reader");
                val reader = BasicAuthenticationReader.builder()
                        .tokenSupplier(() -> httpRequestMessage.getHeaders()
                                .getOrDefault("authorization", null))
                        .build();
                readers.add(reader);
                continue;
            }

            if (authType == AuthenticationType.OAUTH2) {
                log.info("Configure 'Bearer' token authentication reader");
                val reader = BearerTokenAuthenticationReader.builder()
                        .tokenSupplier(() -> httpRequestMessage.getHeaders()
                                .getOrDefault("authorization", null))
                        .build();
                readers.add(reader);
                continue;
            }

            log.warn("Authentication '{}' not supported", authType.name());

        }
        return new CompositeAuthenticationReader(readers);
    }
}
