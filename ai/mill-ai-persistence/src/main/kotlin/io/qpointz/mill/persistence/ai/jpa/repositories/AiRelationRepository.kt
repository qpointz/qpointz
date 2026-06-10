package io.qpointz.mill.persistence.ai.jpa.repositories

import io.qpointz.mill.persistence.RelationRecord
import io.qpointz.mill.persistence.RelationRecordRepository

/**
 * AI v3 specialization of the shared relation repository.
 * Used by [JpaConversationStore] to manage turn-to-artifact links.
 */
interface AiRelationRepository : RelationRecordRepository
