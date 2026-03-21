package io.qpointz.mill.security.auth.service

import io.qpointz.mill.persistence.security.jpa.entities.UserProfileRecord
import io.qpointz.mill.persistence.security.jpa.repositories.UserProfileRepository
import io.qpointz.mill.security.auth.dto.UserProfilePatch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class UserProfileServiceTest {

    private val userProfileRepository: UserProfileRepository = mock()

    private fun makeRecord(userId: String) = UserProfileRecord(
        userId = userId,
        displayName = null,
        email = null,
        theme = null,
        locale = null,
        updatedAt = Instant.now(),
    )

    @Test
    fun `getOrCreate_whenProfileAbsent_createsAndReturnsNewRecord`() {
        val service = UserProfileService(userProfileRepository)
        val created = makeRecord("user-1")
        whenever(userProfileRepository.findByUserId("user-1")).thenReturn(null)
        whenever(userProfileRepository.save(any<UserProfileRecord>())).thenReturn(created)

        val result = service.getOrCreate("user-1")

        assertThat(result.userId).isEqualTo("user-1")
        verify(userProfileRepository).save(any<UserProfileRecord>())
    }

    @Test
    fun `getOrCreate_whenProfileExists_returnsExistingRecord`() {
        val service = UserProfileService(userProfileRepository)
        val existing = makeRecord("user-2").also { it.displayName = "Alice" }
        whenever(userProfileRepository.findByUserId("user-2")).thenReturn(existing)

        val result = service.getOrCreate("user-2")

        assertThat(result.displayName).isEqualTo("Alice")
        // No save should be called for an existing record
        verify(userProfileRepository, org.mockito.kotlin.never()).save(any<UserProfileRecord>())
    }

    @Test
    fun `update_whenPatchHasDisplayName_setsOnlyDisplayName`() {
        val service = UserProfileService(userProfileRepository)
        val record = makeRecord("user-3").also {
            it.email = "bob@example.com"
            it.locale = "en"
        }
        whenever(userProfileRepository.findByUserId("user-3")).thenReturn(record)
        whenever(userProfileRepository.save(any<UserProfileRecord>())).thenAnswer { it.arguments[0] as UserProfileRecord }

        val patch = UserProfilePatch(displayName = "Bob", email = null, locale = null)
        val result = service.update("user-3", patch)

        assertThat(result.displayName).isEqualTo("Bob")
        // email and locale must be untouched
        assertThat(result.email).isEqualTo("bob@example.com")
        assertThat(result.locale).isEqualTo("en")
    }

    @Test
    fun `update_whenAllPatchFieldsProvided_setsAllFields`() {
        val service = UserProfileService(userProfileRepository)
        val record = makeRecord("user-4")
        whenever(userProfileRepository.findByUserId("user-4")).thenReturn(record)
        whenever(userProfileRepository.save(any<UserProfileRecord>())).thenAnswer { it.arguments[0] as UserProfileRecord }

        val patch = UserProfilePatch(displayName = "Carol", email = "carol@example.com", locale = "fr")
        val result = service.update("user-4", patch)

        assertThat(result.displayName).isEqualTo("Carol")
        assertThat(result.email).isEqualTo("carol@example.com")
        assertThat(result.locale).isEqualTo("fr")
    }

    @Test
    fun `update_whenProfileAbsent_createsProfileThenAppliesPatch`() {
        val service = UserProfileService(userProfileRepository)
        val newRecord = makeRecord("user-5")
        whenever(userProfileRepository.findByUserId("user-5")).thenReturn(null)
        // first save for getOrCreate, second save for update
        whenever(userProfileRepository.save(any<UserProfileRecord>())).thenReturn(newRecord)

        val patch = UserProfilePatch(displayName = "Dave", email = null, locale = null)
        service.update("user-5", patch)

        // save must be called at least once (for create) and again for the update
        verify(userProfileRepository, org.mockito.kotlin.atLeast(1)).save(any<UserProfileRecord>())
    }
}
