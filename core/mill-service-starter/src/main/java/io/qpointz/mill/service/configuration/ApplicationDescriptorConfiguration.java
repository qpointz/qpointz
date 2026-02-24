package io.qpointz.mill.service.configuration;

import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethodDescriptor;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import io.qpointz.mill.service.descriptors.ApplicationDescriptor;
import io.qpointz.mill.service.descriptors.SecurityDescriptor;
import io.qpointz.mill.service.descriptors.ServiceAddressDescriptor;
import io.qpointz.mill.service.descriptors.ServiceAddressScheme;
import io.qpointz.mill.service.descriptors.ServiceDescriptor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Configuration
@EnableConfigurationProperties(ServiceAddressProperties.class)
public class ApplicationDescriptorConfiguration {

    @Bean
    @ConditionalOnSecurity
    public SecurityDescriptor securityDescriptor(
            @Autowired(required = false) Optional<AuthenticationMethods> authMethods
    ) {
        val methodDescriptors = authMethods.isPresent()
                ? authMethods.get().getAuthenticationMethodDescriptors()
                : List.<AuthenticationMethodDescriptor>of();
        return new SecurityDescriptor(true, methodDescriptors);
    }

    @Bean
    @ConditionalOnSecurity(value = false)
    public SecurityDescriptor emptySecurityDescriptor() {
        return new SecurityDescriptor(false, Set.of());
    }

    @Bean
    public Map<String, ApplicationDescriptor.SchemaDescriptor> schemaDescriptors(
            @Autowired(required = false) SchemaProvider provider,
            @Autowired Environment environment,
            @Autowired ServiceAddressProperties serviceAddressProperties
    ) {
        if (provider == null || provider.getSchemaNames() == null) {
            return Collections.emptyMap();
        }

        val baseUrl = resolvePublicBaseUrl(environment, serviceAddressProperties);
        return StreamSupport.stream(provider.getSchemaNames().spliterator(), false)
                .map(name -> new ApplicationDescriptor.SchemaDescriptor(
                        name,
                        URI.create(baseUrl + "/.well-known/mill/schemas/" + name)
                ))
                .collect(Collectors.toMap(ApplicationDescriptor.SchemaDescriptor::name, z -> z));
    }

    @Bean
    public ApplicationDescriptor applicationDescriptor(
            @Autowired(required = false) Optional<Collection<ServiceDescriptor>> serviceDescriptors,
            @Autowired(required = false) SecurityDescriptor securityDescriptor,
            @Autowired(required = false) Map<String, ApplicationDescriptor.SchemaDescriptor> schemas
    ) {
        return new ApplicationDescriptor(
                serviceDescriptors.orElse(List.of()),
                securityDescriptor,
                schemas == null ? Map.of() : schemas
        );
    }

    private static String resolvePublicBaseUrl(Environment environment, ServiceAddressProperties serviceAddressProperties) {
        val configuredBaseUrl = environment.getProperty("mill.services.public-base-url");
        if (configuredBaseUrl != null && !configuredBaseUrl.isBlank()) {
            return trimTrailingSlash(configuredBaseUrl);
        }

        val fromServiceHost = resolveFromServiceHosts(serviceAddressProperties);
        if (fromServiceHost != null) {
            return fromServiceHost;
        }

        val host = environment.getProperty("server.address", "localhost");
        val port = environment.getProperty("server.port", "8080");
        return String.format("http://%s:%s", host, port);
    }

    private static String resolveFromServiceHosts(ServiceAddressProperties properties) {
        val addresses = properties.getExternals();
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }

        val preferredKeys = List.of("external-https", "default-https", "external-http", "default-http");
        for (String key : preferredKeys) {
            val address = addresses.get(key);
            if (isHttpCompatible(address)) {
                return formatBaseUrl(address);
            }
        }

        val orderedValues = new ArrayList<>(addresses.values());
        for (ServiceAddressDescriptor address : orderedValues) {
            if (address != null && address.scheme() == ServiceAddressScheme.HTTPS && hasHostPort(address)) {
                return formatBaseUrl(address);
            }
        }
        for (ServiceAddressDescriptor address : orderedValues) {
            if (address != null && address.scheme() == ServiceAddressScheme.HTTP && hasHostPort(address)) {
                return formatBaseUrl(address);
            }
        }
        return null;
    }

    private static boolean isHttpCompatible(ServiceAddressDescriptor address) {
        return address != null
                && hasHostPort(address)
                && (address.scheme() == ServiceAddressScheme.HTTP || address.scheme() == ServiceAddressScheme.HTTPS);
    }

    private static boolean hasHostPort(ServiceAddressDescriptor address) {
        return address.host() != null
                && !address.host().isBlank()
                && address.port() != null;
    }

    private static String formatBaseUrl(ServiceAddressDescriptor address) {
        return String.format("%s://%s:%d", address.scheme().name().toLowerCase(), address.host(), address.port());
    }

    private static String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
