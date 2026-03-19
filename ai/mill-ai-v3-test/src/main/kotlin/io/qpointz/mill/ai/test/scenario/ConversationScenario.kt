package io.qpointz.mill.ai.test.scenario

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.kotlinModule

data class ConversationScenario(
    val name: String,
    val steps: List<Step> = emptyList(),
    val measures: List<String> = emptyList(),
) {
    data class Step(
        val user: String,
        val system: String? = null,
        val expect: Expectations? = null,
        val measures: List<String>? = null,
    )

    companion object {
        private val yaml = ObjectMapper(YAMLFactory()).registerModule(kotlinModule())

        fun fromResource(resourcePath: String): List<ConversationScenario> {
            val stream = requireNotNull(
                Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
            ) { "Resource not found: $resourcePath" }
            return stream.use { s ->
                val result = mutableListOf<ConversationScenario>()
                yaml.readerFor(ConversationScenario::class.java)
                    .readValues<ConversationScenario>(s)
                    .forEachRemaining(result::add)
                result
            }
        }
    }
}
