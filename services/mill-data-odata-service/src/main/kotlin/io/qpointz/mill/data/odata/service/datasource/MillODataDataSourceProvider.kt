package io.qpointz.mill.data.odata.service.datasource

import com.sdl.odata.api.ODataBadRequestException
import com.sdl.odata.api.ODataException
import com.sdl.odata.api.edm.model.EntityDataModel
import com.sdl.odata.api.parser.TargetType
import com.sdl.odata.api.processor.datasource.DataSource
import com.sdl.odata.api.processor.datasource.DataSourceProvider
import com.sdl.odata.api.processor.datasource.ODataDataSourceException
import com.sdl.odata.api.processor.datasource.TransactionalDataSource
import com.sdl.odata.api.processor.link.ODataLink
import com.sdl.odata.api.processor.query.QueryOperation
import com.sdl.odata.api.processor.query.QueryResult
import com.sdl.odata.api.processor.query.strategy.QueryOperationStrategy
import com.sdl.odata.api.service.ODataRequestContext
import com.sdl.odata.api.parser.ODataUri
import io.qpointz.mill.data.odata.edm.EntitySetNaming
import io.qpointz.mill.data.odata.exec.ODataQueryExecutor
import io.qpointz.mill.data.odata.expr.ODataExpressionException
import io.qpointz.mill.data.odata.render.ODataJsonFeedSerializer
import io.qpointz.mill.data.odata.service.ODataServiceProperties

/**
 * RWS {@link DataSourceProvider} that pushes OData reads to {@link ODataQueryExecutor}.
 */
class MillODataDataSourceProvider(
    private val queryExecutor: ODataQueryExecutor,
    private val serviceProperties: ODataServiceProperties,
) : DataSourceProvider {

    private val dataSource = MillODataDataSource()
    private val feedSerializer = ODataJsonFeedSerializer()

    override fun isSuitableFor(requestContext: ODataRequestContext, entityType: String): Boolean {
        return try {
            requestContext.entityDataModel.getType(entityType) != null
        } catch (_: ODataException) {
            false
        }
    }

    override fun getDataSource(requestContext: ODataRequestContext): DataSource = dataSource

    override fun getStrategy(
        requestContext: ODataRequestContext,
        operation: QueryOperation,
        expectedODataEntityType: TargetType,
    ): QueryOperationStrategy? {
        val edm = requestContext.entityDataModel
        val entityTypeName = entityTypeName(operation, edm) ?: return null
        if (!isSuitableFor(requestContext, entityTypeName)) {
            return null
        }
        val schemaName = EntitySetNaming.extractSchemaFromServiceRoot(requestContext.uri.serviceRoot())
            ?: return null
        return QueryOperationStrategy {
            try {
                val maxRows = serviceProperties.getMaxTop().coerceAtLeast(1)
                val rows = queryExecutor.executeToMaps(operation, schemaName, maxRows)
                val json = feedSerializer.serializeFeed(
                    rows,
                    operation.entitySetName(),
                    requestContext.uri.serviceRoot(),
                )
                QueryResult.from(json)
            } catch (ex: ODataExpressionException) {
                throw ODataBadRequestException(ex.message ?: "Invalid OData filter", ex)
            }
        }
    }

    private fun entityTypeName(operation: QueryOperation, edm: EntityDataModel): String? {
        val setName = operation.entitySetName()
        val entitySet = edm.entityContainer.getEntitySet(setName)
        if (entitySet != null) {
            return entitySet.typeName
        }
        return edm.entityContainer.getSingleton(setName)?.typeName
    }

    /**
     * Read-only Mill data source; mutations are not supported in v1.
     */
    private class MillODataDataSource : DataSource {
        override fun create(uri: ODataUri, entity: Any, edm: EntityDataModel): Any =
            throw ODataDataSourceException("Create is not supported")

        override fun update(uri: ODataUri, entity: Any, edm: EntityDataModel): Any =
            throw ODataDataSourceException("Update is not supported")

        override fun delete(uri: ODataUri, edm: EntityDataModel) =
            throw ODataDataSourceException("Delete is not supported")

        override fun createLink(uri: ODataUri, link: ODataLink, edm: EntityDataModel) =
            throw ODataDataSourceException("Link create is not supported")

        override fun deleteLink(uri: ODataUri, link: ODataLink, edm: EntityDataModel) =
            throw ODataDataSourceException("Link delete is not supported")

        override fun startTransaction(): TransactionalDataSource =
            throw ODataDataSourceException("Transactions are not supported")
    }
}
