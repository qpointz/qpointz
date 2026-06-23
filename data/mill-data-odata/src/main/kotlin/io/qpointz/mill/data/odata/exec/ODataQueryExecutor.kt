package io.qpointz.mill.data.odata.exec

import com.sdl.odata.api.processor.query.QueryOperation
import io.qpointz.mill.data.backend.calcite.RelPlanDispatcherBridge
import io.qpointz.mill.data.odata.plan.ODataRelComposer
import io.qpointz.mill.data.odata.read.ODataEntityReader
import io.qpointz.mill.proto.QueryExecutionConfig
import io.qpointz.mill.vectors.VectorBlockIterator

/**
 * Orchestrates OData query composition and execution through the Rel→Substrait dispatcher bridge.
 */
class ODataQueryExecutor @JvmOverloads constructor(
    private val relComposer: ODataRelComposer,
    private val bridge: RelPlanDispatcherBridge,
    private val entityReader: ODataEntityReader = ODataEntityReader(),
    private val defaultFetchSize: Int = 512,
) {

    /**
     * @param operation RWS query operation tree
     * @param schemaName physical schema from the OData service root
     * @param config optional execution config override
     * @return streaming vector blocks from the dispatcher
     */
    fun execute(
        operation: QueryOperation,
        schemaName: String,
        config: QueryExecutionConfig? = null,
    ): VectorBlockIterator {
        val relRoot = relComposer.compose(operation, schemaName)
        val executionConfig = config ?: QueryExecutionConfig.newBuilder()
            .setFetchSize(defaultFetchSize)
            .build()
        return bridge.execute(relRoot, executionConfig)
    }

    /**
     * @param operation RWS query operation tree
     * @param schemaName physical schema from the OData service root
     * @param maxRows maximum rows to materialize for HTTP rendering
     * @return row maps keyed by column name
     */
    fun executeToMaps(
        operation: QueryOperation,
        schemaName: String,
        maxRows: Int = Int.MAX_VALUE,
    ): List<Map<String, Any?>> {
        val iterator = execute(operation, schemaName)
        return entityReader.readAll(iterator, maxRows)
    }
}
