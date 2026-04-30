package io.qpointz.mill.ai.test.scenario.json

import tools.jackson.databind.JsonNode
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider

interface JsonPathMatcher {

    private val jsonPathConfig: Configuration
        get() = Configuration.builder()
            .jsonProvider(JacksonJsonNodeJsonProvider())
            .mappingProvider(JacksonMappingProvider())
            .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .build()

    fun JsonNode.matches(expression: String): Boolean {
        return try {
            val result: Any? = JsonPath.using(jsonPathConfig).parse(this).read(expression)
            when (result) {
                null -> false
                is Collection<*> -> result.isNotEmpty()
                is JsonNode -> !result.isMissingNode && !result.isNull
                else -> true
            }
        } catch (_: Exception) {
            false
        }
    }
}
