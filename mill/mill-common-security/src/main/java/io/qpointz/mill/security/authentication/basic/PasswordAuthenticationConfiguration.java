package io.qpointz.mill.security.authentication.basic;

import io.qpointz.mill.security.annotations.ConditionalOnSecurity;
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

@Configuration
@ConditionalOnSecurity
@Slf4j
public class PasswordAuthenticationConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

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
