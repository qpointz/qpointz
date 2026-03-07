package io.qpointz.mill.sql.dialect

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

class DialectLoader {
    private val mapper = ObjectMapper(YAMLFactory())
        .registerKotlinModule()
        .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

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
        val resource = "sql/dialects/$normalized/$normalized.yml"
        val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(resource)
            ?: throw DialectValidationException(
                message = "Dialect resource not found: $resource",
                errors = listOf("missing classpath resource $resource")
            )
        stream.use { return load(it, resource) }
    }
}
