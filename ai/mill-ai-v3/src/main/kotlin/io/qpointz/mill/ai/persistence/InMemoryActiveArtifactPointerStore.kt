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

import java.util.concurrent.ConcurrentHashMap

class InMemoryActiveArtifactPointerStore : ActiveArtifactPointerStore {

    private val store = ConcurrentHashMap<String, ActiveArtifactPointer>()

    private fun key(conversationId: String, pointerKey: String) = "$conversationId::$pointerKey"

    override fun upsert(pointer: ActiveArtifactPointer) {
        store[key(pointer.conversationId, pointer.pointerKey)] = pointer
    }

    override fun find(conversationId: String, pointerKey: String): ActiveArtifactPointer? =
        store[key(conversationId, pointerKey)]

    override fun findAll(conversationId: String): List<ActiveArtifactPointer> =
        store.values.filter { it.conversationId == conversationId }
}





