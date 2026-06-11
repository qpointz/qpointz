package io.qpointz.mill.ai.test.scenario.v3

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Resolves committed scenario baseline paths on the classpath or under test resources.
 */
object BaselineResourcePaths {

    /**
     * Classpath resource path for a pack baseline, e.g. `scenarios/baselines/foo.record.normalized.json`.
     *
     * @param baselineResource Resource path relative to `src/testIT/resources` or `src/test/resources`.
     * @param classLoader Classloader used to load scenario packs.
     * @return Filesystem path to the baseline file.
     */
    fun resolve(baselineResource: String, classLoader: ClassLoader): Path {
        classLoader.getResource(baselineResource)?.let { return Paths.get(it.toURI()) }
        if (System.getenv("UPDATE_BASELINES") == "1") {
            return moduleRelativeTestResource(baselineResource)
        }
        error("baseline resource not found: $baselineResource")
    }

    private fun moduleRelativeTestResource(baselineResource: String): Path {
        val candidates = listOf(
            Paths.get("src/testIT/resources").resolve(baselineResource),
            Paths.get("src/test/resources").resolve(baselineResource),
            Paths.get("ai/mill-ai-test/src/testIT/resources").resolve(baselineResource),
        )
        return candidates.firstOrNull { Files.exists(it.parent) } ?: candidates.first()
    }
}
