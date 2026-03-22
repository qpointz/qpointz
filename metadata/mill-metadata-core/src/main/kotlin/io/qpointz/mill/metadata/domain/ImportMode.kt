package io.qpointz.mill.metadata.domain

/**
 * Import/export mode for bulk metadata operations.
 *
 * - [MERGE] — preserve entities not mentioned in the input; only upsert provided entities.
 * - [REPLACE] — delete all entities in the target scope before importing the new set.
 */
enum class ImportMode { MERGE, REPLACE }
