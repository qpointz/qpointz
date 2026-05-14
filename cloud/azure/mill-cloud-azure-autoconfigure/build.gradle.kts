plugins {
    `java-library`
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill ADLS / Azure Blob Storage Spring Boot auto-configuration"
    publishArtifacts = true
}

dependencies {
    api(project(":cloud:azure:mill-cloud-azure-blob"))
    implementation(libs.azure.storage.blob)
    implementation(libs.boot.starter)
    annotationProcessor(libs.boot.configuration.processor)
    compileOnly(libs.bundles.logging)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.assertj.core)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.slf4j.api)
                    implementation(libs.logback.core)
                    implementation(libs.logback.classic)
                }
            }
        }
    }
}
