package io.qpointz.mill.ai.test.scenario.json

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.JsonNode
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "assert",
    visible = false,
)
@JsonSubTypes(
    JsonSubTypes.Type(value = JsonListNonEmptyAssert::class, name = "not-empty"),
    JsonSubTypes.Type(value = JsonListJsonPathAssert::class, name = "json-path"),
)
fun interface JsonListAssert {
    fun assert(nodes: List<JsonNode?>)
}

class JsonListNonEmptyAssert : JsonListAssert {
    override fun assert(nodes: List<JsonNode?>) {
        assertFalse(nodes.isEmpty(), "Expected non-empty JSON list")
    }
}

data class JsonListJsonPathAssert(
    val exp: String,
    val match: ListMatch = ListMatch.all,
) : JsonListAssert, JsonPathMatcher {

    enum class ListMatch { all, any, none, one }

    override fun assert(nodes: List<JsonNode?>) {
        val matchCount = nodes.count { it?.matches(exp) == true }
        assertTrue(
            when (match) {
                ListMatch.all  -> matchCount == nodes.size
                ListMatch.any  -> matchCount > 0
                ListMatch.none -> matchCount == 0
                ListMatch.one  -> matchCount == 1
            },
            "JSONPath '$exp' match=$match: matched $matchCount of ${nodes.size} nodes"
        )
    }
}
