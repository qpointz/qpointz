plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Mill Metadata service"
    publishArtifacts = false
}

dependencies {
    api(project(":core:mill-metadata-core"))
    implementation(project(":core:mill-service-core"))
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.web)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    compileOnly(libs.bundles.logging)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.boot.starter.web)
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}
