package io.qpointz.mill.ai

/**
 * Fixed profile used by the hello-world validation agent.
 */
object HelloWorldAgentProfile {
    val profile = AgentProfile(
        id = "hello-world",
        capabilityIds = HelloWorldCapabilitySet.requiredCapabilityIds,
    )
}
