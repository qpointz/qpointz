package io.qpointz.mill.persistence.ai.jpa

/**
 * AI v3 URN construction rules.
 * Format: `urn:<type-path>:<id>`
 */
object AiV3Urns {
    const val TYPE_TURN = "agent/conversation-turn"
    const val TYPE_ARTIFACT = "agent/artifact"
    const val RELATION_TURN_TO_ARTIFACT = "turn-to-artifact"

    fun turnUrn(turnId: String) = "urn:$TYPE_TURN:$turnId"
    fun artifactUrn(artifactId: String) = "urn:$TYPE_ARTIFACT:$artifactId"
}
