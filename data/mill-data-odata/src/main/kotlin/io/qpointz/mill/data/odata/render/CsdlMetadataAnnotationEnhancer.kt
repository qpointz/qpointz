package io.qpointz.mill.data.odata.render

import io.qpointz.mill.data.odata.annotation.EdmAnnotationModel
import io.qpointz.mill.data.odata.annotation.EdmCsdlAnnotation
import io.qpointz.mill.data.odata.annotation.ODataVocabularyTerms
import java.io.StringReader
import java.io.StringWriter
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLEventFactory

/**
 * Injects facet-derived CSDL annotations into an RWS-generated {@code $metadata} document.
 *
 * <p>RWS 2.16 programmatic EDM builders do not expose vocabulary annotations; this post-processor
 * adds them while preserving the rest of the metadata XML unchanged.
 */
object CsdlMetadataAnnotationEnhancer {

    private const val EDMX_NS = "http://docs.oasis-open.org/odata/ns/edmx"
    private const val EDM_NS = "http://docs.oasis-open.org/odata/ns/edm"
    private const val EDMX_PREFIX = "edmx"

    private val inputFactory: XMLInputFactory = XMLInputFactory.newInstance()
    private val outputFactory: XMLOutputFactory = XMLOutputFactory.newInstance()
    private val eventFactory: XMLEventFactory = XMLEventFactory.newInstance()

    /**
     * @param metadataXml base CSDL from RWS {@code MetadataDocumentWriter}
     * @param annotations facet-derived annotations for the schema
     * @return metadata XML with annotations and vocabulary references when needed
     */
    fun enhance(metadataXml: String, annotations: EdmAnnotationModel): String {
        if (annotations.isEmpty) {
            return metadataXml
        }
        val reader = inputFactory.createXMLEventReader(StringReader(metadataXml))
        val writer = StringWriter()
        val xmlWriter = outputFactory.createXMLEventWriter(writer)

        var insideEntityType = false
        var currentEntityTypeName: String? = null
        var currentPropertyName: String? = null
        var vocabularyReferenceWritten = false

        while (reader.hasNext()) {
            val event = reader.nextEvent()
            when {
                event.isStartElement -> {
                    xmlWriter.add(event)
                    when (event.asStartElement().name.localPart) {
                        "EntityType" -> {
                            insideEntityType = true
                            currentEntityTypeName = event.asStartElement().getAttributeByName(QName("Name"))?.value
                            currentPropertyName = null
                        }
                        "Property" -> {
                            currentPropertyName = event.asStartElement().getAttributeByName(QName("Name"))?.value
                        }
                        "DataServices" -> {
                            if (!vocabularyReferenceWritten) {
                                writeCoreVocabularyReference(xmlWriter)
                                vocabularyReferenceWritten = true
                            }
                        }
                    }
                }
                event.isEndElement -> {
                    when (event.asEndElement().name.localPart) {
                        "Key" -> {
                            xmlWriter.add(event)
                            if (insideEntityType && currentEntityTypeName != null) {
                                writeAnnotations(xmlWriter, annotations.entityType(currentEntityTypeName!!))
                            }
                        }
                        "Property" -> {
                            if (insideEntityType && currentEntityTypeName != null && currentPropertyName != null) {
                                writeAnnotations(
                                    xmlWriter,
                                    annotations.structuralProperty(currentEntityTypeName!!, currentPropertyName!!),
                                )
                            }
                            currentPropertyName = null
                            xmlWriter.add(event)
                        }
                        "EntityType" -> {
                            insideEntityType = false
                            currentEntityTypeName = null
                            currentPropertyName = null
                            xmlWriter.add(event)
                        }
                        else -> xmlWriter.add(event)
                    }
                }
                else -> xmlWriter.add(event)
            }
        }

        xmlWriter.flush()
        xmlWriter.close()
        reader.close()
        return writer.toString()
    }

    private fun writeCoreVocabularyReference(xmlWriter: javax.xml.stream.XMLEventWriter) {
        xmlWriter.add(
            eventFactory.createStartElement(
                QName(EDMX_NS, "Reference", EDMX_PREFIX),
                listOf(eventFactory.createAttribute("Uri", ODataVocabularyTerms.CORE_VOCABULARY_REFERENCE_URI)).iterator(),
                null,
            ),
        )
        xmlWriter.add(
            eventFactory.createStartElement(
                QName(EDMX_NS, "Include", EDMX_PREFIX),
                listOf(
                    eventFactory.createAttribute("Namespace", ODataVocabularyTerms.CORE_VOCABULARY_NAMESPACE),
                    eventFactory.createAttribute("Alias", ODataVocabularyTerms.CORE_VOCABULARY_ALIAS),
                ).iterator(),
                null,
            ),
        )
        xmlWriter.add(eventFactory.createEndElement(QName(EDMX_NS, "Include", EDMX_PREFIX), null))
        xmlWriter.add(eventFactory.createEndElement(QName(EDMX_NS, "Reference", EDMX_PREFIX), null))
    }

    private fun writeAnnotations(xmlWriter: javax.xml.stream.XMLEventWriter, annotations: List<EdmCsdlAnnotation>) {
        if (annotations.isEmpty()) {
            return
        }
        annotations.forEach { annotation ->
            xmlWriter.add(
                eventFactory.createStartElement(
                    QName(EDM_NS, "Annotation"),
                    listOf(
                        eventFactory.createAttribute("Term", annotation.term),
                        eventFactory.createAttribute("String", annotation.stringValue),
                    ).iterator(),
                    null,
                ),
            )
            xmlWriter.add(eventFactory.createEndElement(QName(EDM_NS, "Annotation"), null))
        }
    }
}
