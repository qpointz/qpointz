package io.qpointz.mill.plugins

import org.gradle.api.Project
import org.gradle.api.tasks.Sync

internal fun configureEditionPackaging(project: Project, editions: MillEditionsExtension) {
    val selection = editions.resolveEditionSelection()
    val selectedEdition = selection.name

    selection.modules.forEach { modulePath ->
        project.dependencies.add("implementation", project.project(modulePath))
    }

    project.tasks.matching { task ->
        task.name == "bootDist" ||
            task.name == "installBootDist" ||
            task.name == "distZip" ||
            task.name == "distTar" ||
            task.name == "installDist"
    }.configureEach {
        inputs.property("mill.edition", selectedEdition)
    }

    project.tasks.matching { task ->
        task.name == "installBootDist"
    }.configureEach {
        if (this is Sync) {
            into(project.layout.buildDirectory.dir("install/${project.name}-boot-$selectedEdition"))
        }
    }

    project.tasks.matching { task ->
        task.name == "installDist"
    }.configureEach {
        if (this is Sync) {
            into(project.layout.buildDirectory.dir("install/${project.name}-$selectedEdition"))
        }
    }
}
