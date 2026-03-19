package io.qpointz.mill.ai.persistence

import io.qpointz.mill.ai.core.capability.*
import io.qpointz.mill.ai.core.prompt.*
import io.qpointz.mill.ai.core.protocol.*
import io.qpointz.mill.ai.core.tool.*
import io.qpointz.mill.ai.memory.*
import io.qpointz.mill.ai.persistence.*
import io.qpointz.mill.ai.profile.*
import io.qpointz.mill.ai.runtime.*
import io.qpointz.mill.ai.runtime.events.*
import io.qpointz.mill.ai.runtime.events.routing.*

import java.util.concurrent.CopyOnWriteArrayList

class InMemoryRunEventStore : RunEventStore {

    private val records = CopyOnWriteArrayList<RunEventRecord>()

    override fun save(record: RunEventRecord) {
        records.add(record)
    }

    override fun findByRun(runId: String): List<RunEventRecord> =
        records.filter { it.runId == runId }
}





