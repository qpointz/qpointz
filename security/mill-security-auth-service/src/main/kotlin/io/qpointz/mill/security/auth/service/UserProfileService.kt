package io.qpointz.mill.security.auth.service

import io.qpointz.mill.persistence.security.jpa.entities.UserProfileRecord
import io.qpointz.mill.persistence.security.jpa.repositories.UserProfileRepository
import io.qpointz.mill.security.auth.dto.UserProfilePatch
import java.time.Instant

/**
 * Service responsible for reading and updating a user's editable profile.
 *
 * Profile rows are created lazily: the first call to [getOrCreate] for a
 * given `userId` inserts an empty [UserProfileRecord] and persists it.
 * Subsequent calls return the existing row without modification.
 *
 * @property userProfileRepository JPA repository for [UserProfileRecord] entities
 */
class UserProfileService(
    private val userProfileRepository: UserProfileRepository,
) {

    /**
     * Returns the profile for [userId], creating an empty one if it does not yet exist.
     *
     * The newly created record has all optional fields set to `null` and [UserProfileRecord.updatedAt]
     * set to the current instant.
     *
     * @param userId canonical `users.id` whose profile to fetch or create
     * @return the existing or newly persisted [UserProfileRecord]
     */
    fun getOrCreate(userId: String): UserProfileRecord {
        return userProfileRepository.findByUserId(userId)
            ?: userProfileRepository.save(
                UserProfileRecord(
                    userId = userId,
                    displayName = null,
                    email = null,
                    theme = null,
                    locale = null,
                    updatedAt = Instant.now(),
                )
            )
    }

    /**
     * Applies a partial update to the profile of [userId].
     *
     * Only non-null fields in [patch] overwrite the stored values; null fields
     * are ignored, leaving the existing stored value intact. [UserProfileRecord.updatedAt]
     * is always refreshed on every call, even if no fields changed.
     *
     * If no profile row exists yet one is created via [getOrCreate] before the patch is applied.
     *
     * @param userId canonical `users.id` whose profile to update
     * @param patch partial values to apply; null fields are ignored
     * @return the updated and persisted [UserProfileRecord]
     */
    fun update(userId: String, patch: UserProfilePatch): UserProfileRecord {
        val record = getOrCreate(userId)
        patch.displayName?.let { record.displayName = it }
        patch.email?.let { record.email = it }
        patch.locale?.let { record.locale = it }
        record.updatedAt = Instant.now()
        return userProfileRepository.save(record)
    }
}
