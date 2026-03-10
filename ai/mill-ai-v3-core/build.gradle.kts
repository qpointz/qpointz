plugins {
    kotlin("jvm")
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill AI v3 core runtime skeleton"
    publishArtifacts = false
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api(libs.slf4j.api)
    implementation(libs.bundles.jackson)

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
