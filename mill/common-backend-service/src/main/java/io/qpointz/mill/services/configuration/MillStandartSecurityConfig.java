package io.qpointz.mill.services.configuration;

import io.qpointz.mill.proto.MillServiceGrpc;
import io.qpointz.mill.services.security.SecurityProviders;
import lombok.*;
import net.devh.boot.grpc.server.security.authentication.CompositeGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.check.AccessPredicate;
import net.devh.boot.grpc.server.security.check.AccessPredicateVoter;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@Setter
@Getter
@Configuration
@EnableConfigurationProperties
@EnableWebSecurity
@ConfigurationProperties(prefix="qp.mill.backend.security")
@AllArgsConstructor
@NoArgsConstructor
@ConditionalOnExpression("${qp.mill.backend.security.enabled}")
public class MillStandartSecurityConfig {

    private List<Map<String,Object>> providers;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public List<AuthenticationProvider> authenticationProviders(PasswordEncoder passwordEncoder) {
        return SecurityProviders.createAuthProviders(providers, passwordEncoder);
    }

    @Bean
    AuthenticationManager authenticationManager(List<AuthenticationProvider> providers) {
        return new ProviderManager(providers);
    }

    @Bean
    List<GrpcAuthenticationReader> authReaders() {
        return SecurityProviders.createAuthReaders(this.providers);
    }

    @Bean
    GrpcAuthenticationReader authReader(List<GrpcAuthenticationReader> readers) {
        return new CompositeGrpcAuthenticationReader(readers);
    }

    @Bean
    GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
        val source = new ManualGrpcSecurityMetadataSource();
        source.set(MillServiceGrpc.getServiceDescriptor(), AccessPredicate.authenticated());
        return source;
    }

    @Bean
    AccessDecisionManager accessDecisionManager() {
        final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
        voters.add(new AccessPredicateVoter());
        return new UnanimousBased(voters);
    }


}
