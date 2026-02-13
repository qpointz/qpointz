package io.qpointz.mill.ai.test.scenario.json

import com.fasterxml.jackson.databind.JsonNode
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider

interface JsonNodeMatcherMixin {

    private val jsonPathConfig: Configuration
        get() = Configuration.builder()
            .jsonProvider(JacksonJsonNodeJsonProvider())
            .mappingProvider(JacksonMappingProvider())
            .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .build()

    fun JsonNode.matches(exp: String): Boolean {
        return try {
            val ctx = JsonPath.using(jsonPathConfig).parse(this)
            val result: Any? = ctx.read(exp)

            when (result) {
                null -> false
                is Collection<*> -> result.isNotEmpty()
                is JsonNode -> !result.isMissingNode && !result.isNull
                else -> true
            }
        } catch (e: Exception) {
            false
        }
    }
}