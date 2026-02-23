plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill metadata core library"
    publishArtifacts = true
}

dependencies {
    api(project(":core:mill-core"))
    api(libs.bundles.jackson)
    api(libs.jackson.module.kotlin)
    implementation(libs.json.schema.validator)
    implementation(libs.bundles.logging)
}

// During Java->Kotlin migration, CI caches can contain stale Java class outputs
// for types that now exist only in Kotlin sources. Exclude duplicates at jar time
// and purge stale java output folder to keep packaging deterministic.
tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    doFirst {
        delete(layout.buildDirectory.dir("classes/java/main"))
    }
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            useJUnitJupiter(libs.versions.junit.get())

            dependencies {
                implementation(project())
                implementation(project(":metadata:mill-metadata-autoconfigure"))
                implementation(libs.boot.starter.test)
                implementation(libs.boot.starter.web)
            }
        }

        configureEach {
            if (this is JvmTestSuite && this.name != "testIT") {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.h2.database)
                }
            }
        }
    }
}
