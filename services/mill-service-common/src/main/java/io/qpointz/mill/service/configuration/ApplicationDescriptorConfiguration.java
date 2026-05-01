package io.qpointz.mill.service.configuration;

import io.qpointz.mill.annotations.security.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import io.qpointz.mill.service.descriptors.AuthMethod;
import io.qpointz.mill.service.descriptors.AuthMethodDescriptor;
import io.qpointz.mill.service.descriptors.SecurityDescriptor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Registers beans for the {@code /.well-known/mill} discovery document: {@link ApplicationDescriptor},
 * {@link SecurityDescriptor}, optional {@link MillHttpListenPort}, and other contributors are assembled by
 * {@link io.qpointz.mill.service.service.WellKnownService}.
 */
@Configuration
@EnableConfigurationProperties(ServiceAddressProperties.class)
public class ApplicationDescriptorConfiguration {

    /**
     * Exposes {@code server.port} (with default {@code 8080} when the property is absent) for other beans.
     *
     * @param serverProperties Boot {@code server.*} binding including {@link ServerProperties#getPort()}
     * @return holder with the configured listen port
     */
    @Bean
    public MillHttpListenPort millHttpListenPort(ServerProperties serverProperties) {
        Integer p = serverProperties.getPort();
        return new MillHttpListenPort(p != null ? p : 8080);
    }

    /**
     * Builds a security descriptor when Mill authentication is enabled.
     *
     * @param authMethods optional aggregate of configured authentication types
     * @return descriptor with {@code enabled=true} and zero or more {@link AuthMethodDescriptor} entries
     */
    @Bean
    @ConditionalOnSecurity
    public SecurityDescriptor securityDescriptor(
            @Autowired(required = false) Optional<AuthenticationMethods> authMethods
    ) {
        val methodDescriptors = authMethods
                .map(this::authMethodDescriptors)
                .orElseGet(List::of);
        return new SecurityDescriptor(true, methodDescriptors);
    }

    /**
     * @param methods live authentication configuration from the security module
     * @return one descriptor per supported {@link AuthMethod}
     */
    private Collection<AuthMethodDescriptor> authMethodDescriptors(AuthenticationMethods methods) {
        return methods.getAuthenticationTypes().stream()
                .map(k -> new AuthMethodDescriptor(AuthMethod.valueOf(k.getValue())))
                .toList();
    }

    /**
     * Placeholder security descriptor when security auto-configuration is off (local dev / tests).
     *
     * @return descriptor with {@code enabled=false} and no auth methods
     */
    @Bean
    @ConditionalOnSecurity(value = false)
    public SecurityDescriptor emptySecurityDescriptor() {
        return new SecurityDescriptor(false, Set.of());
    }

    /**
     * Application identity bean (type {@link io.qpointz.mill.service.descriptors.DescriptorTypes#APP_TYPE_NAME})
     * picked up by {@link io.qpointz.mill.service.service.WellKnownService} for the {@code app} JSON key.
     *
     * @param applicationName {@code spring.application.name} or default {@code Mill}
     * @return descriptor containing only the display name
     */
    @Bean
    public ApplicationDescriptor applicationDescriptor(
            @Value("${spring.application.name:Mill}") String applicationName
    ) {
        return new ApplicationDescriptor(applicationName);
    }

    /*
     * Reserved helpers for deriving a public HTTP base URL from mill.services.public-base-url and
     * ServiceAddressProperties. Kept commented until well-known responses need absolute links again.
     */
//    private static String resolvePublicBaseUrl(Environment environment, ServiceAddressProperties serviceAddressProperties) {
//        val configuredBaseUrl = environment.getProperty("mill.services.public-base-url");
//        if (configuredBaseUrl != null && !configuredBaseUrl.isBlank()) {
//            return trimTrailingSlash(configuredBaseUrl);
//        }
//
//        val fromServiceHost = resolveFromServiceHosts(serviceAddressProperties);
//        if (fromServiceHost != null) {
//            return fromServiceHost;
//        }
//
//        val host = environment.getProperty("server.address", "localhost");
//        val port = environment.getProperty("server.port", "8080");
//        return String.format("http://%s:%s", host, port);
//    }

//    private static String resolveFromServiceHosts(ServiceAddressProperties properties) {
//        val addresses = properties.getExternals();
//        if (addresses == null || addresses.isEmpty()) {
//            return null;
//        }
//
//        val preferredKeys = List.of("external-https", "default-https", "external-http", "default-http");
//        for (String key : preferredKeys) {
//            val address = addresses.get(key);
//            if (isHttpCompatible(address)) {
//                return formatBaseUrl(address);
//            }
//        }
//
//        val orderedValues = new ArrayList<>(addresses.values());
//        for (ServiceAddressDescriptor address : orderedValues) {
//            if (address != null && address.scheme() == ServiceAddressScheme.HTTPS && hasHostPort(address)) {
//                return formatBaseUrl(address);
//            }
//        }
//        for (ServiceAddressDescriptor address : orderedValues) {
//            if (address != null && address.scheme() == ServiceAddressScheme.HTTP && hasHostPort(address)) {
//                return formatBaseUrl(address);
//            }
//        }
//        return null;
//    }

//    private static boolean isHttpCompatible(ServiceAddressDescriptor address) {
//        return address != null
//                && hasHostPort(address)
//                && (address.scheme() == ServiceAddressScheme.HTTP || address.scheme() == ServiceAddressScheme.HTTPS);
//    }

//    private static boolean hasHostPort(ServiceAddressDescriptor address) {
//        return address.host() != null
//                && !address.host().isBlank()
//                && address.port() != null;
//    }

//    private static String formatBaseUrl(ServiceAddressDescriptor address) {
//        return String.format("%s://%s:%d", address.scheme().name().toLowerCase(), address.host(), address.port());
//    }

//    private static String trimTrailingSlash(String value) {
//        if (value.endsWith("/")) {
//            return value.substring(0, value.length() - 1);
//        }
//        return value;
//    }
}
