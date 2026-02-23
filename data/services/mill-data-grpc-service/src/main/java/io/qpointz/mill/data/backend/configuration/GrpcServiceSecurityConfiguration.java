package io.qpointz.mill.data.backend.configuration;

import io.qpointz.mill.proto.DataConnectServiceGrpc;
import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import io.qpointz.mill.security.authentication.AuthenticationType;
import io.qpointz.mill.service.annotations.ConditionalOnService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.CompositeGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.check.AccessPredicate;
import net.devh.boot.grpc.server.security.check.AccessPredicateVoter;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

import java.util.*;

@Slf4j
@Setter
@Getter
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
@EnableWebSecurity
@ConditionalOnSecurity
@ConditionalOnService("grpc")
public class GrpcServiceSecurityConfiguration {

    private final AuthenticationMethods authenticationMethods;

    public GrpcServiceSecurityConfiguration(@Autowired AuthenticationMethods authenticationMethods) {
        this.authenticationMethods = authenticationMethods;
    }

    @Bean
    List<GrpcAuthenticationReader> authReaders() {
        return authenticationMethods.getAuthenticationTypes()
                .stream().map(GrpcServiceSecurityConfiguration::createAuthReader)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private static Optional<GrpcAuthenticationReader> createAuthReader(AuthenticationType type) {
        return switch (type) {
            case BASIC -> Optional.of(new BasicGrpcAuthenticationReader());
            case OAUTH2 -> Optional.of(new BearerAuthenticationReader(BearerTokenAuthenticationToken::new));
            case CUSTOM -> Optional.empty();
        };
    }

    @Bean
    GrpcAuthenticationReader authReader(List<GrpcAuthenticationReader> readers) {
        if (log.isInfoEnabled()) {
            readers.stream().forEach(k-> log.info("Registered auth reader:{}",k.getClass().getCanonicalName()));
        }
        return new CompositeGrpcAuthenticationReader(readers);
    }

    @Bean
    GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
        val source = new ManualGrpcSecurityMetadataSource();
        source.set(DataConnectServiceGrpc.getServiceDescriptor(), AccessPredicate.authenticated());
        return source;
    }

    @Bean
    AccessDecisionManager accessDecisionManager() {
        final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
        voters.add(new AccessPredicateVoter());
        return new UnanimousBased(voters);
    }


}
