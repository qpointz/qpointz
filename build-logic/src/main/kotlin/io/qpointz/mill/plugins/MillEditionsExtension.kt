package io.qpointz.mill.plugins

import org.gradle.api.Project
import org.gradle.api.provider.Provider

open class MillEditionsExtension(private val project: Project) {
    private val featureModules = linkedMapOf<String, LinkedHashSet<String>>()
    private val editionFeatures = linkedMapOf<String, LinkedHashSet<String>>()
    private val editionIncludes = linkedMapOf<String, LinkedHashSet<String>>()
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
        declaration.inheritsFrom.forEach { baseEdition ->
            includeEdition(name, baseEdition)
        }
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
            .union(editionIncludes.keys)
            .union(editionDescriptions.keys)
            .map { name ->
                EditionDefinition(
                    name = name,
                    description = editionDescriptions[name],
                    features = editionFeatures[name].orEmpty().toSet(),
                    inheritsFrom = editionIncludes[name].orEmpty().toSet()
                )
            }
    }

    internal fun editionFeatureMatrix(): Map<String, Set<String>> {
        val knownEditions = knownEditionNames()
        val cache = linkedMapOf<String, Set<String>>()
        return knownEditions.associateWith { editionName ->
            resolveFeaturesForEdition(
                editionName = editionName,
                visiting = linkedSetOf(),
                cache = cache,
                knownEditions = knownEditions
            )
        }
    }

    internal fun resolveEditionLineage(editionName: String): List<String> {
        val knownEditions = knownEditionNames()
        if (!knownEditions.contains(editionName)) {
            error(
                "Unknown edition '$editionName' for ${project.path}. " +
                    "Allowed values: ${knownEditions.joinToString(", ")}"
            )
        }

        val lineage = mutableListOf<String>()
        val visited = linkedSetOf<String>()
        resolveEditionLineage(
            editionName = editionName,
            visiting = linkedSetOf(),
            visited = visited,
            lineage = lineage,
            knownEditions = knownEditions
        )
        return lineage
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

        val features = resolveFeaturesForEdition(selectedEditionName)

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

    private fun includeEdition(edition: String, inheritedEdition: String) {
        require(edition.isNotBlank()) { "Edition name must not be blank" }
        require(inheritedEdition.isNotBlank()) { "Inherited edition name must not be blank" }
        require(edition != inheritedEdition) { "Edition '$edition' cannot inherit from itself" }
        val includes = editionIncludes.getOrPut(edition) { linkedSetOf() }
        includes.add(inheritedEdition)
    }

    private fun resolveFeaturesForEdition(editionName: String): Set<String> {
        val knownEditions = knownEditionNames()
        if (!knownEditions.contains(editionName)) {
            error(
                "Unknown edition '$editionName' for ${project.path}. " +
                    "Allowed values: ${knownEditions.joinToString(", ")}"
            )
        }
        return resolveFeaturesForEdition(
            editionName = editionName,
            visiting = linkedSetOf(),
            cache = linkedMapOf(),
            knownEditions = knownEditions
        )
    }

    private fun resolveFeaturesForEdition(
        editionName: String,
        visiting: LinkedHashSet<String>,
        cache: MutableMap<String, Set<String>>,
        knownEditions: Set<String>
    ): Set<String> {
        cache[editionName]?.let { return it }

        if (!visiting.add(editionName)) {
            val cycle = (visiting + editionName).joinToString(" -> ")
            error("Edition inheritance cycle detected for ${project.path}: $cycle")
        }

        val resolved = linkedSetOf<String>()
        val inheritedEditions = editionIncludes[editionName].orEmpty()
        inheritedEditions.forEach { baseEdition ->
            if (!knownEditions.contains(baseEdition)) {
                error(
                    "Edition '$editionName' inherits unknown edition '$baseEdition' for ${project.path}. " +
                        "Allowed values: ${knownEditions.joinToString(", ")}"
                )
            }
            resolved.addAll(
                resolveFeaturesForEdition(
                    editionName = baseEdition,
                    visiting = visiting,
                    cache = cache,
                    knownEditions = knownEditions
                )
            )
        }
        resolved.addAll(editionFeatures[editionName].orEmpty())

        visiting.remove(editionName)
        return resolved.toSet().also { cache[editionName] = it }
    }

    private fun resolveEditionLineage(
        editionName: String,
        visiting: LinkedHashSet<String>,
        visited: MutableSet<String>,
        lineage: MutableList<String>,
        knownEditions: Set<String>
    ) {
        if (visited.contains(editionName)) {
            return
        }

        if (!visiting.add(editionName)) {
            val cycle = (visiting + editionName).joinToString(" -> ")
            error("Edition inheritance cycle detected for ${project.path}: $cycle")
        }

        val inheritedEditions = editionIncludes[editionName].orEmpty()
        inheritedEditions.forEach { baseEdition ->
            if (!knownEditions.contains(baseEdition)) {
                error(
                    "Edition '$editionName' inherits unknown edition '$baseEdition' for ${project.path}. " +
                        "Allowed values: ${knownEditions.joinToString(", ")}"
                )
            }
            resolveEditionLineage(
                editionName = baseEdition,
                visiting = visiting,
                visited = visited,
                lineage = lineage,
                knownEditions = knownEditions
            )
        }

        visiting.remove(editionName)
        visited.add(editionName)
        lineage.add(editionName)
    }

    private fun knownEditionNames(): Set<String> {
        return editionFeatures.keys
            .union(editionIncludes.keys)
            .union(editionDescriptions.keys)
            .toSortedSet()
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
    internal val inheritsFrom = linkedSetOf<String>()

    fun feature(name: String) {
        require(name.isNotBlank()) { "Edition feature name must not be blank" }
        features.add(name)
    }

    fun features(vararg names: String) {
        names.forEach(::feature)
    }

    fun from(editionName: String) {
        require(editionName.isNotBlank()) { "Inherited edition name must not be blank" }
        inheritsFrom.add(editionName)
    }

    fun inherits(editionName: String) {
        from(editionName)
    }

    fun imports(editionName: String) {
        from(editionName)
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
    val features: Set<String>,
    val inheritsFrom: Set<String>
)
