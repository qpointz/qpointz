package io.qpointz.mill.plugins

import org.gradle.api.Project
import org.gradle.api.provider.Provider

open class MillEditionsExtension(private val project: Project) {
    private val featureModules = linkedMapOf<String, LinkedHashSet<String>>()
    private val editionFeatures = linkedMapOf<String, LinkedHashSet<String>>()
    private val featureDescriptions = linkedMapOf<String, String>()
    private val editionDescriptions = linkedMapOf<String, String>()

    var defaultEdition: String? = null

    val selectedEdition: Provider<String> = project.providers.provider {
        resolveEditionSelection().name
    }

    val activeFeatures: Provider<Set<String>> = project.providers.provider {
        resolveEditionSelection().features.toSet()
    }

    fun feature(name: String, vararg projectModules: String) {
        require(name.isNotBlank()) { "Feature name must not be blank" }
        val modules = featureModules.getOrPut(name) { linkedSetOf() }
        projectModules.forEach {
            require(it.isNotBlank()) { "Feature module path must not be blank (feature=$name)" }
            modules.add(it)
        }
    }

    fun feature(name: String, configure: FeatureDeclaration.() -> Unit) {
        val declaration = FeatureDeclaration().apply(configure)
        feature(name, *declaration.modules.toTypedArray())
        declaration.description
            ?.takeIf { it.isNotBlank() }
            ?.let { featureDescriptions[name] = it }
    }

    fun edition(name: String, vararg features: String) {
        require(name.isNotBlank()) { "Edition name must not be blank" }
        val editionFeatureSet = editionFeatures.getOrPut(name) { linkedSetOf() }
        features.forEach {
            require(it.isNotBlank()) { "Feature name must not be blank (edition=$name)" }
            editionFeatureSet.add(it)
        }
    }

    fun edition(name: String, configure: EditionDeclaration.() -> Unit) {
        val declaration = EditionDeclaration().apply(configure)
        edition(name, *declaration.features.toTypedArray())
        declaration.description
            ?.takeIf { it.isNotBlank() }
            ?.let { editionDescriptions[name] = it }
    }

    fun isActive(featureName: String): Provider<Boolean> {
        require(featureName.isNotBlank()) { "Feature name must not be blank" }
        return activeFeatures.map { it.contains(featureName) }
    }

    internal fun isConfigured(): Boolean {
        return editionFeatures.isNotEmpty() || defaultEdition != null
    }

    internal fun configuredFeatures(): List<FeatureDefinition> {
        return featureModules.keys
            .union(featureDescriptions.keys)
            .map { name ->
                FeatureDefinition(
                    name = name,
                    description = featureDescriptions[name],
                    modules = featureModules[name].orEmpty().toSet()
                )
            }
    }

    internal fun configuredEditions(): List<EditionDefinition> {
        return editionFeatures.keys
            .union(editionDescriptions.keys)
            .map { name ->
                EditionDefinition(
                    name = name,
                    description = editionDescriptions[name],
                    features = editionFeatures[name].orEmpty().toSet()
                )
            }
    }

    internal fun resolveSelectionOrError(): Result<EditionSelection> {
        return runCatching { resolveEditionSelection() }
    }

    internal fun resolveEditionSelection(): EditionSelection {
        if (editionFeatures.isEmpty()) {
            error("Editions are not configured. Declare editions under mill { editions { ... } }.")
        }

        val selectedEditionName = (
            project.findProperty("edition")
                ?.toString()
                ?.takeIf { it.isNotBlank() }
                ?: defaultEdition
            )
            ?: error(
                "No edition selected for ${project.path}. Set -Pedition=<name> or define mill.editions.defaultEdition."
            )

        val features = editionFeatures[selectedEditionName]
            ?: error(
                "Unknown edition '$selectedEditionName' for ${project.path}. " +
                    "Allowed values: ${editionFeatures.keys.joinToString(", ")}"
            )

        val missingFeatures = features.filterNot { featureModules.containsKey(it) }
        if (missingFeatures.isNotEmpty()) {
            error(
                "Edition '$selectedEditionName' references undefined feature(s): " +
                    "${missingFeatures.joinToString(", ")}. " +
                    "Declare each with feature(\"name\", ...)."
            )
        }

        val modules = linkedSetOf<String>()
        features.forEach { feature ->
            modules.addAll(featureModules[feature].orEmpty())
        }

        return EditionSelection(
            name = selectedEditionName,
            features = features.toSet(),
            modules = modules.toSet()
        )
    }
}

open class FeatureDeclaration {
    var description: String? = null
    internal val modules = linkedSetOf<String>()

    fun module(projectModule: String) {
        require(projectModule.isNotBlank()) { "Feature module path must not be blank" }
        modules.add(projectModule)
    }

    fun modules(vararg projectModules: String) {
        projectModules.forEach(::module)
    }
}

open class EditionDeclaration {
    var description: String? = null
    internal val features = linkedSetOf<String>()

    fun feature(name: String) {
        require(name.isNotBlank()) { "Edition feature name must not be blank" }
        features.add(name)
    }

    fun features(vararg names: String) {
        names.forEach(::feature)
    }
}

internal data class EditionSelection(
    val name: String,
    val features: Set<String>,
    val modules: Set<String>
)

internal data class FeatureDefinition(
    val name: String,
    val description: String?,
    val modules: Set<String>
)

internal data class EditionDefinition(
    val name: String,
    val description: String?,
    val features: Set<String>
)
