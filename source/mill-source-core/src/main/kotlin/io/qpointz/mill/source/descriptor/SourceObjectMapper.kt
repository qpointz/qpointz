package io.qpointz.mill.source.descriptor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

/**
 * Pre-configured [ObjectMapper] instances for source descriptors.
 *
 * These mappers include all necessary modules:
 * - [KotlinModule] for Kotlin data class support
 * - [JavaTimeModule] / [Jdk8Module] for date/time types
 * - [DescriptorModule] for SPI-driven polymorphic descriptor types
 *
 * The YAML mapper disables native YAML type tags so that polymorphic
 * type info is serialized as a regular `type` property (matching JSON behavior).
 */
object SourceObjectMapper {

    /**
     * YAML mapper suitable for reading/writing [SourceDescriptor] files.
     */
    val yaml: ObjectMapper by lazy {
        val factory = YAMLFactory.builder()
            .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
            .build()
        ObjectMapper(factory).apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            registerModule(Jdk8Module())
            registerModule(DescriptorModule())
        }
    }

    /**
     * JSON mapper suitable for reading/writing [SourceDescriptor] as JSON.
     */
    val json: ObjectMapper by lazy {
        ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            registerModule(Jdk8Module())
            registerModule(DescriptorModule())
        }
    }
}
