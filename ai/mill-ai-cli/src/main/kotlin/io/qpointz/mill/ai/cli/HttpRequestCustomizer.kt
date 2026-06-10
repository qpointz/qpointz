package io.qpointz.mill.ai.cli

import java.net.http.HttpRequest

/**
 * Decorates outbound HTTP requests (e.g. auth headers). The default implementation is a no-op.
 *
 * Replace with a bean or subclass when the chat service enforces non-static identity or tokens.
 */
fun interface HttpRequestCustomizer {

    /**
     * @param builder Request under construction for a single call.
     * @return The same or a further-configured builder.
     */
    fun customize(builder: HttpRequest.Builder): HttpRequest.Builder
}

/** Default [HttpRequestCustomizer] that does not modify requests. */
object NoOpHttpRequestCustomizer : HttpRequestCustomizer {
    override fun customize(builder: HttpRequest.Builder): HttpRequest.Builder = builder
}
