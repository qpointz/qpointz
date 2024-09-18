import org.gradle.kotlin.dsl.support.zipTo
import java.nio.file.Files
import java.nio.file.Paths

plugins {
    base
    id("jacoco-report-aggregation")
    id ("org.sonarqube") version "5.0.0.4638"
    java
    `maven-publish`
    signing
}

sonar {
    properties {
        property("sonar.projectKey", "qpointz-delta")
        property("sonar.projectName", "qpointz-delta")
        property("sonar.qualitygate.wait", true)        
    }
}

val javaProjects = listOf(
    project(":mill-core"),
    project(":mill-backend-core"),
    project(":mill-calcite-backend"),
    project(":mill-jdbc-backend"),
    project(":clients:mill-jdbc-driver")
)

dependencies {
    javaProjects.forEach { proj -> jacocoAggregation(proj)}
}

tasks.register<Zip>("publishSonatypeBundle") {
    from(layout.buildDirectory.dir("repo"))
    include ("**/*")
    archiveBaseName.set("sonatype-bundle")
    destinationDirectory.set(layout.buildDirectory.dir("sonatype-bundle"))
}


configure(javaProjects) {
    fun getVersion():String {
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

    val defaultGroup = "io.qpointz.mill"
    val defaultVersion = getVersion()

    group = defaultGroup
    version = defaultVersion

    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "jacoco")
    apply(plugin = "jvm-test-suite")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        withJavadocJar()
        withSourcesJar()
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    tasks.jacocoTestReport {
        reports {
            xml.required = true
        }
    }

    publishing {
        repositories {
            maven {
                url = uri(rootProject.layout.buildDirectory.dir("repo"))
            }
        }
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                if (project.tasks.findByName("shadowJar") != null) {
                    artifact(tasks["shadowJar"])
                }
                pom {
                    name = (project.properties["lib.name"] ?:this.name).toString()
                    description = (project.properties["lib.description"] ?:"").toString()
                    url = "https://github.com/qpointz/qpointz"
                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }
                    developers {
                        developer {
                            id = "qpointz"
                            name = "vm"
                            email = "vm@qpointz.io"
                        }
                    }
                    scm {
                        connection = "scm:git:https://github.com/qpointz/qpointz.git"
                        developerConnection = "scm:git:https://github.com/qpointz/qpointz.git"
                        url = "https://github.com/qpointz/qpointz"
                    }
                }
            }
        }
    }

    signing {
        sign(publishing.publications)
    }
}

/*
reporting {
        reports {
            val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
                testType = TestSuiteType.UNIT_TEST
            }
        }
    }
* */

tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}