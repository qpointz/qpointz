package io.qpointz.mill.events.configuration

import io.qpointz.mill.events.model.EventPayload

/** Simple payload for integration tests. */
data class TestPayload(val message: String) : EventPayload
