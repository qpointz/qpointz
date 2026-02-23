package io.qpointz.mill.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.internal.cc.base.logger
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.jvm.java


open class MillExtension {
    var description: String = ""
    var publishArtifacts: Boolean = true
}

class MillPlugin: Plugin<Project> {

    fun getVersion(project: Project): String {
        return project.findProperty("projectVersion")?.toString()
            ?: getVersionFromFile(project)
    }

    fun getVersionFromFile(project: Project):String {
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

    override fun apply(project: Project) {
        project.extensions.add("mill", MillExtension::class.java)

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

        project.tasks.findByName("clean")?.apply {
            doLast {
                project.file("bin").deleteRecursively()
            }
        }

        project.pluginManager.apply(MillPublishPlugin::class.java)
    }
}
