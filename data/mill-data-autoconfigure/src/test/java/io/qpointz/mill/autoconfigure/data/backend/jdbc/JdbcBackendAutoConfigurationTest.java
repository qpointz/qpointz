package io.qpointz.mill.autoconfigure.data.backend.jdbc;

import io.qpointz.mill.autoconfigure.data.BaseDataAutoconfigurationTest;
import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.data.backend.jdbc.JdbcCalciteConfiguration;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JdbcBackendAutoConfigurationTest extends BaseDataAutoconfigurationTest {

    @Override
    protected ApplicationContextRunner configureContext(ApplicationContextRunner contextRunner) {
        return contextRunner
                .withConfiguration(AutoConfigurations.of(
                        SqlAutoConfiguration.class,
                        BackendAutoConfiguration.class,
                        JdbcBackendAutoConfiguration.class
                ))
                .withBean(SubstraitDispatcher.class, () -> mock(SubstraitDispatcher.class))
                .withPropertyValues(
                        "mill.data.backend.type:jdbc",
                        "mill.data.backend.jdbc.url:jdbc:h2:mem:test",
                        "mill.data.backend.jdbc.driver:org.h2.Driver",
                        "mill.data.backend.jdbc.target-schema:ts"
                );
    }

    @Test
    void shouldBindTargetSchema() {
        contextRunner().run(context -> {
            assertThat(context).hasSingleBean(JdbcCalciteConfiguration.class);
            var cfg = context.getBean(JdbcCalciteConfiguration.class);
            assertThat(cfg.getTargetSchema()).contains("ts");
        });
    }

    @Test
    void shouldIgnoreLegacyOutputSchemaKey() {
        contextRunner()
                .withPropertyValues(
                        "mill.data.backend.jdbc.target-schema:",
                        "mill.data.backend.jdbc.output-schema:legacy")
                .run(context -> {
                    var cfg = context.getBean(JdbcCalciteConfiguration.class);
                    assertThat(cfg.getTargetSchema().orElse("")).isNotEqualTo("legacy");
                });
    }
}
