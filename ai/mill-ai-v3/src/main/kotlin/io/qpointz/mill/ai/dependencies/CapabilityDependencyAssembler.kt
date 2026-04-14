package io.qpointz.mill.ai.dependencies

import io.qpointz.mill.ai.core.capability.CapabilityDependencyContainer
import io.qpointz.mill.ai.persistence.ChatMetadata
import io.qpointz.mill.ai.profile.AgentProfile

/**
 * Produces [CapabilityDependencyContainer] for a chat turn from the resolved profile and
 * persisted [ChatMetadata]. Implementations may ignore [metadata] when dependencies are purely
 * bean-driven; the parameter is reserved for future context-aware wiring.
 *
 * Thread-safety: implementations used by [io.qpointz.mill.ai.autoconfigure.chat.LangChain4jChatRuntime] (autoconfigure)
 * must be safe for concurrent [assemble] calls unless the runtime contract is narrowed later.
 */
fun interface CapabilityDependencyAssembler {

    /**
     * Assembles dependency bindings for [profile] for the current turn.
     *
     * @param profile Agent profile for the chat (from [io.qpointz.mill.ai.profile.ProfileRegistry]).
     * @param metadata Persisted chat row driving rehydration; may inform future assembler strategies.
     * @return Container to assign to [io.qpointz.mill.ai.runtime.AgentContext.capabilityDependencies]
     * (replaces any empty rehydration container).
     */
    fun assemble(profile: AgentProfile, metadata: ChatMetadata): CapabilityDependencyContainer
}

/**
 * [CapabilityDependencyAssembler] that leaves [CapabilityDependencyContainer] empty.
 *
 * Appropriate for profiles that only use capabilities without external agent-owned dependencies
 * (for example hello-world), or for tests that supply dependencies out-of-band.
 */
class EmptyCapabilityDependencyAssembler : CapabilityDependencyAssembler {
    override fun assemble(profile: AgentProfile, metadata: ChatMetadata): CapabilityDependencyContainer =
        CapabilityDependencyContainer.empty()
}
