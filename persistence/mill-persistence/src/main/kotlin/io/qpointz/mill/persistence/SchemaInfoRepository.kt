package io.qpointz.mill.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface SchemaInfoRepository : JpaRepository<SchemaInfoEntity, Long>
