package io.qpointz.mill.ai.service

import io.qpointz.mill.ai.autoconfigure.ConditionalOnAiEnabled
import io.qpointz.mill.service.descriptors.Descriptor
import io.qpointz.mill.service.descriptors.DescriptorTypes
import org.springframework.stereotype.Component

/**
 * Well-known advertisement for the AI v3 chat HTTP surface when {@link ConditionalOnAiEnabled} applies.
 */
@Component
@ConditionalOnAiEnabled
class AiChatServiceDescriptor : Descriptor {

    /**
     * @return {@link DescriptorTypes#SERVICE_TYPE_NAME}
     */
    override fun getTypeName(): String = DescriptorTypes.SERVICE_TYPE_NAME

    /** Logical service id in discovery JSON. */
    val name: String = "ai-chat"
}
