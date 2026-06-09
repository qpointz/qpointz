package io.qpointz.mill.analysis.queries.web

import io.qpointz.mill.analysis.queries.SavedQuery
import io.qpointz.mill.analysis.queries.web.dto.SavedQueryWireDto

/**
 * Maps domain {@link SavedQuery} records to HTTP wire DTOs.
 */
object SavedQueryWireMapper {

    /**
     * @param query domain saved query
     * @return wire DTO with epoch-millisecond timestamps
     */
    fun toWire(query: SavedQuery): SavedQueryWireDto = SavedQueryWireDto(
        id = query.id,
        name = query.name,
        description = query.description,
        sql = query.sql,
        createdAt = query.createdAt.toEpochMilli(),
        updatedAt = query.updatedAt.toEpochMilli(),
        tags = query.tags.takeIf { it.isNotEmpty() },
    )
}
