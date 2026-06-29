package io.qpointz.mill.ai.scenario

import io.qpointz.mill.ai.runtime.events.routing.DefaultEventRoutingPolicy
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScenarioCaptureRoutingTest {

    @Test
    fun shouldPersistToolEvents_whenExtendedPolicyApplied() {
        val extended = ScenarioCaptureRouting.extendedPolicy(DefaultEventRoutingPolicy.policy)

        assertTrue(extended.ruleFor("tool.call")!!.persistEvent)
        assertTrue(extended.ruleFor("tool.result")!!.persistEvent)
        assertTrue(extended.ruleFor("protocol.final")!!.persistEvent)
    }

    @Test
    fun shouldLeaveDefaultPersistFlags_whenNotOverridden() {
        val extended = ScenarioCaptureRouting.extendedPolicy(DefaultEventRoutingPolicy.policy)

        assertFalse(extended.ruleFor("thinking.delta")!!.persistEvent)
        assertTrue(extended.ruleFor("run.started")!!.persistEvent)
    }
}
