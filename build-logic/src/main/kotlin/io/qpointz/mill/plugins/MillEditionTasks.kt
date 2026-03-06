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
                    val effectiveFeatureNames = runCatching {
                        editions.editionFeatureMatrix()[edition.name].orEmpty()
                    }.getOrElse { emptySet() }
                    val effective = if (effectiveFeatureNames.isEmpty()) {
                        "<no features>"
                    } else {
                        effectiveFeatureNames.joinToString(", ")
                    }
                    val inherited = if (edition.inheritsFrom.isEmpty()) {
                        "<none>"
                    } else {
                        edition.inheritsFrom.joinToString(", ")
                    }
                    project.logger.lifecycle("    - ${edition.name}$descriptionSuffix")
                    project.logger.lifecycle("      inherits: $inherited")
                    project.logger.lifecycle("      features(local): $featureNames")
                    project.logger.lifecycle("      features(effective): $effective")
                }
            }

            resolvedSelection.exceptionOrNull()?.let { error ->
                project.logger.warn("mill edition selection is not currently resolvable: ${error.message}")
            }
        }
    }

    project.tasks.register("millEditionMatrix") {
        group = "help"
        description = "Prints edition matrix (edition -> effective features)."
        doLast {
            val matrix = editions.editionFeatureMatrix()
            if (matrix.isEmpty()) {
                project.logger.lifecycle("mill edition matrix for ${project.path}: <none>")
                return@doLast
            }

            project.logger.lifecycle("mill edition matrix for ${project.path}")
            matrix.toSortedMap().forEach { (editionName, features) ->
                val featureList = if (features.isEmpty()) {
                    "<no features>"
                } else {
                    features.joinToString(", ")
                }
                project.logger.lifecycle("  - $editionName -> $featureList")
            }
        }
    }
}
