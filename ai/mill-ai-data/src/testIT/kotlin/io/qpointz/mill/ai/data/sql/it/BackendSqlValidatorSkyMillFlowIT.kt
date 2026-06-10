package io.qpointz.mill.ai.data.sql.it

import io.qpointz.mill.ai.data.sql.BackendSqlValidator
import io.qpointz.mill.data.backend.SqlProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * End-to-end validation using [BackendSqlValidator] and the live [SqlProvider] from the Skymill + flow fixture.
 * Spring properties are loaded from `src/testIT/resources/application.yml` (see `apps/mill-service/application.yml`).
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [SqlValidatorSkymillFlowItApplication::class])
class BackendSqlValidatorSkyMillFlowIT {

    @Autowired
    private lateinit var sqlProvider: SqlProvider

    @Test
    fun `accepts select on skymill cities`() {
        val validator = BackendSqlValidator(sqlProvider)
        val outcome =
            validator.validate(
                // Backtick-quoted names match Calcite/flow parser config; unquoted `skymill` becomes SKYMILL.
                "SELECT `id`, `city` FROM `skymill`.`cities`",
            )
        assertThat(outcome.passed)
            .withFailMessage("parseSql failed: %s", outcome.message)
            .isTrue()
    }

    @Test
    fun `rejects unknown table`() {
        val validator = BackendSqlValidator(sqlProvider)
        val outcome =
            validator.validate(
                "SELECT 1 FROM skymill.nonexistent_table",
            )
        assertThat(outcome.passed).isFalse()
    }
}
