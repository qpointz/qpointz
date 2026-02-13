package io.qpointz.mill.ai.test.scenario

import io.qpointz.mill.utils.YamlUtils
import java.io.InputStream

data class ConversationScenario(val name: String,
                                val measures: List<String> = emptyList(),
                                val conversation: List<Step> = emptyList(),
                                val persist: Boolean = true,
                                val expect: Expectations = DefaultExpectations()
) {
    companion object {
        fun fromResource(resourcePath: String): List<ConversationScenario> {
            fun rs(path: String): InputStream? {
                val classLoader = Thread.currentThread().contextClassLoader
                return requireNotNull(classLoader.getResourceAsStream(path)) {
                    "Resource not found: $path"
                }
            }

            val all = ArrayList<ConversationScenario>()
            YamlUtils.defaultYamlMapper()
                .readerFor(ConversationScenario::class.java)
                .readValues<ConversationScenario>(rs(resourcePath))
                .forEachRemaining { all.add(it) }

            return all
        }
    }

    data class Step(val user: String?,
                    val system: String?,
                    val measures: List<String>?,
                    val expect: Expectations?)

}