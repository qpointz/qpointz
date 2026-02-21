package io.qpointz.mill.services.descriptors;

import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethodDescriptor;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import io.qpointz.mill.services.SchemaProvider;
import lombok.Data;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Data
public class ApplicationDescriptor {


    @Bean
    @ConditionalOnSecurity
    public static SecurityDescriptor securityDescriptor(@Autowired(required = false) Optional<AuthenticationMethods> authMethods)
    {
        val methodDescriptors = authMethods.isPresent()
                ? authMethods.get().getAuthenticationMethodDescriptors()
                : List.<AuthenticationMethodDescriptor>of();
        return new SecurityDescriptor(true, methodDescriptors);
    }

    @Bean
    @ConditionalOnSecurity(value = false)
    public static SecurityDescriptor emptySecurityDescriptor(
            @Autowired(required = false) Optional<AuthenticationMethods> authMethods
    ) {
        return new SecurityDescriptor(false, Set.of());
    }

    public record SchemaDescriptor(String name, URI link) {}

    @Bean
    public static Map<String, SchemaDescriptor> schemaDescriptors(@Autowired(required = false) SchemaProvider provider) {
        if (provider == null || provider.getSchemaNames() == null) {
            return Collections.emptyMap();
        }
        return StreamSupport.stream(provider.getSchemaNames().spliterator(), false)
                .map(name -> new SchemaDescriptor(name, URI.create(String.format("http://localhost:8080/.well-known/mill/schemas/%s", name))))
                .collect(Collectors.toMap(k->k.name, z->z));
    }


    @Getter
    private final Collection<ServiceDescriptor> services;

    @Getter
    private final SecurityDescriptor security;

    @Getter
    private final Map<String, SchemaDescriptor> schemas;

    public ApplicationDescriptor(
            @Autowired(required = false) Optional<Collection<ServiceDescriptor>> serviceDescriptors,
            @Autowired(required = false) SecurityDescriptor securityDescriptor,
            @Autowired(required = false) Map<String, SchemaDescriptor> schemas
    ) {
        this.services = serviceDescriptors.orElse(List.of());
        this.security = securityDescriptor;
        this.schemas = schemas;
    }


}