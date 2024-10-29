package io.qpointz.mill.services.meta;

import io.qpointz.mill.security.authentication.AuthenticationMethods;
import io.qpointz.mill.security.authentication.AuthenticationType;
import io.qpointz.mill.services.MetadataProvider;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    public static SecurityDescriptor securityDescriptor(
            @Value("${mill.security.enable:false}") Boolean enabled,
            @Autowired(required = false) Optional<AuthenticationMethods> authMethods
    ) {
        Collection<AuthenticationType> authTypes = authMethods.isPresent()
                ? authMethods.get().getAuthenticationTypes()
                : List.of();
        return new SecurityDescriptor(enabled, Set.copyOf(authTypes));
    }

    public record SchemaDescriptor(String name, URI link) {}

    @Bean
    public static Map<String, SchemaDescriptor> schemaDescriptors(@Autowired(required = false) MetadataProvider provider) {
        if (provider == null) {
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