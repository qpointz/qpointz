package io.qpointz.mill.autoconfigure.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseDataAutoconfigurationTest {

    @Getter(value = AccessLevel.PROTECTED, lazy = true)
    @Accessors(fluent = true)
    private final ApplicationContextRunner contextRunner = configureContext(new ApplicationContextRunner());

    protected abstract ApplicationContextRunner configureContext(ApplicationContextRunner contextRunner);

    @Test
    void autoConfigurationShouldLoadFromImports() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        SqlAutoConfiguration.class
                ))
                .run(context -> assertThat(context).hasNotFailed());
    }

}
