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
    project(":core"),
    project(":backends:backend-core"),
    project(":backends:calcite-backend-service"),
    project(":backends:jdbc-backend-service"),
    project(":services:auth-service")
)

dependencies {
    javaProjects.forEach { proj -> jacocoAggregation(proj)}
}


configure(javaProjects) {
    fun getVersion():String {
        val path = Paths.get("${project.rootProject.projectDir}/../VERSION")
        if (!Files.exists(path)) {
            logger.trace("VERSION file missing {}:", path.toAbsolutePath().toString())
            return "0.0.1"
        }
        val version =  Files.readAllLines(path).get(0)
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
                url = uri(layout.buildDirectory.dir("repo"))
            }
        }
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
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