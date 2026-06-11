package io.qpointz.mill.ai.test.scenario.v3.checks

import io.qpointz.mill.ai.test.scenario.v3.ArtifactSnapshot
import io.qpointz.mill.ai.test.scenario.v3.CheckResult
import io.qpointz.mill.ai.test.scenario.v3.CheckStatus
import io.qpointz.mill.ai.test.scenario.v3.TurnCheck
import io.qpointz.mill.ai.test.scenario.v3.TurnOutcome

/** Asserts JSON path values on a persisted artefact payload. */
class ArtifactsShapeTurnCheck : TurnCheck {
    override val type: String = "artifacts.shape"

    override fun run(outcome: TurnOutcome, spec: Any?): CheckResult {
        val body = spec.asMap()
        val kind = body["persistKind"]?.toString()
            ?: return fail("artifacts.shape requires persistKind")
        val match = body["match"] as? Map<*, *>
            ?: return fail("artifacts.shape requires match map")
        val candidates = outcome.artifacts.filter { it.persistKind == kind }
        if (candidates.isEmpty()) {
            return fail("no artifact with persistKind=$kind")
        }
        val artifact = candidates.singleOrNull()
            ?: return fail("expected one artifact with persistKind=$kind but found ${candidates.size}")
        for ((rawPath, expected) in match) {
            val path = rawPath.toString()
            val actual = resolvePath(artifact, path)
            if (actual != expected) {
                return fail("path $path on persistKind=$kind expected $expected but was $actual")
            }
        }
        return pass()
    }

    private fun resolvePath(artifact: ArtifactSnapshot, path: String): Any? {
        var current: Any? = artifact.payload
        for (segment in path.split('.')) {
            current = when (current) {
                is Map<*, *> -> current[segment]
                else -> return null
            }
        }
        return current
    }
}

private fun Any?.asMap(): Map<String, Any?> = this as? Map<String, Any?> ?: emptyMap()

private fun pass() = CheckResult(CheckStatus.PASS)

private fun fail(detail: String) = CheckResult(CheckStatus.FAIL, detail)
