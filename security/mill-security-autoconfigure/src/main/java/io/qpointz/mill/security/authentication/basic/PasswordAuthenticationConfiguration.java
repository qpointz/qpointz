package io.qpointz.mill.security.authentication.basic;

import io.qpointz.mill.annotations.security.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.basic.providers.UserRepo;
import io.qpointz.mill.security.authentication.basic.providers.UserRepoAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;

/**
 * Auto-configuration for HTTP Basic / password-based authentication.
 *
 * <p>This configuration is only active when {@code mill.security.enable=true}. It
 * registers a {@link PasswordEncoder} bean and, when
 * {@code mill.security.authentication.basic.enable=true}, creates a file-backed
 * {@link AuthenticationMethod} that loads user credentials from a YAML file specified
 * by {@code mill.security.authentication.basic.file-store}.
 */
@Configuration
@ConditionalOnSecurity
@Slf4j
public class PasswordAuthenticationConfiguration {

    /**
     * Creates a delegating {@link PasswordEncoder} that supports multiple encoding
     * strategies (bcrypt, scrypt, argon2, etc.).
     *
     * @return the {@link PasswordEncoder} bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Creates an {@link AuthenticationMethod} backed by a YAML user store when
     * {@code mill.security.authentication.basic.enable=true}.
     *
     * @param pathToFileStore  the resource path to the YAML file containing user
     *                         credentials, resolved from
     *                         {@code mill.security.authentication.basic.file-store}
     * @param resourceLoader   the Spring {@link ResourceLoader} used to open the file
     * @param passwordEncoder  the {@link PasswordEncoder} used to verify passwords
     * @return the file-store-backed {@link AuthenticationMethod}
     * @throws IOException if the user-store file cannot be opened or parsed
     */
    @Bean
    @ConditionalOnProperty(prefix = "mill.security.authentication.basic", name = "enable")
    public AuthenticationMethod fileStoreAuthMethod(
            @Value("${mill.security.authentication.basic.file-store}") String pathToFileStore,
            ResourceLoader resourceLoader,
            PasswordEncoder passwordEncoder
    ) throws IOException {
        val stream = resourceLoader.getResource(pathToFileStore).getInputStream();
        val userRepo = UserRepo.fromYaml(stream);
        val provider = new UserRepoAuthenticationProvider(userRepo, passwordEncoder);
        return new BasicAuthenticationMethod(provider, 299);
    }

}
