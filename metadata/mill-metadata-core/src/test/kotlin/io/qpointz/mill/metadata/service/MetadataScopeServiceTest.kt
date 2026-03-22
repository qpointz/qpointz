package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MetadataScopeServiceTest {

    @Mock
    private lateinit var repo: MetadataScopeRepository

    private lateinit var service: MetadataScopeService

    private val globalScope = MetadataScope(
        scopeId = MetadataUrns.SCOPE_GLOBAL,
        displayName = "Global",
        ownerId = null,
        createdAt = Instant.now()
    )

    @BeforeEach
    fun setUp() {
        service = MetadataScopeService(repo)
    }

    @Test
    fun shouldReturnGlobalScope_whenRequested() {
        whenever(repo.findById(MetadataUrns.SCOPE_GLOBAL)).thenReturn(Optional.of(globalScope))
        val result = service.globalScope()
        assertEquals(MetadataUrns.SCOPE_GLOBAL, result.scopeId)
    }

    @Test
    fun shouldThrow_whenGlobalScopeAbsent() {
        whenever(repo.findById(MetadataUrns.SCOPE_GLOBAL)).thenReturn(Optional.empty())
        assertThrows<IllegalStateException> {
            service.globalScope()
        }
    }

    @Test
    fun shouldReturnAllScopes_whenFindAllCalled() {
        val scopes = listOf(globalScope)
        whenever(repo.findAll()).thenReturn(scopes)
        val result = service.findAll()
        assertEquals(1, result.size)
        assertEquals(MetadataUrns.SCOPE_GLOBAL, result[0].scopeId)
    }

    @Test
    fun shouldReturnScope_whenFindByKeyFindsIt() {
        whenever(repo.findById(MetadataUrns.SCOPE_GLOBAL)).thenReturn(Optional.of(globalScope))
        val result = service.findByKey(MetadataUrns.SCOPE_GLOBAL)
        assertEquals(MetadataUrns.SCOPE_GLOBAL, result.get().scopeId)
    }

    @Test
    fun shouldCreate_whenScopeDoesNotExist() {
        val userScope = MetadataUrns.scopeUser("alice")
        whenever(repo.existsById(userScope)).thenReturn(false)
        whenever(repo.save(any<MetadataScope>())).thenAnswer { it.arguments[0] as MetadataScope }

        val result = service.create(userScope, "Alice's scope", "alice")

        assertEquals(userScope, result.scopeId)
        assertEquals("Alice's scope", result.displayName)
        verify(repo).save(any<MetadataScope>())
    }

    @Test
    fun shouldThrow_whenCreatingDuplicateScope() {
        val userScope = MetadataUrns.scopeUser("alice")
        whenever(repo.existsById(userScope)).thenReturn(true)

        assertThrows<IllegalArgumentException> {
            service.create(userScope, null, null)
        }
        verify(repo, never()).save(any<MetadataScope>())
    }

    @Test
    fun shouldDelete_whenScopeIsNotGlobal() {
        val userScope = MetadataUrns.scopeUser("alice")
        service.delete(userScope)
        verify(repo).deleteById(userScope)
    }

    @Test
    fun shouldThrow_whenDeletingGlobalScope() {
        assertThrows<IllegalArgumentException> {
            service.delete(MetadataUrns.SCOPE_GLOBAL)
        }
        verify(repo, never()).deleteById(any())
    }
}
