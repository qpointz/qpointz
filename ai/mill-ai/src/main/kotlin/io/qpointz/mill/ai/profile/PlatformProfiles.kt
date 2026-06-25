package io.qpointz.mill.ai.profile

/**
 * Classpath-backed platform [ProfileRegistry] for tests and CLI harnesses.
 *
 * Production hosts load the same YAML via `mill.ai.profiles.seed.resources`.
 */
object PlatformProfiles {

    private const val PLATFORM_SEED = "classpath:profiles/platform-agent-profiles.yaml"

    private val delegate: ResourceProfileRegistry by lazy {
        ResourceProfileRegistry.load(
            PlatformProfiles::class.java.classLoader,
            listOf(PLATFORM_SEED),
        )
    }

    /** @return registry loaded from [PLATFORM_SEED] */
    fun registry(): ProfileRegistry = delegate

    /**
     * @param profileId stable profile id from platform YAML
     * @return resolved profile
     * @throws IllegalArgumentException when id is missing
     */
    fun require(profileId: String): AgentProfile =
        delegate.resolve(profileId) ?: error("Unknown platform profile: $profileId")
}
