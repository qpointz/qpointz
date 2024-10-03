package io.qpointz.mill.services.security;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

import java.io.FileNotFoundException;
import java.util.*;

@Slf4j
public final class SecurityProviders {

    public static final String TYPE_KEY = "type";

    private SecurityProviders() {}

    private static final Set<SecurityProviderFactory<?>> FACTORIES = Set.of(
            new NoopSecurityProviderFactory(),
            new DenySecurityProviderFactory(),
            new JwtSecurityProviderFactory(),
            new FileBasedSecurityProviderFactory()
    );

    public static SecurityProviderFactory<?> getFactory(String type) {
        val mayBeFactory = FACTORIES.stream()
                .filter(k-> k.getProviderKey().equals(type))
                .findFirst();
        return mayBeFactory.isPresent()
                ? mayBeFactory.get()
                : null;

    }

    @SneakyThrows
    public static List<AuthenticationProvider> createAuthProviders(List<Map<String,Object>> configs, PasswordEncoder passwordEncoder) {
        return configs.stream()
                .map(config -> {
                    try {
                        return createAuthProvider(config, passwordEncoder);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    public static List<GrpcAuthenticationReader> createAuthReaders(List<Map<String,Object>> configs) {
        val readerTypeSet = new HashSet<AuthReaderType>();
        configs.stream()
                .map(SecurityProviders::getProviderFactory)
                .forEach(k-> readerTypeSet.addAll(k.getRequeiredAuthReaderTypes()));
        return readerTypeSet.stream()
                .map(SecurityProviders::createAuthReader)
                .toList();
    }

    public static GrpcAuthenticationReader createAuthReader(AuthReaderType type) {
        return switch (type) {
            case BasicGrpc -> new BasicGrpcAuthenticationReader();
            case Bearer -> new BearerAuthenticationReader(BearerTokenAuthenticationToken::new);
        };
    }

    private static SecurityProviderFactory<?> getProviderFactory(Map<String, Object> config) {
        if (! config.containsKey(TYPE_KEY)) {
            val sb = new StringBuilder();
            config.forEach((k, v) -> sb.append(k).append(": ").append(v).append(System.lineSeparator()));
            log.error("No authentication provider type configured:\n{}", sb);
            throw new IllegalArgumentException("No authentication provider type configured");
        }

        val type = config.get(TYPE_KEY).toString()
                .toLowerCase();
        val factory = getFactory(type);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown authentication provider type: " + type);
        }
        return factory;
    }

    private static AuthenticationProvider createAuthProvider(Map<String, Object> config, PasswordEncoder passwordEncoder) throws FileNotFoundException {
        val factory = getProviderFactory(config);
        //remove type key for the factories implementing config validation
        val factoryConfig = new HashMap<>(config);
        factoryConfig.putAll(config);
        factoryConfig.remove(TYPE_KEY);
        return factory.createAuthenticationProvider(factoryConfig, passwordEncoder);
    }

}
