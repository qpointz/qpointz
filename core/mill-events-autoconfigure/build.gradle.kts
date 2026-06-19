plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill events bus auto-configuration"
    publishArtifacts = true
}

dependencies {
    api(project(":core:mill-events"))
    implementation(libs.boot.starter)
    annotationProcessor(libs.boot.configuration.processor)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(libs.boot.starter.test)
                implementation(libs.assertj.core)
                runtimeOnly(libs.h2.database)
            }
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())
                dependencies {
                    implementation(project())
                    implementation(libs.boot.starter.test)
                    implementation(libs.assertj.core)
                }
            }
        }
    }
}

tasks.named<Test>("testIT") {
    testLogging {
        events("passed", "failed", "skipped")
    }
}
