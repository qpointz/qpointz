package io.qpointz.mill.metadata.service

import io.qpointz.mill.metadata.domain.MetadataScope
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.repository.MetadataScopeRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class MetadataScopeServiceTest {

  private val repo: MetadataScopeRepository = mock()
  private val service = MetadataScopeService(repo)

  @Test
  fun shouldCreateChatScope_whenMissing() {
    val chatId = "abc-123"
    val scopeUrn = MetadataUrns.scopeChat(chatId)
    whenever(repo.findByRes(scopeUrn)).thenReturn(null)
    whenever(repo.save(any())).thenAnswer { it.arguments[0] as MetadataScope }

    val created = service.ensureChatScope(chatId, "Passenger docs", "user-1")

    assertThat(created.res).isEqualTo(scopeUrn)
    assertThat(created.scopeType).isEqualTo("CHAT")
    assertThat(created.referenceId).isEqualTo(chatId)
    assertThat(created.displayName).isEqualTo("Chat Passenger docs")
    assertThat(created.ownerId).isEqualTo("user-1")
    assertThat(created.visibility).isEqualTo("PRIVATE")
  }

  @Test
  fun shouldUpdateDisplayName_whenChatRenamed() {
    val chatId = "abc-123"
    val scopeUrn = MetadataUrns.scopeChat(chatId)
    val now = Instant.now()
    val existing = MetadataScope(
      res = scopeUrn,
      scopeType = "CHAT",
      referenceId = chatId,
      displayName = "Chat Old",
      ownerId = "user-1",
      visibility = "PRIVATE",
      uuid = "uuid-1",
      createdAt = now,
      createdBy = null,
      lastModifiedAt = now,
      lastModifiedBy = null,
    )
    whenever(repo.findByRes(scopeUrn)).thenReturn(existing)
    whenever(repo.save(any())).thenAnswer { it.arguments[0] as MetadataScope }

    val updated = service.ensureChatScope(chatId, "Renamed", "user-1")

    assertThat(updated.displayName).isEqualTo("Chat Renamed")
    verify(repo).save(any())
  }

  @Test
  fun shouldNotSave_whenChatScopeUnchanged() {
    val chatId = "abc-123"
    val scopeUrn = MetadataUrns.scopeChat(chatId)
    val now = Instant.now()
    val existing = MetadataScope(
      res = scopeUrn,
      scopeType = "CHAT",
      referenceId = chatId,
      displayName = "Chat Same",
      ownerId = "user-1",
      visibility = "PRIVATE",
      uuid = "uuid-1",
      createdAt = now,
      createdBy = null,
      lastModifiedAt = now,
      lastModifiedBy = null,
    )
    whenever(repo.findByRes(scopeUrn)).thenReturn(existing)

    val result = service.ensureChatScope(chatId, "Same", "user-1")

    assertThat(result).isSameAs(existing)
    verify(repo, never()).save(any())
  }
}
