package io.qpointz.mill.ai.test.scenario

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.qpointz.mill.ai.streaming.Transformations.content
import io.qpointz.mill.ai.test.scenario.json.JsonNodeExpectations
import io.qpointz.mill.ai.test.scenario.json.JsonNodeListExpectations
import io.qpointz.mill.ai.test.scenario.text.TextExpectations
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.ai.chat.client.ChatClient

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "format",
    visible = false)
@JsonSubTypes(
    JsonSubTypes.Type(value = JsonNodeListExpectations::class, name = "json-list"),
    JsonSubTypes.Type(value = JsonNodeExpectations::class, name = "json"),
    JsonSubTypes.Type(value = TextExpectations::class, name = "text")
)

interface Expectations {
    fun assert(spec: ChatClient.ChatClientRequestSpec)
}

class DefaultExpectations: Expectations {
    override fun assert(spec: ChatClient.ChatClientRequestSpec) {
        assertDoesNotThrow { spec
            .stream()
            .content()
            .content()
            .collectList()
            .block()!!}
    }

}




