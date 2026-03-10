plugins {
    kotlin("jvm")
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill AI v3 LangChain4j integration skeleton"
    publishArtifacts = false
}

dependencies {
    api(project(":ai:mill-ai-v3-core"))
    implementation(project(":ai:mill-ai-v3-capabilities"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.langchain4j.core)
    implementation(libs.langchain4j.open.ai)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
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
