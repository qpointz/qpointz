package io.qpointz.mill.security.configuration;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@ConditionalOnMissingBean(name = "functionContextFlag") //TODO: hack to figure out if it running as function
public class ServicesSecurityConfiguration {

    @Bean
    @ConditionalOnSecurity
    public SecurityFilterChain secureServicesRequests(HttpSecurity http ,
                                                 AuthenticationMethods authenticationMethods) throws Exception {
        log.info("Securing `services` routes access.");
        http
                .securityMatcher("/services/**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable);


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
    @ConditionalOnSecurity(false)
    public SecurityFilterChain permitServicesRequests(HttpSecurity http , AuthenticationMethods authenticationMethods) throws Exception {
        log.debug("Permit all services requests.");
        http
                .securityMatcher("/services/**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
