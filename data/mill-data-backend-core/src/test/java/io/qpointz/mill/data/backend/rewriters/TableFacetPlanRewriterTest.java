package io.qpointz.mill.data.backend.rewriters;

import com.google.protobuf.util.JsonFormat;
import io.qpointz.mill.data.backend.dispatchers.SubstraitDispatcher;
import io.qpointz.mill.test.data.backend.JdbcBackendContextRunner;
import io.substrait.plan.ProtoPlanConverter;
import io.substrait.proto.Plan;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class TableFacetPlanRewriterTest {

    private static final JdbcBackendContextRunner runner = JdbcBackendContextRunner.jdbcH2Context(
            "jdbc:h2:mem:rewriter;INIT=RUNSCRIPT FROM '../../test/datasets/cmart/sql/cmart.sql'",
            "cmart");

    private static io.substrait.plan.Plan loadTestPlan(String name) throws IOException {
        val planBuilder = Plan.newBuilder();
        try (val reader = new InputStreamReader(new FileInputStream("../../test/plans/" + name + ".json"))) {
            JsonFormat.parser().merge(reader, planBuilder);
            return new ProtoPlanConverter().from(planBuilder.build());
        }
    }

    @Test
    void planRewrites() {
        runner.run(ctx -> {
            try {
                val substrait = new SubstraitDispatcher(io.substrait.extension.SimpleExtension.loadDefaults());
                val exp = ctx.getSqlProvider().parseSqlExpression(List.of("cmart", "CLIENT"), "`CLIENT_ID` <> 0");

                val sourcePlan = loadTestPlan("trivial");
                val facets = TableFacetFactories.fromCollection(TableFacetsCollection.builder()
                        .facets(Map.of(
                                List.of("cmart", "CLIENT"), TableFacet.builder().recordFacetExpression(exp.expression()).build()
                        ))
                        .build());

                val rewriter = new TableFacetPlanRewriter(facets, substrait);
                val rewritten = rewriter.rewritePlan(sourcePlan, null);
                assertNotNull(rewritten);
                assertNotEquals(substrait.planToProto(sourcePlan), substrait.planToProto(rewritten));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void parseLogical() {
        runner.run(ctx -> {
            val exp = ctx.getSqlProvider().parseSqlExpression(
                    List.of("cmart", "CLIENT"),
                    "`CLIENT_ID` <> 0 OR `FIRST_NAME` = 'ANN' OR `LAST_NAME` = 'SMITHS'");
            assertTrue(exp.isSuccess());
            log.info("Parsed:{}", exp.expression());
        });
    }
}
