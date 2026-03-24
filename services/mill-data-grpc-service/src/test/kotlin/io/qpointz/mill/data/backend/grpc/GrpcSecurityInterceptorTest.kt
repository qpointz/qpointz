package io.qpointz.mill.data.backend.grpc

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.Status
import io.qpointz.mill.security.authentication.AuthenticationMethods
import io.qpointz.mill.security.authentication.AuthenticationType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Unit tests for [GrpcSecurityInterceptor]: metadata parsing, [AuthenticationManager] delegation,
 * gRPC status on failure, and [SecurityContextHolder] cleanup after the RPC completes.
 */
@ExtendWith(MockitoExtension::class)
class GrpcSecurityInterceptorTest {

    @Mock
    lateinit var authenticationMethods: AuthenticationMethods

    @Mock
    lateinit var authenticationManager: AuthenticationManager

    @Mock
    lateinit var call: ServerCall<Any, Any>

    @Mock
    lateinit var next: ServerCallHandler<Any, Any>

    private lateinit var interceptor: GrpcSecurityInterceptor

    @BeforeEach
    fun setup() {
        interceptor = GrpcSecurityInterceptor(authenticationMethods, authenticationManager)
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun shouldCloseUnauthenticated_whenAuthorizationHeaderMissing() {
        val headers = Metadata()

        interceptor.interceptCall(call, headers, next)

        verify(call).close(
            argThat { s: Status -> s.code == Status.Code.UNAUTHENTICATED },
            any(),
        )
        verify(next, never()).startCall(any(), any())
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun shouldCloseUnauthenticated_whenOnlyBasicAllowedButBearerSent() {
        whenever(authenticationMethods.authenticationTypes).thenReturn(listOf(AuthenticationType.BASIC))
        val headers = Metadata().apply {
            put(AUTH_KEY, "Bearer some-token")
        }

        interceptor.interceptCall(call, headers, next)

        verify(call).close(
            argThat { s: Status -> s.code == Status.Code.UNAUTHENTICATED },
            any(),
        )
        verify(next, never()).startCall(any(), any())
    }

    @Test
    fun shouldCloseUnauthenticated_whenMalformedBasicPayload() {
        whenever(authenticationMethods.authenticationTypes).thenReturn(listOf(AuthenticationType.BASIC))
        val headers = Metadata().apply {
            put(AUTH_KEY, "Basic " + Base64.getEncoder().encodeToString("nocolon".toByteArray(StandardCharsets.UTF_8)))
        }

        interceptor.interceptCall(call, headers, next)

        verify(call).close(
            argThat { s: Status -> s.code == Status.Code.UNAUTHENTICATED },
            any(),
        )
        verify(next, never()).startCall(any(), any())
    }

    @Test
    fun shouldCloseUnauthenticated_whenAuthenticationManagerRejects() {
        whenever(authenticationMethods.authenticationTypes).thenReturn(listOf(AuthenticationType.BASIC))
        whenever(authenticationManager.authenticate(any()))
            .thenThrow(BadCredentialsException("bad password"))
        val headers = basicHeader("alice", "secret")

        interceptor.interceptCall(call, headers, next)

        verify(authenticationManager).authenticate(
            argThat { a ->
                a is UsernamePasswordAuthenticationToken &&
                    a.name == "alice" &&
                    a.credentials == "secret"
            },
        )
        verify(call).close(
            argThat { s: Status -> s.code == Status.Code.UNAUTHENTICATED },
            any(),
        )
        verify(next, never()).startCall(any(), any())
    }

    @Test
    fun shouldStartCallAndRestoreContext_whenBasicAuthenticationSucceeds() {
        whenever(authenticationMethods.authenticationTypes).thenReturn(listOf(AuthenticationType.BASIC))
        val authenticated = UsernamePasswordAuthenticationToken(
            "alice",
            null,
            listOf(SimpleGrantedAuthority("ROLE_USER")),
        )
        whenever(authenticationManager.authenticate(any())).thenReturn(authenticated)
        val headers = basicHeader("alice", "secret")
        val delegateListener = mock<ServerCall.Listener<Any>>()
        whenever(next.startCall(eq(call), eq(headers))).thenReturn(delegateListener)

        val listener = interceptor.interceptCall(call, headers, next)

        assertSame(authenticated, SecurityContextHolder.getContext().authentication)
        listener.onComplete()
        assertNull(SecurityContextHolder.getContext().authentication)
        verify(next).startCall(eq(call), eq(headers))
        verify(call, never()).close(any(), any())
    }

    @Test
    fun shouldRestorePreviousContext_whenCallCompletesAfterSuccess() {
        val previous = UsernamePasswordAuthenticationToken("prev", "pwd", emptyList())
        SecurityContextHolder.getContext().authentication = previous

        whenever(authenticationMethods.authenticationTypes).thenReturn(listOf(AuthenticationType.BASIC))
        val authenticated = UsernamePasswordAuthenticationToken("alice", null, emptyList())
        whenever(authenticationManager.authenticate(any())).thenReturn(authenticated)
        val headers = basicHeader("alice", "secret")
        val delegateListener = mock<ServerCall.Listener<Any>>()
        whenever(next.startCall(eq(call), eq(headers))).thenReturn(delegateListener)

        val listener = interceptor.interceptCall(call, headers, next)
        assertSame(authenticated, SecurityContextHolder.getContext().authentication)

        listener.onComplete()

        assertSame(previous, SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun shouldRestorePreviousContext_whenCallCancelledAfterSuccess() {
        val previous = UsernamePasswordAuthenticationToken("prev", "pwd", emptyList())
        SecurityContextHolder.getContext().authentication = previous

        whenever(authenticationMethods.authenticationTypes).thenReturn(listOf(AuthenticationType.BASIC))
        val authenticated = UsernamePasswordAuthenticationToken("alice", null, emptyList())
        whenever(authenticationManager.authenticate(any())).thenReturn(authenticated)
        val headers = basicHeader("alice", "secret")
        val delegateListener = mock<ServerCall.Listener<Any>>()
        whenever(next.startCall(eq(call), eq(headers))).thenReturn(delegateListener)

        val listener = interceptor.interceptCall(call, headers, next)
        listener.onCancel()

        assertSame(previous, SecurityContextHolder.getContext().authentication)
        verify(delegateListener).onCancel()
    }

    @Test
    fun shouldAuthenticateBearer_whenOauth2MethodEnabled() {
        whenever(authenticationMethods.authenticationTypes).thenReturn(listOf(AuthenticationType.OAUTH2))
        val headers = Metadata().apply { put(AUTH_KEY, "Bearer my-jwt") }
        val authenticated = UsernamePasswordAuthenticationToken("sub", null, emptyList())
        whenever(authenticationManager.authenticate(any())).thenReturn(authenticated)
        val delegateListener = mock<ServerCall.Listener<Any>>()
        whenever(next.startCall(eq(call), eq(headers))).thenReturn(delegateListener)

        val listener = interceptor.interceptCall(call, headers, next)

        verify(authenticationManager).authenticate(
            argThat { a -> a is BearerTokenAuthenticationToken && a.token == "my-jwt" },
        )
        assertSame(authenticated, SecurityContextHolder.getContext().authentication)
        listener.onComplete()
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun shouldCloseUnauthenticated_whenBearerTokenEmpty() {
        whenever(authenticationMethods.authenticationTypes).thenReturn(listOf(AuthenticationType.OAUTH2))
        val headers = Metadata().apply { put(AUTH_KEY, "Bearer   ") }

        interceptor.interceptCall(call, headers, next)

        verify(call).close(
            argThat { s: Status -> s.code == Status.Code.UNAUTHENTICATED },
            any(),
        )
        verify(authenticationManager, never()).authenticate(any())
    }

    private fun basicHeader(user: String, password: String): Metadata {
        val token = Base64.getEncoder().encodeToString("$user:$password".toByteArray(StandardCharsets.UTF_8))
        return Metadata().apply { put(AUTH_KEY, "Basic $token") }
    }

    companion object {
        private val AUTH_KEY: Metadata.Key<String> =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
    }
}
