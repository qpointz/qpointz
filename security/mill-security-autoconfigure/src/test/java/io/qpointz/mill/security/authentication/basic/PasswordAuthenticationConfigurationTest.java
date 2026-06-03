package io.qpointz.mill.security.authentication.basic;

import io.qpointz.mill.security.authentication.AuthenticationMethod;
import io.qpointz.mill.security.authentication.basic.providers.UserRepoAuthenticationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordAuthenticationConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("mill.security.enable=true")
            .withConfiguration(AutoConfigurations.of(PasswordAuthenticationConfiguration.class));

    @Test
    void shouldRegisterFileStoreBeans_whenStoreIsResourcePath() {
        contextRunner
                .withPropertyValues(
                        "mill.security.authentication.basic.enable=true",
                        "mill.security.authentication.basic.store=classpath:userstore/passwd.yml"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(PasswordEncoder.class);
                    assertThat(context).hasSingleBean(AuthenticationMethod.class);

                    var method = context.getBean(AuthenticationMethod.class);
                    var provider = (UserRepoAuthenticationProvider) method.getAuthenticationProvider();
                    var auth = provider.authenticate(
                            new UsernamePasswordAuthenticationToken("test-user", "secret")
                    );
                    assertThat(auth).isNotNull();
                });
    }

    @Test
    void shouldNotRegisterFileStoreBeans_whenStoreIsJpa() {
        contextRunner
                .withPropertyValues(
                        "mill.security.authentication.basic.enable=true",
                        "mill.security.authentication.basic.store=jpa"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(PasswordEncoder.class);
                    assertThat(context).doesNotHaveBean(AuthenticationMethod.class);
                });
    }
}
