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
    visible = false)
@JsonSubTypes(
    JsonSubTypes.Type(value = JsonListNonEmptyAssert::class, name = "not-empty"),
    JsonSubTypes.Type(value = JsonListJsonPathAssert::class, name = "json-path")
)
fun interface JsonListAssert {
    fun assert(nodes: List<JsonNode?>)
}


class JsonListNonEmptyAssert: JsonListAssert {
    override fun assert(nodes: List<JsonNode?>) {
        assertFalse(nodes.isNullOrEmpty())
    }
}

data class JsonListJsonPathAssert(val exp:String, val match: ListMatch = ListMatch.all): JsonListAssert, JsonNodeMatcherMixin {

    enum class ListMatch(val match:String) {
        all("all"),
        any("any"),
        none("none"),
        one("one")
    }

    override fun assert(nodes: List<JsonNode?>) {
        val matches = nodes
            .map { node -> node?.matches(this.exp) }
            .count()

        assertTrue(
            match == ListMatch.all && matches == nodes.size ||
                    match == ListMatch.none && matches ==0 ||
                    match == ListMatch.one && matches == 1 ||
                    match == ListMatch.any && matches >0
        )
    }
}