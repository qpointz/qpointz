package io.qpointz.mill.metadata.io

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.qpointz.mill.metadata.domain.MetadataEntity
import java.io.InputStream
import java.io.OutputStream

/** Contract for exporting metadata entities to an output stream. */
interface MetadataExporter {
    fun export(entities: Collection<MetadataEntity>, target: OutputStream)
}

/** Contract for importing metadata entities from an input stream. */
interface MetadataImporter {
    fun importFrom(source: InputStream): Collection<MetadataEntity>
}

/** JSON implementation of [MetadataExporter]. */
class JsonMetadataExporter : MetadataExporter {
    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerKotlinModule()
        enable(SerializationFeature.INDENT_OUTPUT)
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    override fun export(entities: Collection<MetadataEntity>, target: OutputStream) {
        objectMapper.writeValue(target, mapOf("entities" to entities))
    }
}

/** JSON implementation of [MetadataImporter]. */
class JsonMetadataImporter : MetadataImporter {
    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerKotlinModule()
    }

    override fun importFrom(source: InputStream): Collection<MetadataEntity> {
        val wrapper: Map<String, List<MetadataEntity>> =
            objectMapper.readValue(source, object : TypeReference<Map<String, List<MetadataEntity>>>() {})
        return wrapper["entities"] ?: emptyList()
    }
}

/** YAML implementation of [MetadataExporter]. */
class YamlMetadataExporter : MetadataExporter {
    private val yamlMapper = ObjectMapper(
        YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
    ).apply {
        registerModule(JavaTimeModule())
        registerKotlinModule()
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    override fun export(entities: Collection<MetadataEntity>, target: OutputStream) {
        yamlMapper.writeValue(target, mapOf("entities" to entities))
    }
}

/** YAML implementation of [MetadataImporter]. */
class YamlMetadataImporter : MetadataImporter {
    private val yamlMapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(JavaTimeModule())
        registerKotlinModule()
    }

    override fun importFrom(source: InputStream): Collection<MetadataEntity> {
        val wrapper: Map<String, List<MetadataEntity>> =
            yamlMapper.readValue(source, object : TypeReference<Map<String, List<MetadataEntity>>>() {})
        return wrapper["entities"] ?: emptyList()
    }
}
