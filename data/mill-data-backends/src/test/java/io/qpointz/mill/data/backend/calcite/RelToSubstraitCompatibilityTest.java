package io.qpointz.mill.data.backend.calcite;

import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.data.backend.flow.FlowContextFactory;
import io.qpointz.mill.data.backend.flow.SingleFileSourceRepository;
import io.qpointz.mill.sql.v2.dialect.DialectRegistry;
import io.substrait.extension.SimpleExtension;
import lombok.val;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Skymill Rel→Substrait gate tests (T0–T5) for OData adapter shapes.
 */
class RelToSubstraitCompatibilityTest {

    private static final Path SKYMILL = Path.of("./config/test/flow-skymill.yaml");

    private static CalciteRelBuilderFactory relBuilderFactory;
    private static CalciteRelToSubstraitPlanConverter converter;

    @BeforeAll
    static void setUp() throws Exception {
        val conventions = new CalciteSqlDialectConventions(
                DialectRegistry.fromClasspathDefaults().requireDialect("CALCITE"));
        val props = new Properties();
        props.putAll(conventions.asMap(java.util.Map.of()));
        val repository = new SingleFileSourceRepository(SKYMILL);
        CalciteContextFactory ctxFactory = new FlowContextFactory(repository, props, true);
        relBuilderFactory = new CalciteRelBuilderFactory(ctxFactory);
        converter = new CalciteRelToSubstraitPlanConverter(
                new SubstraitDispatcher(SimpleExtension.loadDefaults()));
    }

    @Test
    void t0_shouldConvertScanOnSkymillCities() {
        val relRoot = relBuilderFactory.withRelBuilder(builder -> {
            builder.scan("skymill", "cities");
            return RelBuilderRoots.toRoot(builder);
        });
        val plan = converter.convert(relRoot);
        assertThat(plan.getRoots()).isNotEmpty();
    }

    @Test
    void t1_shouldConvertSingleJoinExpandShape() {
        val relRoot = relBuilderFactory.withRelBuilder(builder -> {
            builder.scan("skymill", "cities");
            builder.scan("skymill", "segments");
            builder.join(
                    JoinRelType.INNER,
                    builder.equals(builder.field(2, 0, 0), builder.field(2, 1, 0)));
            return RelBuilderRoots.toRoot(builder);
        });
        assertThat(converter.convert(relRoot).getRoots()).isNotEmpty();
    }

    @Test
    void t2_shouldConvertDualJoinToSameTable() {
        val relRoot = relBuilderFactory.withRelBuilder(builder -> {
            builder.scan("skymill", "segments");
            builder.scan("skymill", "cities");
            builder.join(
                    JoinRelType.INNER,
                    builder.equals(builder.field(2, 0, 1), builder.field(2, 1, 0)));
            builder.scan("skymill", "cities");
            builder.join(
                    JoinRelType.INNER,
                    builder.equals(builder.field(2, 0, 2), builder.field(2, 1, 0)));
            return RelBuilderRoots.toRoot(builder);
        });
        assertThat(converter.convert(relRoot).getRoots()).isNotEmpty();
    }

    @Test
    void t3_shouldConvertBookingToPassengerChain() {
        val relRoot = relBuilderFactory.withRelBuilder(builder -> {
            builder.scan("skymill", "bookings");
            builder.scan("skymill", "passenger");
            builder.join(
                    JoinRelType.INNER,
                    builder.equals(builder.field(2, 0, 0), builder.field(2, 1, 0)));
            builder.scan("skymill", "cities");
            builder.join(
                    JoinRelType.INNER,
                    builder.equals(builder.field(2, 0, 1), builder.field(2, 1, 0)));
            return RelBuilderRoots.toRoot(builder);
        });
        assertThat(converter.convert(relRoot).getRoots()).isNotEmpty();
    }

    @Test
    void t4_shouldConvertFlightInstancesToSegmentsChain() {
        val relRoot = relBuilderFactory.withRelBuilder(builder -> {
            builder.scan("skymill", "flight_instances");
            builder.scan("skymill", "segments");
            builder.join(
                    JoinRelType.INNER,
                    builder.equals(builder.field(2, 0, 0), builder.field(2, 1, 0)));
            return RelBuilderRoots.toRoot(builder);
        });
        assertThat(converter.convert(relRoot).getRoots()).isNotEmpty();
    }

    @Test
    void t5_shouldConvertExpandWithFilter() {
        val relRoot = relBuilderFactory.withRelBuilder(builder -> {
            builder.scan("skymill", "cities");
            builder.scan("skymill", "segments");
            builder.join(
                    JoinRelType.INNER,
                    builder.equals(builder.field(2, 0, 0), builder.field(2, 1, 0)));
            builder.filter(
                    builder.call(
                            SqlStdOperatorTable.EQUALS,
                            builder.field(0),
                            builder.literal("1")));
            return RelBuilderRoots.toRoot(builder);
        });
        assertThat(converter.convert(relRoot).getRoots()).isNotEmpty();
    }
}
