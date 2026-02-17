package io.qpointz.mill.data.autoconfigure;

import io.qpointz.mill.sql.dialect.SqlDialectSpec;
import io.qpointz.mill.sql.dialect.SqlDialectSpecs;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MillDataAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MillDataAutoConfiguration.class));

    @Test
    void autoConfigurationShouldLoadFromImports() {
        new ApplicationContextRunner().run(context -> { });
    }

    @Test
    void providesSqlDialectAndDefaultToCalcite() {
        contextRunner.run(context -> {
            assertThat(context)
                    .hasSingleBean(SqlDialectSpec.class)
                    .getBean(SqlDialectSpec.class)
                    .satisfies(spec -> assertThat(spec.id()).isEqualTo(SqlDialectSpecs.CALCITE.id()));
        });
    }

    @Test
    void setDialectBySqlDialectProperty() {
        contextRunner
                .withPropertyValues("mill.data.sql-dialect:h2")
                .run(context -> {
                    assertThat(context)
                            .getBean(SqlDialectSpec.class)
                            .satisfies(spec -> assertThat(spec.id()).isEqualTo(SqlDialectSpecs.H2.id()));
                });
    }

}
