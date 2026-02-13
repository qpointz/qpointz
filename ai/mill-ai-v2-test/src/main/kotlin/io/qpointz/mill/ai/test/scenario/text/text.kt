package io.qpointz.mill.ai.test.scenario.text

import io.qpointz.mill.ai.streaming.Transformations.content
import io.qpointz.mill.ai.test.scenario.Expectations
import org.springframework.ai.chat.client.ChatClient

interface TextAssert {
    fun assert (text: String?)
}

data class TextExpectations(val asserts: List<TextAssert> ) : Expectations {

    override fun assert(spec: ChatClient.ChatClientRequestSpec) {
       val all = spec
           .stream()
           .content()
           .content()
           .collectList()
           .block()!!
        all.forEach { t -> asserts.forEach {a -> a.assert(t) } }
    }

}