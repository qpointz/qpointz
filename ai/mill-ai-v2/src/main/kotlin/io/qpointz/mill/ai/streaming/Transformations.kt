package io.qpointz.mill.ai.streaming

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import io.qpointz.mill.utils.JsonUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.util.function.Consumer

/**
 * Utility object providing reactive stream transformations for processing streaming data.
 *
 * This object contains functions that transform [Flux] streams, commonly used for
 * processing chunked streaming responses (e.g., from AI/LLM services) into structured data.
 */
object Transformations {

    /**
     * Buffers incoming string chunks and emits complete lines delimited by newline characters.
     *
     * This transformation is useful when receiving streaming text data that arrives in
     * arbitrary chunks but needs to be processed line-by-line. The function:
     * - Accumulates incoming chunks in an internal buffer
     * - Emits complete lines (without the newline character) as they become available
     * - Emits any remaining buffered content when the input stream completes
     * - Empty lines are skipped
     *
     * @param input the input [Flux] of nullable string chunks
     * @return a [Flux] emitting complete lines extracted from the input stream
     */
    fun combineContent(input: Flux<String?>) : Flux<String?> {
        return Flux.create<String?>(Consumer { sink: FluxSink<String?>? ->
            val buffer = StringBuilder()
            input.subscribe(
                Consumer { chunk: String? ->
                    buffer.append(chunk)
                    var idx: Int
                    while ((buffer.indexOf("\n").also { idx = it }) >= 0) {
                        val line = buffer.substring(0, idx)
                        buffer.delete(0, idx + 1)
                        if (line.isNotEmpty()) {
                            sink!!.next(line)
                        }
                    }
                },
                Consumer { e: Throwable? -> sink!!.error(e) },
                {
                    if (buffer.isNotEmpty()) {
                        sink!!.next(buffer.toString())
                    }
                    sink!!.complete()
                }
            )
        })
    }

    fun Flux<String?>.content(): Flux<String?> {
        return Transformations.combineContent(this)
    }

    /**
     * Transforms a stream of JSON strings into a stream of parsed [JsonNode] objects.
     *
     * Each string element in the input stream is parsed as JSON using the default
     * Jackson ObjectMapper from [JsonUtils]. This is typically used after [combineContent]
     * to parse line-delimited JSON (NDJSON/JSON Lines) streams.
     *
     * @param input the input [Flux] of nullable JSON strings
     * @return a [Flux] emitting parsed [JsonNode] objects
     * @throws com.fasterxml.jackson.core.JsonProcessingException propagated via error signal
     *         if any input string is not valid JSON
     */
    fun combineToJson(input: Flux<String?>) : Flux<JsonNode?> {
        return input.map {
            JsonUtils.defaultJsonMapper().readTree(it)
        }
    }

    fun Flux<String?>.json(): Flux<JsonNode?> {
        return combineToJson(this)
    }

    inline fun <reified T> combineTo(input: Flux<JsonNode?>): Flux<T?> {
        val mapper = JsonUtils.defaultJsonMapper()
        return input
            .map { node ->
                node.let {
                    mapper.treeToValue(it,T::class.java)
                }
            }
    }

    inline fun <reified T> Flux<JsonNode?>.to(): Flux<T?> {
        return combineTo(this)
    }

}