plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill data auto-configuration for Spring Boot"
    publishArtifacts = true
}

dependencies {
    //api(project(":services:mill-service-api"))
    api(project(":security:mill-service-security"))
    api(project(":security:mill-security-autoconfigure"))
    api(project(":data:mill-data-backend-core"))
    api(project(":data:mill-data-source-core"))
    api(project(":data:mill-data-schema-core"))
    api(project(":data:mill-data-backends"))
    api(project(":data:mill-data-query"))
    implementation(project(":data:mill-data-odata"))
    implementation(project(":data:mill-data-metadata"))
    implementation(project(":metadata:mill-metadata-core"))
    implementation(project(":core:mill-sql"))
    api(libs.jakarta.servlet.api)
    api(libs.jakarta.annotation.api)
    implementation(project(":core:mill-spring-support"))
    implementation(libs.calcite.core)
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.security)
    implementation(libs.bundles.jackson)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.boot.configuration.processor)
    runtimeOnly(libs.bundles.logging)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                targets.configureEach {
                    testTask.configure {
                        jvmArgs(
                            "--add-opens=java.base/java.nio=ALL-UNNAMED",
                            "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
                        )
                        systemProperty("arrow.enable_unsafe_memory_access", "true")
                        systemProperty("io.netty.tryReflectionSetAccessible", "true")
                    }
                }

                dependencies {
                    implementation(project())
                    implementation(project(":data:mill-data-backends"))
                    implementation(project(":data:formats:mill-data-format-text"))
                    implementation(project(":data:formats:mill-data-format-avro"))
                    implementation(project(":data:formats:mill-data-format-json"))
                    implementation(project(":data:formats:mill-data-format-arrow"))
                    implementation(project(":data:formats:mill-data-format-excel"))
                    implementation(project(":cloud:aws:mill-cloud-aws-autoconfigure"))
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.actuator)
                    implementation(libs.protobuf.java.util)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.h2.database)
                    implementation(libs.lombok)
                    implementation(libs.testcontainers.core)
                    implementation(libs.aws.sdk.s3)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}
