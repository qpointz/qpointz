package io.qpointz.mill.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication


class MillPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.pluginManager.withPlugin("mill") {
            project.afterEvaluate {applyPublishing(project)}
        }
    }

    fun applyPublishing(project: Project) {
        val millExt = project.extensions.findByName("mill") as? MillExtension

        project.pluginManager.apply("maven-publish")
        project.pluginManager.apply("signing")

        val publishing = project.extensions.findByName("publishing") as? org.gradle.api.publish.PublishingExtension

        project.extensions.configure(org.gradle.api.plugins.JavaPluginExtension::class.java) {
            withJavadocJar()
            withSourcesJar()
        }

        project.extensions.configure(org.gradle.plugins.signing.SigningExtension::class.java) {
            sign(publishing?.publications)
        }

        project.extensions.configure<PublishingExtension> {
            repositories.maven {
                url = project.uri(project.rootProject.layout.buildDirectory.dir("repo"))
            }
            publications.create("mavenJava", MavenPublication::class.java, {
                val comp = project.components.findByName("java")
                from(comp)
                createPom(project, millExt, comp!!.name)
            })

//            val shadowComponent = project.components.findByName("shadow")
//            if (shadowComponent != null) {
//                publications.create("mavenJavaShadow", MavenPublication::class.java, {
//                    from(project.components.findByName("shadow"))
//                    createPom(project, millExt, shadowComponent.name)
//                })
//            }


        }
    }
}

    private fun MavenPublication.createPom(
        project: Project,
        millExt: MillExtension?,
        pomName: String
    ) {
        pom {
            name.set(pomName)
            description.set(millExt?.description)
            url.set("https://github.com/qpointz/qpointz")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("qpointz")
                    name.set("vm")
                    email.set("vm@qpointz.io")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/qpointz/qpointz.git")
                developerConnection.set("scm:git:https://github.com/qpointz/qpointz.git")
                url.set("https://github.com/qpointz/qpointz")
            }
        }
    }