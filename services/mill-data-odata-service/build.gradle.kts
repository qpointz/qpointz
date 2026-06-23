import org.gradle.api.plugins.jvm.JvmTestSuite

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "OData v4 HTTP service (/services/odata/{schema}.svc)"
    publishArtifacts = true
}

dependencies {
    implementation(project(":core:mill-spring-support"))
    implementation(project(":services:mill-service-api"))
    implementation(project(":data:mill-data-odata"))
    implementation(project(":data:mill-data-autoconfigure"))
    implementation(libs.sdl.odata.api)
    implementation(libs.sdl.odata.edm)
    implementation(libs.sdl.odata.parser)
    implementation(libs.sdl.odata.renderer)
    implementation(libs.sdl.odata.processor)
    implementation(libs.boot.starter.webmvc)
    implementation(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.boot.configuration.processor)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            useJUnitJupiter(libs.versions.junit.get())

            targets {
                all {
                    testTask.configure {
                        systemProperty(
                            "skymill.datasets.dir",
                            rootProject.file("test/datasets/skymill").absolutePath,
                        )
                    }
                }
            }

            dependencies {
                implementation(project())
                implementation(project(":data:mill-data-autoconfigure"))
                implementation(project(":data:formats:mill-data-format-text"))
                implementation(project(":metadata:mill-metadata-autoconfigure"))
                implementation(platform(libs.boot.dependencies))
                implementation("org.springframework.security:spring-security-test")
                implementation(libs.boot.starter.test)
                implementation(libs.boot.starter.webmvc)
                implementation(libs.boot.starter.webmvc.test)
                implementation(libs.assertj.core)
            }
        }
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                }
            }
        }
    }
}
