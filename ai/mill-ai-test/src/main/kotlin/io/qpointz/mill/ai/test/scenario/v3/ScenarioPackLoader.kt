package io.qpointz.mill.ai.test.scenario.v3

import tools.jackson.databind.json.JsonMapper
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

/**
 * Loads [ScenarioPack] documents from classpath resources or filesystem paths.
 */
object ScenarioPackLoader {

    private val yamlMapper: YAMLMapper = YAMLMapper.builder()
        .addModule(kotlinModule())
        .build()

    val jsonMapper: JsonMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .build()

    /**
     * Loads a pack from a classpath resource (e.g. `scenarios/harness-smoke-hello.yml`).
     *
     * @param resourcePath Path relative to the classloader root (typically `src/test/resources` or `src/testIT/resources`).
     * @param classLoader Classloader used to resolve the resource; defaults to this object's loader.
     */
    fun fromClasspath(
        resourcePath: String,
        classLoader: ClassLoader = ScenarioPackLoader::class.java.classLoader,
    ): ScenarioPack {
        val stream = classLoader.getResourceAsStream(resourcePath)
            ?: error("Scenario resource not found on classpath: $resourcePath")
        return fromStream(stream)
    }

    /**
     * Loads a pack from a filesystem path.
     *
     * @param path Absolute or relative path to a `.yml` file.
     */
    fun fromFile(path: Path): ScenarioPack = fromStream(path.inputStream())

    /**
     * Parses YAML from an open stream (caller closes the stream).
     *
     * @param stream YAML input.
     */
    fun fromStream(stream: InputStream): ScenarioPack =
        stream.use { yamlMapper.readValue<ScenarioPack>(it) }
}
