package io.qpointz.mill.ai.runtime.langchain4j

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

/**
 * Callback-based line buffer that accumulates arbitrary token chunks and emits complete
 * newline-delimited lines.
 *
 * Ported from mill-ai-v2 Transformations.combineContent() — same StringBuilder algorithm,
 * adapted from Reactor Flux to plain callbacks for use with LangChain4j streaming handlers.
 */
class JsonlLineBuffer(
    private val onLine: (String) -> Unit,
    private val onError: (Throwable) -> Unit = {},
    private val onComplete: () -> Unit = {},
) {
    private val buffer = StringBuilder()

    fun onToken(chunk: String?) {
        if (chunk == null) return
        buffer.append(chunk)
        var idx: Int
        while (buffer.indexOf("\n").also { idx = it } >= 0) {
            val line = buffer.substring(0, idx)
            buffer.delete(0, idx + 1)
            if (line.isNotEmpty()) onLine(line)
        }
    }

    fun complete() {
        if (buffer.isNotEmpty()) onLine(buffer.toString())
        onComplete()
    }

    fun error(e: Throwable) = onError(e)
}





