package io.qpointz.mill.security.configuration;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@ConditionalOnMissingBean(name = "functionContextFlag") //TODO: hack to figure out if it running as function
public class AppSecurityConfiguration {

    @Bean
    @Order(1)
    @ConditionalOnSecurity
    public SecurityFilterChain secureAppRequests(HttpSecurity http ,
                                  AuthenticationMethods authenticationMethods) throws Exception {
        log.info("Securing `app` routes access.");
        http
                .securityMatcher("/app/**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .anyRequest().authenticated()

                );

        authenticationMethods.getProviders().forEach(m-> {
            try {
                m.applyLoginConfig(http);
                m.applySecurityConfig(http);}
            catch (Exception ex) {
                throw new MillRuntimeException(ex);
            }});
        return http.build();
    }

    @Bean
    @Order(1)
    @ConditionalOnSecurity(false)
    public SecurityFilterChain permitAppRequests(HttpSecurity http) throws Exception {
        log.trace("Permit any `app` route.");
        http
                .securityMatcher("/app/**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
