package io.qpointz.mill.events.testkit

import io.qpointz.mill.events.model.EventPayload

/**
 * Simple payload used in unit tests.
 *
 * @property message arbitrary test data
 */
data class TestEventPayload(val message: String) : EventPayload
