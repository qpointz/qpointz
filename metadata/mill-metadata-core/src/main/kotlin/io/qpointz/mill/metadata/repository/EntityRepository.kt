package io.qpointz.mill.metadata.repository

/**
 * Full entity persistence adapter: [EntityReadSide] + [EntityWriteSide].
 *
 * JPA and in-memory implementations implement this type. Prefer the narrower read or write
 * interface in new code when only one responsibility is required.
 */
interface EntityRepository : EntityReadSide, EntityWriteSide
