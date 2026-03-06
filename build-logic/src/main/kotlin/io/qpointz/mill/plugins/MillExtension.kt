package io.qpointz.mill.plugins

import org.gradle.api.Project

open class MillExtension(project: Project) {
    var description: String = ""
    var publishArtifacts: Boolean = true
    val editions = MillEditionsExtension(project)

    var publishArtefact: Boolean
        get() = publishArtifacts
        set(value) {
            publishArtifacts = value
        }

    fun editions(configure: MillEditionsExtension.() -> Unit) {
        editions.configure()
    }
}
