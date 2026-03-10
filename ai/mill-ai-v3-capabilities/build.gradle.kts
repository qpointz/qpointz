plugins {
    kotlin("jvm")
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill AI v3 capability skeletons"
    publishArtifacts = false
}

dependencies {
    api(project(":ai:mill-ai-v3-core"))
    implementation(kotlin("stdlib-jdk8"))

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
