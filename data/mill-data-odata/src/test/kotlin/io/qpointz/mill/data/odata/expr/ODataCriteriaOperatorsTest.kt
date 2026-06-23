package io.qpointz.mill.data.odata.expr

import org.apache.calcite.sql.`fun`.SqlStdOperatorTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ODataCriteriaOperatorsTest {

    @Test
    fun shouldMapRwsEqOperatorSingletonToCalciteEquals() {
        val operator = Class.forName("com.sdl.odata.api.processor.query.EqOperator\$")
            .getField("MODULE$")
            .get(null)
        assertThat(ODataCriteriaOperators.comparisonSqlOperator(operator)).isEqualTo(SqlStdOperatorTable.EQUALS)
    }

    @Test
    fun shouldMapRwsOrOperatorSingleton() {
        val operator = Class.forName("com.sdl.odata.api.processor.query.OrOperator\$")
            .getField("MODULE$")
            .get(null)
        assertThat(ODataCriteriaOperators.isOr(operator)).isTrue()
        assertThat(ODataCriteriaOperators.isAnd(operator)).isFalse()
    }

    @Test
    fun shouldRejectUnknownComparisonOperator() {
        assertThrows<ODataExpressionException> {
            ODataCriteriaOperators.comparisonSqlOperator(UnknownOperator("!="))
        }
    }

    private class UnknownOperator(private val label: String) {
        override fun toString(): String = label
    }
}
