package io.qpointz.mill.autoconfigure.data;

import io.qpointz.mill.sql.dialect.SqlDialectSpec;
import io.qpointz.mill.sql.dialect.SqlDialectSpecs;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration.MILL_DATA_DEFAULT_DIALECT;
import static org.assertj.core.api.Assertions.assertThat;

class DataAutoConfigurationTest extends BaseDataAutoconfigurationTest {

    @Override
    protected ApplicationContextRunner configureContext(ApplicationContextRunner contextRunner) {
        return contextRunner
                .withConfiguration(AutoConfigurations.of(SqlAutoConfiguration.class));
    }

    @Test
    void providesSqlDialectAndDefaultToCalcite() {
        contextRunner().run(context -> {
            assertThat(context)
                    .hasSingleBean(SqlDialectSpec.class)
                    .getBean(SqlDialectSpec.class)
                    .satisfies(spec -> assertThat(spec.id()).isEqualTo(MILL_DATA_DEFAULT_DIALECT.id()));
        });
    }

    @Test
    void setDialectBySqlDialectProperty() {
        contextRunner()
                .withPropertyValues("mill.data.sql.dialect:h2")
                .run(context -> {
                    assertThat(context)
                            .getBean(SqlDialectSpec.class)
                            .satisfies(spec -> assertThat(spec.id()).isEqualTo(SqlDialectSpecs.H2.id()));
                });
    }

}
