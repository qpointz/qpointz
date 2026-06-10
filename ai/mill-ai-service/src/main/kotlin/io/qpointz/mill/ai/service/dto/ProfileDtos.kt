package io.qpointz.mill.ai.service.dto

import io.qpointz.mill.ai.profile.AgentProfile

/**
 * JSON view of an [AgentProfile] for discovery APIs (`GET /api/v1/ai/profiles`).
 *
 * @property id Stable profile identifier used in chat create requests (`profileId`).
 * @property capabilityIds Capability ids composed by this profile, sorted for stable output.
 */
data class AgentProfileResponse(
    val id: String,
    val capabilityIds: List<String>,
) {
    companion object {
        /**
         * @param profile domain profile from [io.qpointz.mill.ai.profile.ProfileRegistry]
         */
        fun from(profile: AgentProfile): AgentProfileResponse =
            AgentProfileResponse(
                id = profile.id,
                capabilityIds = profile.capabilityIds.sorted(),
            )
    }
}
