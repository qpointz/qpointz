package io.qpointz.mill.security.authentication.basic;

import io.qpointz.mill.annotations.security.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.basic.providers.UserRepo;
import io.qpointz.mill.security.authentication.basic.providers.UserRepoAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;

/**
 * Auto-configuration for HTTP Basic / password-based authentication using a YAML file store.
 *
 * <p>Active when {@code mill.security.enable=true}, {@code mill.security.authentication.basic.enable=true},
 * and {@code mill.security.authentication.basic.store} is a resource path (not {@code jpa}). Use
 * {@code store: jpa} with {@link io.qpointz.mill.persistence.security.jpa.configuration.JpaPasswordAuthenticationConfiguration}
 * for database-backed credentials.
 */
@Configuration
@ConditionalOnSecurity
@EnableConfigurationProperties(BasicAuthenticationProperties.class)
@Slf4j
public class PasswordAuthenticationConfiguration {

    /**
     * Creates a delegating {@link PasswordEncoder} for the file-backed user store.
     *
     * @return the {@link PasswordEncoder} bean
     */
    @Bean
    @ConditionalOnBasicAuthenticationFileStore
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Creates an {@link AuthenticationMethod} backed by a YAML user store.
     *
     * @param properties      basic authentication properties including {@code store}
     * @param resourceLoader  the Spring {@link ResourceLoader} used to open the file
     * @param passwordEncoder the {@link PasswordEncoder} used to verify passwords
     * @return the file-store-backed {@link AuthenticationMethod}
     * @throws IOException if the user-store file cannot be opened or parsed
     */
    @Bean
    @ConditionalOnBasicAuthenticationFileStore
    public AuthenticationMethod fileStoreAuthMethod(
            BasicAuthenticationProperties properties,
            ResourceLoader resourceLoader,
            PasswordEncoder passwordEncoder
    ) throws IOException {
        val pathToFileStore = properties.getStore();
        log.info("Loading basic-auth user store from {}", pathToFileStore);
        val stream = resourceLoader.getResource(pathToFileStore).getInputStream();
        val userRepo = UserRepo.fromYaml(stream);
        log.info("Loaded {} users from basic-auth store", userRepo.getUsers() == null ? 0 : userRepo.getUsers().size());
        val provider = new UserRepoAuthenticationProvider(userRepo, passwordEncoder);
        return new BasicAuthenticationMethod(provider, 299);
    }

}
