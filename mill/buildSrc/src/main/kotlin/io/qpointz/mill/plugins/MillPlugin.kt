package io.qpointz.mill.plugins

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.cc.base.logger
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.task
import org.gradle.kotlin.dsl.withType
import java.nio.file.Files
import java.nio.file.Paths


open class MillExtension {
    var description: String = ""
    var publishToSonatype: Boolean = true
}

class MillPlugin: Plugin<Project> {

    fun getVersion(project: Project):String {
        val path = Paths.get("${project.rootProject.projectDir}/../VERSION")
        if (!Files.exists(path)) {
            logger.trace("VERSION file missing {}:", path.toAbsolutePath().toString())
            return "0.0.1"
        }
        var version =  Files.readAllLines(path).get(0).uppercase()
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
            sourceCompatibility = JavaVersion.VERSION_17
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }

        val lombok = project.rootProject
            .extensions
            .getByType(VersionCatalogsExtension::class.java).named("libs").findLibrary("lombok").get();

        project.dependencies.add("compileOnly",lombok)
        project.dependencies.add("annotationProcessor",lombok)

        val jacocoTask = project.tasks.findByName("jacocoTestReport") as? org.gradle.testing.jacoco.tasks.JacocoReport
        jacocoTask?.reports?.xml?.required?.set(true)

        val annotProcessors = project.configurations.findByName("annotationProcessor")
        val compileOnly = project.configurations.findByName("compileOnly")
        compileOnly!!.extendsFrom(annotProcessors!!)

        project.rootProject.dependencies.add("jacocoAggregation", project)

        /*project.afterEvaluate({
            project.tasks.withType<org.gradle.api.tasks.bundling.Jar> {
                val an = this.archiveBaseName
                if (an.isPresent && ! an.get().startsWith("mill-")) {
                    this.archiveBaseName.set("mill-"+an.get())
                }
            }
        })*/
    }
}