package io.qpointz.mill.security.authentication.basic;

import io.qpointz.mill.security.authentication.basic.providers.UserRepo;
import io.qpointz.mill.security.authentication.basic.providers.UserRepoAuthenticationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies a file-backed user store ({@code classpath:config/auth.yml} test fixture) authenticates {@code admin}.
 */
class FileStoreAuthCredentialsTest {

    @Test
    void shouldAuthenticateAdmin_whenUsingClasspathAuthYaml() throws Exception {
        var loader = new DefaultResourceLoader();
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        try (var stream = loader.getResource("classpath:config/auth.yml").getInputStream()) {
            var repo = UserRepo.fromYaml(stream);
            var provider = new UserRepoAuthenticationProvider(repo, encoder);

            var auth = provider.authenticate(
                    new UsernamePasswordAuthenticationToken("admin", "admin")
            );

            assertThat(auth).isNotNull();
            assertThat(auth.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .contains("admin");
        }
    }
}
