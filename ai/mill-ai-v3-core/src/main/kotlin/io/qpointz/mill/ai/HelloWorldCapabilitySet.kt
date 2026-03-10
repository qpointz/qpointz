package io.qpointz.mill.ai

/**
 * Canonical capability set expected by the hello-world milestone.
 */
object HelloWorldCapabilitySet {
    val requiredCapabilityIds: Set<String> = setOf(
        "conversation",
        "demo",
    )
}
