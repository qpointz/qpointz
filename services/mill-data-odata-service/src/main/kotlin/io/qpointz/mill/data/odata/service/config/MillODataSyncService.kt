package io.qpointz.mill.data.odata.service.config

import com.sdl.odata.api.ODataClientException
import com.sdl.odata.api.ODataErrorCode
import com.sdl.odata.api.ODataException
import com.sdl.odata.api.ODataServerException
import com.sdl.odata.api.parser.ODataParser
import com.sdl.odata.api.processor.ODataQueryProcessor
import com.sdl.odata.api.processor.ProcessorResult
import com.sdl.odata.api.processor.query.QueryResult
import com.sdl.odata.api.renderer.ODataRenderer
import com.sdl.odata.api.renderer.RendererFactory
import com.sdl.odata.api.service.ODataRequest
import com.sdl.odata.api.service.ODataRequestContext
import com.sdl.odata.api.service.ODataResponse
import io.qpointz.mill.data.backend.SchemaProvider
import io.qpointz.mill.data.odata.service.edm.ODataEdmRegistryCache
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Synchronous OData request handler that mirrors the RWS parse → process → render pipeline without Pekko.
 */
class MillODataSyncService(
    private val edmRegistryCache: ODataEdmRegistryCache,
    private val schemaProvider: SchemaProvider,
    private val oDataParser: ODataParser,
    private val queryProcessor: ODataQueryProcessor,
    private val rendererFactory: RendererFactory,
) {

    /**
     * Handles an inbound servlet request and writes the OData response.
     *
     * @param schema physical schema from the URL path
     * @param request inbound HTTP request
     * @param response outbound HTTP response
     */
    @Throws(Exception::class)
    fun handle(schema: String, request: HttpServletRequest, response: HttpServletResponse) {
        if (!isValidSchemaName(schema) || !schemaProvider.isSchemaExists(schema)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown schema: $schema")
            return
        }

        val oDataRequest = buildODataRequest(request)
        val edm = edmRegistryCache.registryFor(schema).entityDataModel
        try {
            val uri = oDataParser.parseUri(oDataRequest.uri, edm)
            val context = ODataRequestContext(oDataRequest, uri, edm)
            val processorResult = queryProcessor.query(context, null)
            val oDataResponse = render(context, processorResult)
            writeServletResponse(oDataResponse, response)
        } catch (ex: ODataException) {
            val uri = try {
                oDataParser.parseUri(oDataRequest.uri, edm)
            } catch (_: Exception) {
                null
            }
            val context = if (uri != null) {
                ODataRequestContext(oDataRequest, uri, edm)
            } else {
                ODataRequestContext(oDataRequest, edm)
            }
            val status = httpStatusFor(ex)
            val errorResult = QueryResult.from(ex)
            val responseBuilder = ODataResponse.Builder().setStatus(status)
            selectRenderer(context, errorResult)?.render(context, errorResult, responseBuilder)
            writeServletResponse(responseBuilder.build(), response)
        }
    }

    private fun isValidSchemaName(schema: String): Boolean =
        schema.isNotBlank() && !schema.contains('/') && !schema.contains("..")

    private fun render(context: ODataRequestContext, processorResult: ProcessorResult): ODataResponse {
        val queryResult = processorResult.queryResult
            ?: throw ODataServerException(
                ODataErrorCode.PROCESSOR_ERROR,
                "Missing query result for ${context.request.uri}",
            )
        val responseBuilder = ODataResponse.Builder()
            .setStatus(processorResult.status)
            .setHeaders(processorResult.headers)
        val renderer = selectRenderer(context, queryResult)
            ?: throw ODataServerException(
                ODataErrorCode.RENDERER_ERROR,
                "No renderer for ${context.request.uri}",
            )
        renderer.render(context, queryResult, responseBuilder)
        return responseBuilder.build()
    }

    private fun selectRenderer(
        context: ODataRequestContext,
        queryResult: QueryResult,
    ): ODataRenderer? =
        rendererFactory.renderers
            .asSequence()
            .map { renderer -> renderer to renderer.score(context, queryResult) }
            .filter { (_, score) -> score > 0 }
            .maxByOrNull { (_, score) -> score }
            ?.first

    @Throws(IOException::class)
    private fun buildODataRequest(request: HttpServletRequest): ODataRequest {
        val builder = ODataRequest.Builder()
            .setMethod(ODataRequest.Method.valueOf(request.method))
        val url = StringBuilder()
            .append(request.scheme)
            .append("://")
            .append(request.serverName)
        val port = request.serverPort
        val defaultPort = if (request.scheme == "https") HTTPS_PORT else HTTP_PORT
        if (port > 0 && port != defaultPort) {
            url.append(':').append(port)
        }
        url.append(request.requestURI)
        val queryString = request.queryString
        if (!queryString.isNullOrEmpty()) {
            url.append('?').append(queryString)
        }
        builder.setUri(url.toString())
        request.headerNames.asIterator().forEachRemaining { name ->
            builder.setHeader(name, request.getHeader(name))
        }
        val body = readBody(request)
        if (body.isNotEmpty()) {
            builder.setBody(body)
        }
        return builder.build()
    }

    @Throws(IOException::class)
    private fun readBody(request: HttpServletRequest): ByteArray {
        request.inputStream.use { input ->
            ByteArrayOutputStream().use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var read = input.read(buffer)
                while (read != -1) {
                    output.write(buffer, 0, read)
                    read = input.read(buffer)
                }
                return output.toByteArray()
            }
        }
    }

    @Throws(IOException::class)
    private fun writeServletResponse(oDataResponse: ODataResponse, response: HttpServletResponse) {
        response.status = oDataResponse.status.code
        oDataResponse.headers.forEach { (name, value) -> response.setHeader(name, value) }
        val body = oDataResponse.body
        if (body != null && body.isNotEmpty()) {
            response.outputStream.use { it.write(body) }
        } else {
            oDataResponse.streamingContent?.write(response)
        }
    }

    private fun httpStatusFor(ex: ODataException): ODataResponse.Status =
        when (ex) {
            is ODataClientException -> ODataResponse.Status.BAD_REQUEST
            else -> ODataResponse.Status.INTERNAL_SERVER_ERROR
        }

    private companion object {
        private const val BUFFER_SIZE = 1024
        private const val HTTP_PORT = 80
        private const val HTTPS_PORT = 443
    }
}
