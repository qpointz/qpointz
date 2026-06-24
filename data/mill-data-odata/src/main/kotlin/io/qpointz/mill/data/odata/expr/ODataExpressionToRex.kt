package io.qpointz.mill.data.odata.expr

import com.sdl.odata.api.processor.query.ComparisonCriteria
import com.sdl.odata.api.processor.query.CompositeCriteria
import com.sdl.odata.api.processor.query.ContainsMethodCriteria
import com.sdl.odata.api.processor.query.Criteria
import com.sdl.odata.api.processor.query.CriteriaValue
import com.sdl.odata.api.processor.query.EndsWithMethodCriteria
import com.sdl.odata.api.processor.query.LiteralCriteriaValue
import com.sdl.odata.api.processor.query.MethodCriteria
import com.sdl.odata.api.processor.query.PropertyCriteriaValue
import com.sdl.odata.api.processor.query.StartsWithMethodCriteria
import io.qpointz.mill.data.odata.resolve.EdmPropertyResolver
import org.apache.calcite.rex.RexLiteral
import org.apache.calcite.rex.RexNode
import org.apache.calcite.sql.SqlKind
import org.apache.calcite.sql.`fun`.SqlStdOperatorTable
import org.apache.calcite.tools.RelBuilder
import org.apache.calcite.util.DateString
import java.sql.Date
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.GregorianCalendar

/**
 * Converts RWS OData query criteria AST nodes to Calcite {@link RexNode} predicates.
 */
class ODataExpressionToRex(
    private val propertyResolver: EdmPropertyResolver,
) {

    /**
     * @param schemaName physical schema from the OData service root
     * @param tableName entity set name (physical table)
     * @param criteria RWS criteria AST root
     * @param relBuilder active relational builder (scan already pushed)
     * @return filter expression
     */
    fun toRex(
        schemaName: String,
        tableName: String,
        criteria: Criteria,
        relBuilder: RelBuilder,
    ): RexNode = convertCriteria(schemaName, tableName, criteria, relBuilder)

    private fun convertCriteria(
        schemaName: String,
        tableName: String,
        criteria: Criteria,
        relBuilder: RelBuilder,
    ): RexNode {
        return when (criteria) {
            is ComparisonCriteria -> convertComparison(schemaName, tableName, criteria, relBuilder)
            is CompositeCriteria -> convertComposite(schemaName, tableName, criteria, relBuilder)
            is MethodCriteria -> convertMethod(schemaName, tableName, criteria, relBuilder)
            else -> throw ODataExpressionException("Unsupported filter expression: ${criteria::class.java.simpleName}")
        }
    }

    private fun convertComposite(
        schemaName: String,
        tableName: String,
        criteria: CompositeCriteria,
        relBuilder: RelBuilder,
    ): RexNode {
        val left = convertCriteria(schemaName, tableName, criteria.left, relBuilder)
        val right = convertCriteria(schemaName, tableName, criteria.right, relBuilder)
        return when {
            ODataCriteriaOperators.isAnd(criteria.operator) -> relBuilder.and(left, right)
            ODataCriteriaOperators.isOr(criteria.operator) -> relBuilder.or(left, right)
            else -> throw ODataExpressionException("Unsupported boolean operator: ${criteria.operator}")
        }
    }

    private fun convertComparison(
        schemaName: String,
        tableName: String,
        criteria: ComparisonCriteria,
        relBuilder: RelBuilder,
    ): RexNode {
        val left = valueToRex(schemaName, tableName, criteria.left, relBuilder)
        val right = valueToRex(schemaName, tableName, criteria.right, relBuilder)
        val (lhs, rhs) = coerceComparisonOperands(left, right, relBuilder)
        val sqlOp = ODataCriteriaOperators.comparisonSqlOperator(criteria.operator)
        return relBuilder.call(sqlOp, lhs, rhs)
    }

    /**
     * Aligns literal and property operand types so Substrait conversion accepts the filter
     * (e.g. CSV-backed {@code id} columns are strings while OData integer literals are i32).
     */
    private fun coerceComparisonOperands(
        left: RexNode,
        right: RexNode,
        relBuilder: RelBuilder,
    ): Pair<RexNode, RexNode> {
        if (left.type == right.type) {
            return left to right
        }
        val rexBuilder = relBuilder.rexBuilder
        return when {
            isLiteral(right) && !isLiteral(left) ->
                left to rexBuilder.makeCast(left.type, right, true, true)
            isLiteral(left) && !isLiteral(right) ->
                rexBuilder.makeCast(right.type, left, true, true) to right
            else ->
                left to rexBuilder.makeCast(left.type, right, true, true)
        }
    }

    private fun isLiteral(node: RexNode): Boolean =
        node.isA(SqlKind.LITERAL) || node is RexLiteral

    private fun convertMethod(
        schemaName: String,
        tableName: String,
        criteria: MethodCriteria,
        relBuilder: RelBuilder,
    ): RexNode {
        return when (criteria) {
            is ContainsMethodCriteria -> {
                val field = valueToRex(schemaName, tableName, criteria.property, relBuilder)
                val literal = literalValue(criteria.stringLiteral)
                relBuilder.call(
                    SqlStdOperatorTable.LIKE,
                    field,
                    relBuilder.rexBuilder.makeLiteral("%$literal%"),
                )
            }
            is StartsWithMethodCriteria -> {
                val field = valueToRex(schemaName, tableName, criteria.property, relBuilder)
                val literal = literalValue(criteria.stringLiteral)
                relBuilder.call(
                    SqlStdOperatorTable.LIKE,
                    field,
                    relBuilder.rexBuilder.makeLiteral("$literal%"),
                )
            }
            is EndsWithMethodCriteria -> {
                val field = valueToRex(schemaName, tableName, criteria.property, relBuilder)
                val literal = literalValue(criteria.stringLiteral)
                relBuilder.call(
                    SqlStdOperatorTable.LIKE,
                    field,
                    relBuilder.rexBuilder.makeLiteral("%$literal%"),
                )
            }
            else -> throw ODataExpressionException("Unsupported method criteria: ${criteria::class.java.simpleName}")
        }
    }

    private fun valueToRex(
        schemaName: String,
        tableName: String,
        value: CriteriaValue,
        relBuilder: RelBuilder,
    ): RexNode {
        return when (value) {
            is PropertyCriteriaValue -> {
                val index = propertyResolver.columnIndex(schemaName, tableName, value.propertyName)
                    ?: throw ODataExpressionException("Unknown property: ${value.propertyName}")
                relBuilder.field(index)
            }
            is LiteralCriteriaValue -> {
                val literal = value.value
                    ?: throw ODataExpressionException("NULL literal filters are not supported in v1")
                toCalciteLiteral(literal, relBuilder)
            }
            else -> throw ODataExpressionException("Unsupported criteria value: ${value::class.java.simpleName}")
        }
    }

    private fun toCalciteLiteral(literal: Any, relBuilder: RelBuilder): RexNode {
        val calendarDate = calendarDate(literal) ?: return relBuilder.literal(literal)
        return relBuilder.rexBuilder.makeDateLiteral(dateString(calendarDate))
    }

    private fun calendarDate(literal: Any): LocalDate? =
        when (literal) {
            is LocalDate -> literal
            is Date -> literal.toLocalDate()
            is OffsetDateTime -> literal.toLocalDate()
            is ZonedDateTime -> literal.toLocalDate()
            is Instant -> literal.atOffset(ZoneOffset.UTC).toLocalDate()
            is java.util.Date -> literal.toInstant().atOffset(ZoneOffset.UTC).toLocalDate()
            is GregorianCalendar -> LocalDate.of(
                literal.get(java.util.Calendar.YEAR),
                literal.get(java.util.Calendar.MONTH) + 1,
                literal.get(java.util.Calendar.DAY_OF_MONTH),
            )
            else -> null
        }

    private fun dateString(localDate: LocalDate): DateString =
        DateString.fromDaysSinceEpoch(localDate.toEpochDay().toInt())

    private fun literalValue(value: CriteriaValue): String =
        when (value) {
            is LiteralCriteriaValue -> value.value?.toString()
                ?: throw ODataExpressionException("String method requires non-null literal")
            else -> throw ODataExpressionException("String method requires literal operand")
        }
}

/**
 * Thrown when an OData filter cannot be translated to {@link RexNode}.
 */
class ODataExpressionException(message: String) : RuntimeException(message)
