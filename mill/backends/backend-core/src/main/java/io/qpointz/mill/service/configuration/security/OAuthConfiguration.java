package io.qpointz.mill.service.configuration.security;

import io.qpointz.mill.proto.DeltaServiceGrpc;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.CompositeGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.check.AccessPredicate;
import net.devh.boot.grpc.server.security.check.AccessPredicateVoter;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@EnableWebSecurity
public class OAuthConfiguration {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String jwtSetUri;

    @Bean
    public JwtDecoder jwtDecoder() {

        JwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(jwtSetUri).build();

        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                try {
                    log.debug("token: " + token);
                    Jwt jwt = jwtDecoder.decode(token);
                    log.debug("jwt: " + jwt);
                    return jwt;
                } catch (JwtException e) {
                    log.error("Failed to extract token", e);
                    return null;
                }
            }
        };
    }

    @Bean
    AuthenticationManager authenticationManager(JwtDecoder decoder) {
        final List<AuthenticationProvider> providers = new ArrayList<>();
        providers.add(new JwtAuthenticationProvider(decoder));
        providers.add(new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                val username = authentication.getName();
                val password = authentication.getCredentials().toString();
                return new UsernamePasswordAuthenticationToken(username, password, List.of());
            }
            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        });

        return new ProviderManager(providers);
    }

    @Bean
    GrpcAuthenticationReader authenticationReader() {
        final List<GrpcAuthenticationReader> readers = new ArrayList<>();
        readers.add(new BearerAuthenticationReader(BearerTokenAuthenticationToken::new));
        readers.add(new BasicGrpcAuthenticationReader());
        return new CompositeGrpcAuthenticationReader(readers);
    }

    @Bean
    GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
        val source = new ManualGrpcSecurityMetadataSource();
        source.set(DeltaServiceGrpc.getServiceDescriptor(), AccessPredicate.authenticated());
        return source;
    }

    @Bean
    AccessDecisionManager accessDecisionManager() {
        final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
        voters.add(new AccessPredicateVoter());
        return new UnanimousBased(voters);
    }

    @Bean
    SecurityFilterChain customJwtSecurityChain(HttpSecurity http) throws Exception {
        return http.httpBasic(new Customizer<HttpBasicConfigurer<HttpSecurity>>() {
            @Override
            public void customize(HttpBasicConfigurer<HttpSecurity> httpSecurityHttpBasicConfigurer) {
                httpSecurityHttpBasicConfigurer.authenticationDetailsSource(new AuthenticationDetailsSource<HttpServletRequest, Object>() {
                    @Override
                    public Object buildDetails(HttpServletRequest context) {
                        log.debug("here we go");
                        return context;
                    }
                });
            }
        }).build();
    }

}
