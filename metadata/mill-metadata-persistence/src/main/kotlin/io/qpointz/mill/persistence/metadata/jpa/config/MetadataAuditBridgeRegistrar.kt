package io.qpointz.mill.persistence.metadata.jpa.config

import io.qpointz.mill.metadata.repository.MetadataAuditRepository
import io.qpointz.mill.persistence.metadata.jpa.listeners.MetadataAuditBridge
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Connects JPA entity listeners to [MetadataAuditRepository] after the context is wired.
 *
 * @param metadataAuditRepository append-only audit adapter
 */
@Component
@ConditionalOnProperty(prefix = "mill.metadata.repository", name = ["type"], havingValue = "jpa")
class MetadataAuditBridgeRegistrar(
    private val metadataAuditRepository: MetadataAuditRepository
) : InitializingBean {
    override fun afterPropertiesSet() {
        MetadataAuditBridge.recorder = { metadataAuditRepository.record(it) }
    }
}
