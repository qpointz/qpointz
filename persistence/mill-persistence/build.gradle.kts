plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill persistence — JPA entities, Flyway migrations, and store adapters."
    publishArtifacts = true
}

dependencies {
    api(project(":core:mill-core"))
    api(libs.boot.starter.data.jpa)
    api(libs.flyway.core)
    runtimeOnly(libs.flyway.database.postgresql)
    implementation(kotlin("reflect"))
    runtimeOnly(libs.h2.database)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            dependencies {
                implementation(project())
                implementation(libs.boot.starter.test)
                implementation(libs.assertj.core)
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
