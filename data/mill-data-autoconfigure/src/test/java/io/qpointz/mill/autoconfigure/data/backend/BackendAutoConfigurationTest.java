package io.qpointz.mill.autoconfigure.data.backend;

import io.qpointz.mill.autoconfigure.data.BaseDataAutoconfigurationTest;
import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration;
import io.substrait.extension.SimpleExtension;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration.MILL_DATA_DEFAULT_BACKEND;
import static org.assertj.core.api.Assertions.assertThat;

class BackendAutoConfigurationTest extends BaseDataAutoconfigurationTest {

    @Override
    protected ApplicationContextRunner configureContext(ApplicationContextRunner contextRunner) {
        return contextRunner
                .withConfiguration(AutoConfigurations.of(SqlAutoConfiguration.class, BackendAutoConfiguration.class));
    }

    @Test
    void providesBackendType() {
        contextRunner().run(context -> {
            assertThat(context)
                    .hasSingleBean(BackendProperties.class)
                    .getBean(BackendProperties.class)
                    .satisfies(props -> assertThat(props.getType()).isEqualTo(MILL_DATA_DEFAULT_BACKEND));
        });
    }

    @Test
    void providesSubstraitExtensions() {
        contextRunner().run(context -> {
            assertThat(context)
                    .hasSingleBean(SimpleExtension.ExtensionCollection.class);
        });
    }

}
