package io.qpointz.mill.ai.nlsql.models;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class SpecSqlDialectTest {

    private SqlDialect dialect() {
        return SpecSqlDialect.fromResource("templates/nlsql/dialects/h2/h2.yml");
    }

    @Test
    void trivia() {
        assertNotNull(dialect());
    }

    @Test
    void renderTrivia() {
        val trivia = dialect().getConventionsSpec(SqlDialect.SqlFeatures.DEFAULT).getText();
        log.info(trivia);
    }



}