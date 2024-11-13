package io.qpointz.mill.services.rewriters;

import io.qpointz.mill.services.configuration.DefaultServiceConfiguration;
import io.qpointz.mill.services.dispatchers.SubstraitDispatcher;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {TableFacetPlanRewriterTest.class, DefaultServiceConfiguration.class})
@ActiveProfiles("test-cmart")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
class TableFacetPlanRewriterTest extends RewriterBaseTest {

    @Test
    void planRewrites(@Autowired SubstraitDispatcher substrait) throws IOException {
        val exp = this.getSqlProvider().parseSqlExpression(List.of("cmart", "CLIENT"), "`CLIENT_ID` <> 0");

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
    }

    @Test
    void parseLogical() throws IOException {
        val exp = this.getSqlProvider().parseSqlExpression(List.of("cmart", "CLIENT"), "`CLIENT_ID` <> 0 OR `FIRST_NAME` = 'ANN' OR `LAST_NAME` = 'SMITHS'" );
        assertTrue(exp.isSuccess());
        log.info("Parsed:{}", exp.expression());
    }


}