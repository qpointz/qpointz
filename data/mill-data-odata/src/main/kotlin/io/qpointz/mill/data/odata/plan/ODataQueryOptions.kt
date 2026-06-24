package io.qpointz.mill.data.odata.plan

import com.sdl.odata.api.processor.query.Criteria
import com.sdl.odata.api.processor.query.CriteriaFilterOperation
import com.sdl.odata.api.processor.query.ExpandOperation
import com.sdl.odata.api.processor.query.JoinOperation
import com.sdl.odata.api.processor.query.LimitOperation
import com.sdl.odata.api.processor.query.OrderByOperation
import com.sdl.odata.api.processor.query.OrderByProperty
import com.sdl.odata.api.processor.query.QueryOperation
import com.sdl.odata.api.processor.query.SelectOperation
import com.sdl.odata.api.processor.query.SelectPropertiesOperation
import com.sdl.odata.api.processor.query.SkipOperation

/**
 * Normalized OData query options extracted from an RWS {@link QueryOperation} tree.
 */
data class ODataQueryOptions(
    val entitySetName: String,
    val filter: Criteria? = null,
    val select: List<String> = emptyList(),
    val orderBy: List<OrderByProperty> = emptyList(),
    val top: Int? = null,
    val skip: Int? = null,
    val expands: List<JoinOperation> = emptyList(),
    val expandNavigationNames: List<String> = emptyList(),
    val selectDistinct: Boolean = false,
) {
    companion object {
        /**
         * Flattens an RWS query operation tree into {@link ODataQueryOptions}.
         */
        fun from(operation: QueryOperation): ODataQueryOptions {
            var current: QueryOperation = operation
            var filter: Criteria? = null
            var select: List<String> = emptyList()
            var orderBy: List<OrderByProperty> = emptyList()
            var top: Int? = null
            var skip: Int? = null
            var selectDistinct = false
            val expands = mutableListOf<JoinOperation>()
            val expandNavigationNames = mutableListOf<String>()

            while (true) {
                selectDistinct = selectDistinct || current.selectDistinct()
                when (current) {
                    is CriteriaFilterOperation -> {
                        filter = current.criteria
                        current = current.source
                    }
                    is SelectPropertiesOperation -> {
                        select = current.propertyNamesAsJava
                        current = current.source
                    }
                    is OrderByOperation -> {
                        orderBy = current.orderByPropertiesAsJava
                        current = current.source
                    }
                    is LimitOperation -> {
                        top = current.count
                        current = current.source
                    }
                    is SkipOperation -> {
                        skip = current.count
                        current = current.source
                    }
                    is JoinOperation -> {
                        expands += current
                        current = current.leftSource
                    }
                    is ExpandOperation -> {
                        expandNavigationNames += current.expandPropertiesAsJava
                        current = current.source
                    }
                    is SelectOperation -> {
                        return ODataQueryOptions(
                            entitySetName = current.entitySetName,
                            filter = filter,
                            select = select,
                            orderBy = orderBy,
                            top = top,
                            skip = skip,
                            expands = expands,
                            expandNavigationNames = expandNavigationNames,
                            selectDistinct = selectDistinct,
                        )
                    }
                    else -> throw IllegalArgumentException("Unsupported query operation: ${current::class.java.simpleName}")
                }
            }
        }
    }
}
