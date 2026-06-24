package io.qpointz.mill.data.odata.service.render

import com.sdl.odata.api.ODataException
import com.sdl.odata.api.ODataSystemException
import com.sdl.odata.api.parser.MetadataUri
import com.sdl.odata.api.parser.ODataUri
import com.sdl.odata.api.processor.query.QueryResult
import com.sdl.odata.api.service.ODataRequestContext
import com.sdl.odata.api.service.ODataResponse
import com.sdl.odata.renderer.AbstractRenderer
import com.sdl.odata.renderer.metadata.MetadataDocumentWriter
import io.qpointz.mill.data.odata.annotation.EdmAnnotationProvider
import io.qpointz.mill.data.odata.render.CsdlMetadataAnnotationEnhancer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets

/**
 * Renders {@code $metadata} with facet-derived CSDL annotations.
 *
 * <p>Scores above RWS {@code MetadataDocumentRenderer} so Mill replaces the default metadata output.
 */
@Component
class MillMetadataDocumentRenderer(
    private val annotationProvider: EdmAnnotationProvider,
) : AbstractRenderer() {

    /**
     * @param requestContext inbound OData request context
     * @param data processor result (unused for metadata)
     * @return renderer score; positive only for {@code $metadata} requests
     */
    override fun score(requestContext: ODataRequestContext, data: QueryResult): Int {
        val uri = requestContext.uri
        return if (uri != null && uri.relativeUri() is MetadataUri) SCORE else 0
    }

    /**
     * @param requestContext inbound OData request context
     * @param data processor result (unused)
     * @param responseBuilder outbound response builder
     */
    @Throws(ODataException::class)
    override fun render(
        requestContext: ODataRequestContext,
        data: QueryResult,
        responseBuilder: ODataResponse.Builder,
    ) {
        LOG.debug("Rendering annotated \$metadata for request: {}", requestContext)

        val edm = requestContext.entityDataModel
        val writer = MetadataDocumentWriter(edm)
        writer.startDocument()
        writer.writeMetadataDocument()
        writer.endDocument()

        val schemaName = edm.entityContainer.name
        val annotations = annotationProvider.annotationsForSchema(schemaName)
        val xml = CsdlMetadataAnnotationEnhancer.enhance(writer.xml, annotations)

        try {
            responseBuilder
                .setStatus(ODataResponse.Status.OK)
                .setContentType(com.sdl.odata.api.service.MediaType.XML)
                .setHeader("OData-Version", ODATA_VERSION_HEADER)
                .setBodyText(xml, StandardCharsets.UTF_8.name())
        } catch (e: UnsupportedEncodingException) {
            throw ODataSystemException(e)
        }
    }

    private companion object {
        private val LOG = LoggerFactory.getLogger(MillMetadataDocumentRenderer::class.java)
        private const val SCORE = 101
        private const val ODATA_VERSION_HEADER = "4.0"
    }
}
