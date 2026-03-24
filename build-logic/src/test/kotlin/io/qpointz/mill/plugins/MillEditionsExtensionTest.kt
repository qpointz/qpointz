package io.qpointz.mill.plugins

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MillEditionsExtensionTest {

    @Test
    fun shouldFail_whenInheritedEditionIsUnknown() {
        val project = ProjectBuilder.builder().withName("app").build()
        val editions = MillEditionsExtension(project)

        editions.feature("A")
        editions.edition("next1") {
            from("minimal")
            feature("A")
        }
        editions.defaultEdition = "next1"

        val ex = assertThrows<IllegalStateException> {
            editions.resolveEditionSelection()
        }
        assertTrue(ex.message!!.contains("inherits unknown edition 'minimal'"))
    }

    @Test
    fun shouldFail_whenEditionInheritanceHasCycle() {
        val project = ProjectBuilder.builder().withName("app").build()
        val editions = MillEditionsExtension(project)

        editions.feature("A")
        editions.feature("B")
        editions.edition("minimal") {
            from("next1")
            feature("A")
        }
        editions.edition("next1") {
            from("minimal")
            feature("B")
        }
        editions.defaultEdition = "minimal"

        val ex = assertThrows<IllegalStateException> {
            editions.resolveEditionSelection()
        }
        assertTrue(ex.message!!.contains("Edition inheritance cycle detected"))
    }

    @Test
    fun shouldResolveInheritedFeatureChain() {
        val project = ProjectBuilder.builder().withName("app").build()
        val editions = MillEditionsExtension(project)

        editions.feature("A")
        editions.feature("B")
        editions.feature("C")
        editions.feature("D")
        editions.feature("E")

        editions.edition("minimal") {
            features("A", "B", "C")
        }
        editions.edition("next1") {
            from("minimal")
            feature("D")
        }
        editions.edition("next2") {
            from("next1")
            feature("E")
        }
        editions.defaultEdition = "next2"

        val selection = editions.resolveEditionSelection()
        assertEquals("next2", selection.name)
        assertEquals(setOf("A", "B", "C", "D", "E"), selection.features)
    }

    @Test
    fun shouldFail_whenGrpcV1AndGrpcV2BothEnabled() {
        val project = ProjectBuilder.builder().withName("app").build()
        val editions = MillEditionsExtension(project)

        editions.feature("grpc-v1")
        editions.feature("grpc-v2")
        editions.edition("bad") {
            features("grpc-v1", "grpc-v2")
        }
        editions.defaultEdition = "bad"

        val ex = assertThrows<IllegalStateException> {
            editions.resolveEditionSelection()
        }
        assertTrue(ex.message!!.contains("cannot enable both grpc-v1 and grpc-v2"))
    }

    @Test
    fun shouldResolveEditionLineageInInheritanceOrder() {
        val project = ProjectBuilder.builder().withName("app").build()
        val editions = MillEditionsExtension(project)

        editions.feature("A")
        editions.feature("B")
        editions.feature("C")

        editions.edition("base") {
            feature("A")
        }
        editions.edition("middle") {
            from("base")
            feature("B")
        }
        editions.edition("top") {
            from("middle")
            feature("C")
        }

        assertEquals(listOf("base", "middle", "top"), editions.resolveEditionLineage("top"))
    }
}
