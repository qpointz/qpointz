package io.qpointz.mill.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.testing.Test
import org.gradle.internal.cc.base.logger
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.jvm.java

/**
 * Root Mill Gradle convention: Java 21 toolchain, JaCoCo, VERSION resolution, edition packaging hooks,
 * and configuration-cache-safe clean extensions.
 */
class MillPlugin : Plugin<Project> {

    /**
     * @param project Gradle project whose version is resolved
     * @return semantic version from `projectVersion` property or `../VERSION` file
     */
    fun getVersion(project: Project): String {
        return project.findProperty("projectVersion")?.toString()
            ?: getVersionFromFile(project)
    }

    /**
     * Reads `VERSION` one directory above the Gradle root; normalizes pre-release suffixes for Gradle compatibility.
     *
     * @param project used to locate the repo root
     * @return version string, or `0.1.0` when the file is missing
     */
    fun getVersionFromFile(project: Project): String {
        val path = Paths.get("${project.rootProject.projectDir}/../VERSION")
        if (!Files.exists(path)) {
            logger.trace("VERSION file missing {}:", path.toAbsolutePath().toString())
            return "0.1.0"
        }

        var version =  Files.readAllLines(path)[0]
        if (".*\\-\\w+\\.\\d+$".toRegex().matches(version)) {
            logger.debug("candidate version")
            version = "\\.(?=\\d+\$)".toRegex().replace(version, "")
        }
        logger.info("Version set to : {}", version)
        return version
    }

    /**
     * Applies Java/JaCoCo defaults, Kotlin JVM toolchain when the Kotlin plugin is present, and Mill extensions.
     *
     * @param project Gradle project receiving the convention
     */
    override fun apply(project: Project) {
        val millExtension = project.extensions.findByType(MillExtension::class.java)
            ?: project.extensions.create("mill", MillExtension::class.java, project)
        registerEditionInfoTasks(project, millExtension.editions)

        val version = getVersion(project)
        project.version = version
        project.group = "io.qpointz.mill"

        project.pluginManager.apply("java")
        project.pluginManager.apply("jacoco")
        project.pluginManager.apply("jvm-test-suite")

        project.extensions.configure(org.gradle.api.plugins.JavaPluginExtension::class.java) {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }

        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            project.extensions.configure(KotlinJvmProjectExtension::class.java) {
                jvmToolchain(21)
            }
        }

        project.tasks.withType(Test::class.java).configureEach {
            workingDir = project.projectDir
        }

        val jacocoTask = project.tasks.findByName("jacocoTestReport") as? org.gradle.testing.jacoco.tasks.JacocoReport
        jacocoTask?.reports?.xml?.required?.set(true)

        project.rootProject.dependencies.add("jacocoAggregation", project)

        // Keep configuration-cache compatible: configure the existing Delete task instead of
        // capturing Project in task actions.
        project.tasks.withType(Delete::class.java).matching { it.name == "clean" }.configureEach {
            delete(project.layout.projectDirectory.dir("bin"))
        }

        project.afterEvaluate {
            if (millExtension.editions.isConfigured()) {
                configureEditionPackaging(project, millExtension.editions)
            }

            if (millExtension.publishArtifacts) {
                project.pluginManager.apply(MillPublishPlugin::class.java)
            }
        }
    }
}
