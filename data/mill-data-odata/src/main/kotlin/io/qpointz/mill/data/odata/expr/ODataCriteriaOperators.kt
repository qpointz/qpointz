package io.qpointz.mill.data.odata.expr

import org.apache.calcite.sql.SqlOperator
import org.apache.calcite.sql.`fun`.SqlStdOperatorTable

/**
 * Maps RWS Scala singleton criteria operators (identified by [toString]) to Calcite SQL operators.
 */
internal object ODataCriteriaOperators {

    /**
     * @param operator RWS comparison operator singleton
     * @return matching Calcite comparison operator
     */
    fun comparisonSqlOperator(operator: Any): SqlOperator =
        when (operator.toString()) {
            "=" -> SqlStdOperatorTable.EQUALS
            "<>" -> SqlStdOperatorTable.NOT_EQUALS
            ">" -> SqlStdOperatorTable.GREATER_THAN
            ">=" -> SqlStdOperatorTable.GREATER_THAN_OR_EQUAL
            "<" -> SqlStdOperatorTable.LESS_THAN
            "<=" -> SqlStdOperatorTable.LESS_THAN_OR_EQUAL
            else -> throw ODataExpressionException("Unsupported comparison operator: $operator")
        }

    /**
     * @param operator RWS boolean composite operator singleton
     * @return true when the operator is logical AND
     */
    fun isAnd(operator: Any): Boolean = operator.toString() == "AND"

    /**
     * @param operator RWS boolean composite operator singleton
     * @return true when the operator is logical OR
     */
    fun isOr(operator: Any): Boolean = operator.toString() == "OR"
}
