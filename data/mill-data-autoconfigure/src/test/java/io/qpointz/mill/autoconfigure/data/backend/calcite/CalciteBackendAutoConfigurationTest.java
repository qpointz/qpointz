package io.qpointz.mill.autoconfigure.data.backend.calcite;

import io.qpointz.mill.autoconfigure.data.BaseDataAutoconfigurationTest;
import io.qpointz.mill.autoconfigure.data.SqlAutoConfiguration;
import io.qpointz.mill.autoconfigure.data.backend.BackendAutoConfiguration;
import io.qpointz.mill.data.backend.ExecutionProvider;
import io.qpointz.mill.data.backend.SchemaProvider;
import io.qpointz.mill.data.backend.SqlProvider;
import io.qpointz.mill.data.backend.calcite.CalciteContextFactory;
import io.qpointz.mill.data.backend.calcite.CalciteSqlDialectConventions;
import io.qpointz.mill.data.backend.calcite.ConnectionContextFactory;
import io.qpointz.mill.data.backend.calcite.providers.PlanConverter;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.substrait.extension.ExtensionCollector;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link CalciteBackendAutoConfiguration}.
 *
 * <p>Composes the full auto-configuration chain ({@link SqlAutoConfiguration},
 * {@link BackendAutoConfiguration}, {@link CalciteBackendAutoConfiguration}) and supplies
 * a mock {@link SubstraitDispatcher} since it is provided outside this chain.</p>
 */
class CalciteBackendAutoConfigurationTest extends BaseDataAutoconfigurationTest {

    @Override
    protected ApplicationContextRunner configureContext(ApplicationContextRunner contextRunner) {
        return contextRunner
                .withConfiguration(AutoConfigurations.of(
                        SqlAutoConfiguration.class,
                        BackendAutoConfiguration.class,
                        CalciteBackendAutoConfiguration.class
                ))
                .withBean(SubstraitDispatcher.class, () -> mock(SubstraitDispatcher.class))
                .withPropertyValues("mill.data.backend.calcite.model:inline:{version:'1.0',defaultSchema:'test',schemas:[]}");
    }

    // -- conditional activation --

    @Test
    void shouldActivateWhenBackendTypeIsCalcite() {
        contextRunner()
                .withPropertyValues("mill.data.backend.type:calcite")
                .run(context -> assertThat(context).hasSingleBean(CalciteBackendAutoConfiguration.class));
    }

    @Test
    void shouldActivateWhenBackendTypeIsMissing() {
        contextRunner()
                .run(context -> assertThat(context).hasSingleBean(CalciteBackendAutoConfiguration.class));
    }

    @Test
    void shouldNotActivateWhenBackendTypeIsNotCalcite() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        SqlAutoConfiguration.class,
                        BackendAutoConfiguration.class,
                        CalciteBackendAutoConfiguration.class
                ))
                .withPropertyValues("mill.data.backend.type:jdbc")
                .run(context -> assertThat(context).doesNotHaveBean(CalciteBackendAutoConfiguration.class));
    }

    // -- CalciteProperties --

    @Test
    void shouldBindCalciteProperties() {
        contextRunner().run(context -> {
            assertThat(context).hasSingleBean(CalciteBackendProperties.class);
        });
    }

    // -- CalciteSqlDialectConventions bean --

    @Test
    void shouldProvideSqlDialectConventions() {
        contextRunner().run(context -> {
            assertThat(context).hasSingleBean(CalciteSqlDialectConventions.class);
        });
    }

    // -- CalciteContextFactory bean --

    @Test
    void shouldProvideCalciteContextFactory() {
        contextRunner().run(context -> {
            assertThat(context).hasSingleBean(CalciteContextFactory.class);
        });
    }

    // -- ExtensionCollector bean --

    @Test
    void shouldProvideExtensionCollector() {
        contextRunner().run(context -> {
            assertThat(context).hasSingleBean(ExtensionCollector.class);
        });
    }

    // -- PlanConverter bean --

    @Test
    void shouldProvidePlanConverter() {
        contextRunner().run(context -> {
            assertThat(context).hasSingleBean(PlanConverter.class);
        });
    }

    // -- Service provider beans --

    @Test
    void shouldProvideSchemaProvider() {
        contextRunner().run(context -> {
            assertThat(context).hasSingleBean(SchemaProvider.class);
        });
    }

    @Test
    void shouldProvideExecutionProvider() {
        contextRunner().run(context -> {
            assertThat(context).hasSingleBean(ExecutionProvider.class);
        });
    }

    @Test
    void shouldProvideSqlProvider() {
        contextRunner().run(context -> {
            assertThat(context).hasSingleBean(SqlProvider.class);
        });
    }

    @Test
    void shouldApplySqlDialectProperties() {
        contextRunner().withPropertyValues(
                        "mill.data.backend.type:calcite",
                               "mill.data.sql.conventions.unquotedCasing:TO_LOWER")
            .run(context -> {
                assertThat(context).hasSingleBean(CalciteContextFactory.class);
                if (context.getBean(CalciteContextFactory.class) instanceof ConnectionContextFactory contextFactory) {
                    val props = contextFactory.getConnectionProperties();
                    assertThat(props.getProperty("unquotedCasing")).isEqualTo("TO_LOWER");
                } else {
                    throw new IllegalStateException("Expected ConnectionContextFactory");
                }
        });
    }

}
