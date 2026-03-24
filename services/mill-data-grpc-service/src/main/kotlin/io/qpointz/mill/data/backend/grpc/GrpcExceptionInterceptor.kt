package io.qpointz.mill.data.backend.grpc

import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.StatusRuntimeException
import io.qpointz.mill.annotations.service.ConditionalOnService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Maps uncaught exceptions on the server call listener to gRPC [Status], mirroring the v1
 * {@code MillGrpcServiceExceptionAdvice} behaviour.
 */
@Component
@ConditionalOnService(value = "grpc", group = "data")
class GrpcExceptionInterceptor : ServerInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun <Req : Any, Resp : Any> interceptCall(
        call: ServerCall<Req, Resp>,
        headers: Metadata,
        next: ServerCallHandler<Req, Resp>,
    ): ServerCall.Listener<Req> {
        val delegate = next.startCall(call, headers)
        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<Req>(delegate) {
            override fun onHalfClose() {
                runCatching { super.onHalfClose() }
                    .onFailure { handle(it, call) }
            }

            override fun onMessage(message: Req) {
                runCatching { super.onMessage(message) }
                    .onFailure { handle(it, call) }
            }

            override fun onReady() {
                runCatching { super.onReady() }
                    .onFailure { handle(it, call) }
            }
        }
    }

    /**
     * Closes the call with an appropriate [Status]. [StatusRuntimeException] / [StatusException] are
     * resolved from the causal chain (e.g. wrapper from an async boundary) and closed with their
     * status instead of mapping to [Status.UNKNOWN].
     */
    private fun handle(t: Throwable, call: ServerCall<*, *>) {
        findStatusThrowable(t)?.let { st ->
            when (st) {
                is StatusRuntimeException -> {
                    call.close(st.status, st.trailers ?: Metadata())
                    return
                }
                is StatusException -> {
                    call.close(st.status, st.trailers ?: Metadata())
                    return
                }
            }
        }
        when (t) {
            is Error -> {
                log.error("Intercepted JVM error on gRPC call", t)
                call.close(
                    Status.INTERNAL.withDescription("ERROR:${t.message}").withCause(t),
                    Metadata(),
                )
            }
            else -> {
                log.debug("Mapping exception to gRPC status", t)
                call.close(
                    Status.UNKNOWN.withDescription(t.message ?: "Unknown failure").withCause(t),
                    Metadata(),
                )
            }
        }
    }

    private fun findStatusThrowable(t: Throwable): Throwable? {
        var current: Throwable? = t
        val seen = HashSet<Throwable>()
        while (current != null) {
            when (current) {
                is StatusRuntimeException, is StatusException -> return current
                else -> {
                    if (!seen.add(current)) {
                        return null
                    }
                    current = current.cause
                }
            }
        }
        return null
    }
}
