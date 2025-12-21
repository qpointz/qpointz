package io.qpointz.mill.test.services;

import io.qpointz.mill.MillRuntimeException;
import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test-security/")
@Slf4j
@AutoConfiguration
@EnableWebSecurity
public class TestController {

    public TestController() {
        log.info("TestController");
        //test controller
    }

    @Bean
    @Order(1)
    @ConditionalOnSecurity
    public SecurityFilterChain secureTestControllerRoutes(HttpSecurity http ,
                                                 AuthenticationMethods authenticationMethods) throws Exception {
        log.info("Securing `test-security` routes access.");
        http
                .securityMatcher("/test-security/**")
                .authorizeHttpRequests(authHttp -> authHttp
                        .anyRequest().authenticated()

                )
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable);

        authenticationMethods.getProviders().forEach(m-> {
            try {
                m.applyLoginConfig(http);
                m.applySecurityConfig(http);}
            catch (Exception ex) {
                throw new MillRuntimeException(ex);
            }});
        return http.build();
    }



    public record AuthInfo(boolean authenticated, String principalName, List<String> authorities) {
    }

    @GetMapping("auth-info")
    public AuthInfo authInfo() {
        val ctx = SecurityContextHolder.getContext();
        val auth = ctx.getAuthentication();
        if (auth == null) {
            return new AuthInfo(false, "ANONYMOUS", List.of());
        }

        val authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new AuthInfo(true, auth.getName(), authorities);
    }

}
