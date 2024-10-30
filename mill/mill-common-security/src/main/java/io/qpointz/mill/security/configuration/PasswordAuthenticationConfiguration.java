package io.qpointz.mill.security.configuration;

import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.AuthenticationMethods;
import io.qpointz.mill.security.authentication.AuthenticationType;
import io.qpointz.mill.security.authentication.password.PasswordAuthenticationMethod;
import io.qpointz.mill.security.authentication.password.UserRepo;
import io.qpointz.mill.security.authentication.password.UserRepoAuthenticationProvider;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;

@Configuration
@ConditionalOnSecurity
public class PasswordAuthenticationConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @ConditionalOnProperty(prefix = "mill.security.authentication.password", name = "file-store")
    public AuthenticationMethod fileStoreAuthMethod(
            @Value("${mill.security.authentication.password.file-store}") String pathToFileStore,
            ResourceLoader resourceLoader,
            PasswordEncoder passwordEncoder
    ) throws IOException {
        val file = resourceLoader
                .getResource(pathToFileStore)
                .getFile();
        val userRepo = UserRepo.fromYaml(file);

        val provider = new UserRepoAuthenticationProvider(userRepo, passwordEncoder);

        return new PasswordAuthenticationMethod(provider, 299);
    }

}
