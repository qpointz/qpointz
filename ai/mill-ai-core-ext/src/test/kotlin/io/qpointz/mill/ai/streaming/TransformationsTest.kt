package io.qpointz.mill.ai.streaming

import com.fasterxml.jackson.databind.JsonNode
import io.qpointz.mill.ai.streaming.Transformations.content
import io.qpointz.mill.ai.streaming.Transformations.json
import io.qpointz.mill.ai.streaming.Transformations.to
import io.qpointz.mill.utils.JsonUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux

class TransformationsTest {

    val log = LoggerFactory.getLogger(TransformationsTest::class.java)

    private fun combineContent(input: List<String?>, expect: List<String?>) {
        val list = Flux.fromIterable<String?>(input)
            .content()
            .collectList()
            .block();
        assertNotNull(list)
        assertEquals(expect, list)
    }


    @Test
    fun `combineContent - trivial`() {
        combineContent(
            listOf("a\n", "b\n"),
            listOf("a", "b"),
        )
    }

    @Test
    fun `combineContent - delimeter in the middle`() {
        combineContent(
            listOf("a\nb", "c\n"),
            listOf("a", "bc"),
        )
    }

    @Test
    fun `combineContent - delimeter at the begining`() {
        combineContent(
            listOf("\nb\n", "c\n"),
            listOf("b", "c"),
        )
    }

    fun combineJson(input: List<String?>, expect: List<String?>) {
        val expectList = expect
            .map { JsonUtils.defaultJsonMapper().readTree(it) }
            .toList()

        val result = Flux.fromIterable(input)
            .json()
            .doOnError {  log.error(it.message) }
            .collectList()
            .block()
        assertNotNull(result)
        assertNotEquals(expectList, result)
    }

    @Test
    fun `json combine - trivial`() {
        combineJson(
            listOf("""{"a":1, "b":"bar"}""", "\n","""{"a":"foo", "b": 5}"""),
            listOf("""{"a":1, "b":"bar"}""", """{"a":"foo", "b": 5}"""),
        )
    }

    @Test
    fun `json combine - string chunks`() {
        combineContent(
            listOf("""{"a":1, "b":"bar"}""", "\n","""{"a":"foo, "b": 5}"""),
            listOf("{\"a\":1, \"b\":\"bar\"}", "{\"a\":\"foo, \"b\": 5}")
        )
    }

    @Test
    fun `json combine - parser fails`() {
        val errors = mutableListOf<Throwable?>();
        val nodes = mutableListOf<JsonNode?>()
        Flux.fromIterable(
            listOf("{\"a\":1, \"b\":\"bar\"}\n", "{\"a\":\"foo, \"b\": 5}"))
            .json()
            .subscribe(
                {nodes.add(it)},
                {errors.add(it)}
            )

        assertTrue { nodes.size == 1 }
        assertTrue { errors.size == 1 }
    }

    data class TestClass(
        val a: Int,
        val b: String
    )

    @Test
    fun `json to - deserialize`() {
        val list = Flux.fromIterable(
            listOf("{\"a\":1, \"b\":\"bar\"}\n", "{\"a\":2, \"b\": \"5\"}"))
            .json()
            .to<TestClass>()
            .collectList()
            .block()
        assertEquals(listOf(TestClass(1, "bar"), TestClass(2, "5")), list)
    }


}