import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.util.Node
import io.qpointz.mill.plugins.MillExtension
import io.qpointz.mill.plugins.applyMillPomMetadata
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    `java-library`
    java
    `java-library-distribution`
    id("io.qpointz.plugins.mill")
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.6"
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill JDBC Driver and Mill Client."
    publishArtifacts = true
}

distributions {
    main {
    }
}

dependencies {
    implementation(project(":core:mill-core"))
    implementation(libs.protobuf.java.util)
    implementation(libs.okhttp)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation(libs.boot.starter.test)
}

tasks.withType<ProcessResources>() {
    from(rootProject.layout.projectDirectory.dir("../").file("VERSION"))
}

tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("mill-jdbc-driver")
    archiveClassifier.set("all")
    mergeServiceFiles {
        include("META-INF/services/java.sql.Driver")
    }
}

components.named("java", AdhocComponentWithVariants::class.java) {
    listOf("shadowRuntimeElements", "shadowApiElements")
        .mapNotNull { configurations.findByName(it) }
        .forEach { shadowConfiguration ->
            withVariantsFromConfiguration(shadowConfiguration) {
                skip()
            }
        }
}

extensions.configure<PublishingExtension>("publishing") {
    val millExt = extensions.findByName("mill") as? MillExtension

    publications.withType(MavenPublication::class.java).configureEach {
        applyMillPomMetadata(millExt, "java")
        if (name == "mavenJava") {
            artifactId = "mill-jdbc-driver"
        }
    }

    publications.create("mavenJavaAll", MavenPublication::class.java) {
        artifactId = "mill-jdbc-driver-all"
        artifact(tasks.named<ShadowJar>("shadowJar"))
        pom.withXml {
            val root = asNode()
            root.children()
                .filterIsInstance<Node>()
                .filter { it.name().toString() == "dependencies" }
                .toList()
                .forEach { root.remove(it) }
        }
    }
}


testing {
    suites {
        register<JvmTestSuite>("testIT") {
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(project(":data:mill-data-backends"))
                    implementation(project(":data:mill-data-autoconfigure"))
                    implementation(project(":data:services:mill-data-grpc-service"))
                    implementation(libs.okhttp.mock.webserver)
                    implementation(libs.bootGRPC.client)
                    implementation(libs.bootGRPC.server)
                    implementation(libs.boot.starter.test)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.lombok)
                    implementation(libs.h2.database)
                    compileOnly(libs.lombok)
                    runtimeOnly(libs.opencensus.impl)
                    runtimeOnly(libs.grpc.census)
                    runtimeOnly(libs.grpc.context)
                    runtimeOnly(libs.grpc.all)
                    annotationProcessor(libs.lombok)

                }

                targets {
                    all {
                        testTask.configure {
                            testLogging {
                                showStandardStreams = true
                                outputs.upToDateWhen { false }
                            }
                        }
                    }
                }
            }
        }
    }
}