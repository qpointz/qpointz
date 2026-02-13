package io.qpointz.mill.ai.test.scenario.json

import io.qpointz.mill.ai.streaming.Transformations.content
import io.qpointz.mill.ai.streaming.Transformations.json
import io.qpointz.mill.ai.test.scenario.Expectations
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.ai.chat.client.ChatClient

data class JsonNodeListExpectations(val asserts: List<JsonListAssert> ) : Expectations {
    override fun assert(spec: ChatClient.ChatClientRequestSpec) {
        val all = spec.stream()
            .content()
            .content()
            .json()
            .collectList()
            .block()
        asserts.forEach {a-> a.assert(all)}
    }
}

data class JsonNodeExpectations(val asserts: List<JsonNodeAssert> ) : Expectations {
    override fun assert(spec: ChatClient.ChatClientRequestSpec) {
        val all = spec.stream()
            .content()
            .content()
            .json()
            .collectList()
            .block()!!
            .first()
        asserts.forEach {a-> a.assert(all)}
    }
}
