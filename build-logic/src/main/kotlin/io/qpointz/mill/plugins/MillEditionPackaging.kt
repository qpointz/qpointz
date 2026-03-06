package io.qpointz.mill.plugins

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.Sync

internal fun configureEditionPackaging(project: Project, editions: MillEditionsExtension) {
    val selection = editions.resolveEditionSelection()
    val selectedEdition = selection.name
    val editionLineage = editions.resolveEditionLineage(selectedEdition)
    val editionContentDirs = editionLineage.map { editionName ->
        editionName to project.layout.projectDirectory.dir("src/main/editions/$editionName")
    }

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
            configureEditionContentSync(
                project = project,
                selectedEdition = selectedEdition,
                editionContentDirs = editionContentDirs
            )
        }
    }

    project.tasks.matching { task ->
        task.name == "installDist"
    }.configureEach {
        if (this is Sync) {
            into(project.layout.buildDirectory.dir("install/${project.name}-$selectedEdition"))
            configureEditionContentSync(
                project = project,
                selectedEdition = selectedEdition,
                editionContentDirs = editionContentDirs
            )
        }
    }

    project.tasks.matching { task ->
        task.name == "distZip" || task.name == "distTar"
    }.configureEach {
        if (this is AbstractArchiveTask) {
            archiveBaseName.set("${project.name}-$selectedEdition")
        }
    }
}

private fun Sync.configureEditionContentSync(
    project: Project,
    selectedEdition: String,
    editionContentDirs: List<Pair<String, org.gradle.api.file.Directory>>
) {
    inputs.property("mill.edition.lineage", editionContentDirs.map { it.first })

    doFirst {
        project.logger.lifecycle(
            "[$name] edition '$selectedEdition': checking edition content for lineage ${editionContentDirs.map { it.first }}"
        )
        editionContentDirs.forEach { (editionName, editionDir) ->
            val editionDirFile = editionDir.asFile
            if (editionDirFile.exists() && editionDirFile.isDirectory) {
                project.logger.lifecycle(
                    "[$name] edition '$editionName': syncing ${editionDirFile.absolutePath} into install root"
                )
            } else {
                project.logger.lifecycle(
                    "[$name] edition '$editionName': no directory found at ${editionDirFile.absolutePath}, skipping"
                )
            }
        }
    }

    doLast {
        editionContentDirs.forEach { (_, editionDir) ->
            val editionDirFile = editionDir.asFile
            if (editionDirFile.exists() && editionDirFile.isDirectory) {
                project.copy {
                    from(editionDirFile)
                    into(destinationDir)
                }
            }
        }
    }
}
