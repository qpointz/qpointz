package io.qpointz.mill.data.backend.grpc

import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import io.qpointz.mill.annotations.security.ConditionalOnSecurity
import io.qpointz.mill.annotations.service.ConditionalOnService
import io.qpointz.mill.security.authentication.AuthenticationMethods
import io.qpointz.mill.security.authentication.AuthenticationType
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Authenticates inbound gRPC calls using the {@code authorization} metadata entry,
 * aligned with the v1 net.devh {@code BasicGrpcAuthenticationReader} / {@code BearerAuthenticationReader} setup.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnSecurity
@ConditionalOnService(value = "grpc", group = "data")
class GrpcSecurityInterceptor(
    private val authenticationMethods: AuthenticationMethods,
    private val authenticationManager: AuthenticationManager,
) : ServerInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun <Req : Any, Resp : Any> interceptCall(
        call: ServerCall<Req, Resp>,
        headers: Metadata,
        next: ServerCallHandler<Req, Resp>,
    ): ServerCall.Listener<Req> {
        val previous = SecurityContextHolder.getContext().authentication
        val authHeader = headers.get(AUTHORIZATION_KEY)
        val token = try {
            extractAuthentication(authHeader)
        } catch (e: BadCredentialsException) {
            log.debug("Malformed credentials on gRPC call: {}", e.message)
            call.close(Status.UNAUTHENTICATED.withDescription(e.message).withCause(e), Metadata())
            return noopListener()
        }

        if (token == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing or unsupported Authorization"), Metadata())
            return noopListener()
        }

        val authenticated = try {
            authenticationManager.authenticate(token)
        } catch (e: org.springframework.security.core.AuthenticationException) {
            log.debug("Authentication failed: {}", e.message)
            call.close(Status.UNAUTHENTICATED.withDescription(e.message).withCause(e), Metadata())
            return noopListener()
        }

        SecurityContextHolder.getContext().authentication = authenticated
        val delegate = next.startCall(call, headers)
        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<Req>(delegate) {
            override fun onComplete() {
                try {
                    super.onComplete()
                } finally {
                    restoreContext(previous)
                }
            }

            override fun onCancel() {
                try {
                    super.onCancel()
                } finally {
                    restoreContext(previous)
                }
            }
        }
    }

    private fun restoreContext(previous: Authentication?) {
        if (previous == null) {
            SecurityContextHolder.clearContext()
        } else {
            SecurityContextHolder.getContext().authentication = previous
        }
    }

    private fun extractAuthentication(rawHeader: String?): Authentication? {
        if (rawHeader.isNullOrBlank()) {
            return null
        }
        val types = authenticationMethods.authenticationTypes
        if (rawHeader.startsWith("Basic ", ignoreCase = true) && types.contains(AuthenticationType.BASIC)) {
            return parseBasic(rawHeader)
        }
        if (rawHeader.startsWith("Bearer ", ignoreCase = true) && types.contains(AuthenticationType.OAUTH2)) {
            val token = rawHeader.substring(7).trim()
            if (token.isEmpty()) {
                return null
            }
            return BearerTokenAuthenticationToken(token)
        }
        return null
    }

    private fun parseBasic(header: String): UsernamePasswordAuthenticationToken {
        val base64Token = header.substring(6).trim()
        val decoded = String(Base64.getDecoder().decode(base64Token), StandardCharsets.UTF_8)
        val delim = decoded.indexOf(':')
        if (delim < 0) {
            throw BadCredentialsException("Malformed Basic authentication token")
        }
        val username = decoded.substring(0, delim)
        val password = decoded.substring(delim + 1)
        return UsernamePasswordAuthenticationToken(username, password)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <Req : Any> noopListener(): ServerCall.Listener<Req> =
        object : ServerCall.Listener<Req>() {}

    companion object {
        private val AUTHORIZATION_KEY: Metadata.Key<String> =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
    }
}
