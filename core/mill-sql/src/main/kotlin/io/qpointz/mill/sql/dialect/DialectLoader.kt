package io.qpointz.mill.sql.v2.dialect

import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

class DialectLoader {
    private val mapper = YAMLMapper.builder()
        .addModule(kotlinModule())
        .propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        .build()

    fun load(stream: InputStream, sourceName: String): SqlDialectSpec {
        return try {
            val spec: SqlDialectSpec = mapper.readValue(stream)
            DialectValidator.validate(spec, sourceName)
            spec
        } catch (ex: Exception) {
            throw DialectValidationException(
                message = "Failed to read dialect from $sourceName",
                errors = listOf(ex.message ?: "unknown parse error"),
                cause = ex
            )
        }
    }

    fun loadFromPath(path: Path): SqlDialectSpec {
        Files.newInputStream(path).use { stream ->
            return load(stream, path.toString())
        }
    }

    fun loadFromClasspath(dialectResourceId: String): SqlDialectSpec {
        val normalized = dialectResourceId.lowercase(Locale.ROOT)
        val resource = "sql/v2/dialects/$normalized/$normalized.yml"
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(resource)
            ?: throw DialectValidationException(
                message = "Dialect resource not found: $resource",
                errors = listOf("missing classpath resource $resource")
            )
        stream.use { return load(it, resource) }
    }
}
