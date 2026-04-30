package io.qpointz.mill.source.descriptor

import tools.jackson.databind.json.JsonMapper
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.dataformat.yaml.YAMLWriteFeature
import tools.jackson.module.kotlin.KotlinModule

/**
 * Pre-configured Jackson 3 mappers for source descriptors.
 *
 * These mappers include all necessary modules:
 * - [KotlinModule] for Kotlin data class support
 * - Classpath-discovered modules (e.g. Java time) via [findAndAddModules]
 * - [DescriptorModule] for SPI-driven polymorphic descriptor types
 *
 * The YAML mapper disables native YAML type tags so that polymorphic
 * type info is serialized as a regular `type` property (matching JSON behavior).
 */
object SourceObjectMapper {

    /**
     * YAML mapper suitable for reading/writing [SourceDescriptor] files.
     */
    val yaml: YAMLMapper by lazy {
        YAMLMapper.builder()
            .disable(YAMLWriteFeature.USE_NATIVE_TYPE_ID)
            .findAndAddModules()
            .addModule(KotlinModule.Builder().build())
            .addModule(DescriptorModule())
            .build()
    }

    /**
     * JSON mapper suitable for reading/writing [SourceDescriptor] as JSON.
     */
    val json: JsonMapper by lazy {
        JsonMapper.builder()
            .findAndAddModules()
            .addModule(KotlinModule.Builder().build())
            .addModule(DescriptorModule())
            .build()
    }
}
