package io.qpointz.mill.data.odata.plan

import com.sdl.odata.api.processor.query.Ascending
import com.sdl.odata.api.processor.query.Criteria
import com.sdl.odata.api.processor.query.JoinOperation
import com.sdl.odata.api.processor.query.OrderByProperty
import com.sdl.odata.api.processor.query.QueryOperation
import io.qpointz.mill.data.odata.expr.ODataExpressionException
import io.qpointz.mill.data.odata.expr.ODataExpressionToRex
import io.qpointz.mill.data.odata.resolve.EdmPropertyResolver
import io.qpointz.mill.data.backend.calcite.RelBuilderFactory
import org.apache.calcite.rel.RelRoot
import org.apache.calcite.rel.core.JoinRelType
import org.apache.calcite.sql.`fun`.SqlStdOperatorTable
import org.apache.calcite.tools.RelBuilder
import io.qpointz.mill.data.backend.calcite.RelBuilderRoots

/**
 * Composes Calcite {@link RelRoot} plans from normalized OData query options.
 */
class ODataRelComposer @JvmOverloads constructor(
    private val relBuilderFactory: RelBuilderFactory,
    private val propertyResolver: EdmPropertyResolver,
    private val expressionToRex: ODataExpressionToRex,
    private val maxTop: Int = DEFAULT_MAX_TOP,
) {

    /**
     * @param operation RWS query operation tree
     * @param schemaName physical schema from the OData service root
     * @return composed relational plan root
     */
    fun compose(operation: QueryOperation, schemaName: String): RelRoot {
        val options = ODataQueryOptions.from(operation)
        return relBuilderFactory.withRelBuilder { builder ->
            composeOnBuilder(options, schemaName, builder)
        }
    }

    private fun composeOnBuilder(
        options: ODataQueryOptions,
        schemaName: String,
        builder: RelBuilder,
    ): RelRoot {
        val tableName = options.entitySetName
        if (propertyResolver.resolveTable(schemaName, tableName) == null) {
            throw ODataExpressionException("Unknown entity set: $tableName")
        }

        builder.scan(schemaName, tableName)

        options.expands.forEach { expand ->
            applyExpand(schemaName, options.entitySetName, expand, builder)
        }

        options.filter?.let { criteria ->
            val rex = expressionToRex.toRex(schemaName, options.entitySetName, criteria, builder)
            builder.filter(rex)
        }

        if (options.select.isNotEmpty()) {
            val fields = options.select.map { name ->
                val index = propertyResolver.columnIndex(schemaName, tableName, name)
                    ?: throw ODataExpressionException("Unknown select property: $name")
                builder.field(index)
            }.toTypedArray()
            builder.project(*fields)
        }

        if (options.orderBy.isNotEmpty()) {
            val fields = options.orderBy.map { order ->
                val index = propertyResolver.columnIndex(schemaName, tableName, order.propertyName)
                    ?: throw ODataExpressionException("Unknown orderby property: ${order.propertyName}")
                if (order.direction is Ascending) {
                    builder.field(index)
                } else {
                    builder.call(SqlStdOperatorTable.DESC, builder.field(index))
                }
            }.toTypedArray()
            builder.sort(*fields)
        }

        val top = options.top?.coerceAtMost(maxTop)
        val skip = options.skip ?: 0
        when {
            top != null && skip > 0 -> builder.limit(skip, top)
            top != null -> builder.limit(0, top)
            skip > 0 -> builder.limit(skip, Int.MAX_VALUE)
        }

        return RelBuilderRoots.toRoot(builder)
    }

    private fun applyExpand(
        schemaName: String,
        entitySetName: String,
        expand: JoinOperation,
        builder: RelBuilder,
    ) {
        val targetTable = expand.rightSource.entitySetName()
        if (propertyResolver.resolveTable(schemaName, targetTable) == null) {
            throw ODataExpressionException("Unknown expand target entity set: $targetTable")
        }
        builder.scan(schemaName, targetTable)
        val joinName = expand.joinPropertyName()
        val sourceIndex = propertyResolver.columnIndex(schemaName, entitySetName, joinName)
            ?: throw ODataExpressionException("Unknown expand join property: $joinName")
        val targetIndex = propertyResolver.columnIndex(schemaName, targetTable, joinName)
            ?: throw ODataExpressionException("Unknown expand target join property: $joinName")
        val joinType = if (expand.isOuterJoin) JoinRelType.LEFT else JoinRelType.INNER
        builder.join(
            joinType,
            builder.equals(builder.field(2, 0, sourceIndex), builder.field(2, 1, targetIndex)),
        )
    }

    companion object {
        const val DEFAULT_MAX_TOP: Int = 10_000
    }
}
