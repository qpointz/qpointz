package io.qpointz.mill.ai.test.scenario.json

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.JsonNode
import io.qpointz.mill.ai.test.scenario.Expectations
import io.qpointz.mill.ai.test.scenario.text.TextExpectations
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "assert",
    visible = false)
@JsonSubTypes(
    JsonSubTypes.Type(value = NonEmptyAssert::class, name = "not-empty"),
    JsonSubTypes.Type(value = TextExpectations::class, name = "text")
)
interface JsonListAssert {
    fun assert(nodes: List<JsonNode>)
}


data class JsonListExpectations(val asserts: List<JsonListAssert> ) : Expectations {

}

class NonEmptyAssert: JsonListAssert {
    override fun assert(nodes: List<JsonNode>) {
        assertEquals(true, nodes != null && nodes.size>0)
    }

}
