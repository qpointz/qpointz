plugins {
    kotlin("jvm")
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill AI v3 — merged runtime: core types, LangChain4j adapter, and capabilities"
    publishArtifacts = false
}

dependencies {
    api(libs.reactor.core)
    api(libs.langchain4j.core)
    implementation(libs.langchain4j.open.ai)
    implementation(libs.bundles.jackson)
    implementation(libs.caffeine)
    implementation(libs.slf4j.api)
    implementation(project(":metadata:mill-metadata-core"))
    implementation(project(":core:mill-sql"))
    implementation(project(":data:mill-data-metadata"))
    implementation(project(":data:mill-data-backend-core"))

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.mockito.kotlin)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                }
            }
        }
    }
}

repositories {
    mavenCentral()
}
