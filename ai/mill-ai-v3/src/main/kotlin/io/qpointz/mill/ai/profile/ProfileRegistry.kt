package io.qpointz.mill.ai.profile

/**
 * Registry that maps a stable [AgentProfile.id] back to a live [AgentProfile].
 *
 * Used at runtime rehydration to resolve the profile a chat was originally created with,
 * without re-inferring it from transcript or context rules.
 */
interface ProfileRegistry {
    fun resolve(profileId: String): AgentProfile?
}

/**
 * Map-backed [ProfileRegistry]. Profiles are registered at construction time.
 *
 * Usage:
 * ```
 * val registry = MapProfileRegistry(
 *     HelloWorldAgentProfile.profile,
 *     SchemaExplorationAgentProfile.profile,
 * )
 * ```
 */
class MapProfileRegistry(profiles: Iterable<AgentProfile>) : ProfileRegistry {

    private val profilesById: Map<String, AgentProfile> =
        profiles.associateBy { it.id }

    constructor(vararg profiles: AgentProfile) : this(profiles.asIterable())

    override fun resolve(profileId: String): AgentProfile? = profilesById[profileId]

    val knownIds: Set<String> get() = profilesById.keys
}

/**
 * Default [ProfileRegistry] containing all profiles known at compile time.
 *
 * Intended for use before a dynamic/configurable registry is introduced.
 * Replace with [MapProfileRegistry] or a Spring-managed registry when runtime
 * profile selection becomes configurable.
 */
object DefaultProfileRegistry : ProfileRegistry {

    private val profiles = listOf(
        HelloWorldAgentProfile.profile,
        SchemaExplorationAgentProfile.profile,
        SchemaAuthoringAgentProfile.profile,
    ).associateBy { it.id }

    override fun resolve(profileId: String): AgentProfile? = profiles[profileId]

    val knownIds: Set<String> get() = profiles.keys
}
