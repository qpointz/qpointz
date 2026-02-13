package io.qpointz.mill.ai.test.scenario.json

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.JsonNode
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertNotNull

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "assert",
    visible = false)
@JsonSubTypes(
    JsonSubTypes.Type(value = JsonNodeNonEmptyAssert::class, name = "not-empty"),
    JsonSubTypes.Type(value = JsonJsonPathAssert::class, name = "json-path")
)
fun interface JsonNodeAssert {
    fun assert(node: JsonNode?)
}

class JsonJsonPathAssert(val exp:String) : JsonNodeAssert, JsonNodeMatcherMixin {
    override fun assert(node: JsonNode?) {
        assertTrue(node?.matches(exp)!!)
    }

}

class JsonNodeNonEmptyAssert: JsonNodeAssert {
    override fun assert(node: JsonNode?) {
        assertNotNull(node)
        assertFalse(node.isEmpty)
    }
}
