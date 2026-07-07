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
        val contains = body["contains"] as? Map<*, *>
        if (match == null && contains == null) {
            return fail("artifacts.shape requires match or contains map")
        }
        val where = body["where"] as? Map<*, *>
        val candidates = outcome.artifacts
            .filter { it.persistKind == kind }
            .filter { artifact -> where?.let { matchesWhere(artifact, it) } ?: true }
        if (candidates.isEmpty()) {
            return fail("no artifact with persistKind=$kind matching where=${where ?: emptyMap<String, Any?>()}")
        }
        val artifact = selectArtifact(candidates, body)
            ?: return fail("expected one artifact with persistKind=$kind but found ${candidates.size}; specify which=first|last or index")
        match?.let {
            val result = checkExactMatches(artifact, kind, it)
            if (!result.passed) return result
        }
        contains?.let {
            val result = checkStringContains(artifact, kind, it)
            if (!result.passed) return result
        }
        return pass()
    }

    private fun checkExactMatches(
        artifact: ArtifactSnapshot,
        kind: String,
        match: Map<*, *>,
    ): CheckResult {
        for ((rawPath, expected) in match) {
            val path = rawPath.toString()
            val actual = resolvePath(artifact, path)
            if (actual != expected) {
                return fail("path $path on persistKind=$kind expected $expected but was $actual")
            }
        }
        return pass()
    }

    private fun checkStringContains(
        artifact: ArtifactSnapshot,
        kind: String,
        contains: Map<*, *>,
    ): CheckResult {
        for ((rawPath, expected) in contains) {
            val path = rawPath.toString()
            val actual = resolvePath(artifact, path)?.toString()
                ?: return fail("path $path on persistKind=$kind was null")
            val expectedFragments = when (expected) {
                is List<*> -> expected.map { it.toString() }
                else -> listOf(expected.toString())
            }
            val missing = expectedFragments.filterNot {
                actual.contains(it, ignoreCase = true)
            }
            if (missing.isNotEmpty()) {
                return fail("path $path on persistKind=$kind did not contain $missing; was $actual")
            }
        }
        return pass()
    }

    private fun selectArtifact(candidates: List<ArtifactSnapshot>, body: Map<String, Any?>): ArtifactSnapshot? {
        body["which"]?.toString()?.let { selector ->
            return when (selector.lowercase()) {
                "first" -> candidates.first()
                "last" -> candidates.last()
                else -> null
            }
        }
        (body["index"] as? Number)?.toInt()?.let { index ->
            return candidates.getOrNull(index)
        }
        return candidates.singleOrNull()
    }

    private fun matchesWhere(artifact: ArtifactSnapshot, where: Map<*, *>): Boolean =
        where.all { (rawPath, expected) ->
            resolvePath(artifact, rawPath.toString()) == expected
        }

    private fun resolvePath(artifact: ArtifactSnapshot, path: String): Any? {
        var current: Any? = artifact.payload
        for (segment in path.split('.')) {
            current = when (current) {
                is Map<*, *> -> current[segment]
                is List<*> -> segment.toIntOrNull()?.let { current.getOrNull(it) } ?: return null
                else -> return null
            }
        }
        return current
    }
}

private fun Any?.asMap(): Map<String, Any?> = this as? Map<String, Any?> ?: emptyMap()

private fun pass() = CheckResult(CheckStatus.PASS)

private fun fail(detail: String) = CheckResult(CheckStatus.FAIL, detail)
