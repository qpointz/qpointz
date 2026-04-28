package io.qpointz.mill.ai.autoconfigure

import org.springframework.context.annotation.ImportSelector
import org.springframework.core.type.AnnotationMetadata

/**
 * Decides whether to register [AiV3JpaRepositoriesConfiguration].
 *
 * [org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass] does not apply when a
 * configuration class is listed in [org.springframework.context.annotation.Import] (the class is
 * imported unconditionally), which caused duplicate `@EnableJpaRepositories` when
 * [io.qpointz.mill.persistence.configuration.PersistenceAutoConfiguration] was also present.
 *
 * When `mill-persistence-autoconfigure` is on the classpath (the usual case), that auto-configuration
 * already enables Spring Data for `io.qpointz.mill.persistence` **including** AI v3 repositories — so we
 * omit the AI-only registry. When it is absent, we import [AiV3JpaRepositoriesConfiguration] so AI
 * repositories are still discovered.
 *
  * Detection uses Class.forName with the thread context class loader and this type's class loader.
 * PersistenceAutoConfiguration is provided by a separate artifact — during @Import, the effective
 * loader can differ from Spring's ClassUtils.isPresent defaults.
 */
internal class AiV3JpaRepositoriesImportSelector : ImportSelector {

    override fun selectImports(importingClassMetadata: AnnotationMetadata): Array<String> =
        if (hasPersistenceAutoConfiguration()) {
            emptyArray()
        } else {
            arrayOf(AiV3JpaRepositoriesConfiguration::class.java.name)
        }

    private fun hasPersistenceAutoConfiguration(): Boolean {
        val loaders = listOfNotNull(
            Thread.currentThread().contextClassLoader,
            AiV3JpaRepositoriesImportSelector::class.java.classLoader,
        ).distinct()

        return loaders.any { loader ->
            try {
                Class.forName(PERSISTENCE_AUTO_CONFIGURATION, false, loader)
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        }
    }

    private companion object {
        private const val PERSISTENCE_AUTO_CONFIGURATION =
            "io.qpointz.mill.persistence.configuration.PersistenceAutoConfiguration"
    }
}
