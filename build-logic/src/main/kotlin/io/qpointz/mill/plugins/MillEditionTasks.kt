package io.qpointz.mill.plugins

import org.gradle.api.Project

internal fun registerEditionInfoTasks(project: Project, editions: MillEditionsExtension) {
    project.tasks.register("millListEditions") {
        group = "help"
        description = "Lists configured mill features and editions."
        doLast {
            val features = editions.configuredFeatures()
            val editionDefinitions = editions.configuredEditions()

            val resolvedSelection = editions.resolveSelectionOrError()
            val selectedEdition = resolvedSelection.getOrNull()?.name
            val defaultEdition = editions.defaultEdition

            project.logger.lifecycle("mill editions for ${project.path}")
            project.logger.lifecycle("  default edition: ${defaultEdition ?: "<unset>"}")
            project.logger.lifecycle("  selected edition: ${selectedEdition ?: "<unresolved>"}")

            if (features.isEmpty()) {
                project.logger.lifecycle("  features: <none>")
            } else {
                project.logger.lifecycle("  features:")
                features.sortedBy { it.name }.forEach { feature ->
                    val descriptionSuffix = feature.description?.let { " - $it" } ?: ""
                    val modules = if (feature.modules.isEmpty()) {
                        "<no modules>"
                    } else {
                        feature.modules.joinToString(", ")
                    }
                    project.logger.lifecycle("    - ${feature.name}$descriptionSuffix")
                    project.logger.lifecycle("      modules: $modules")
                }
            }

            if (editionDefinitions.isEmpty()) {
                project.logger.lifecycle("  editions: <none>")
            } else {
                project.logger.lifecycle("  editions:")
                editionDefinitions.sortedBy { it.name }.forEach { edition ->
                    val descriptionSuffix = edition.description?.let { " - $it" } ?: ""
                    val featureNames = if (edition.features.isEmpty()) {
                        "<no features>"
                    } else {
                        edition.features.joinToString(", ")
                    }
                    project.logger.lifecycle("    - ${edition.name}$descriptionSuffix")
                    project.logger.lifecycle("      features: $featureNames")
                }
            }

            resolvedSelection.exceptionOrNull()?.let { error ->
                project.logger.warn("mill edition selection is not currently resolvable: ${error.message}")
            }
        }
    }
}
