plugins {
    `java-library`
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

sourceSets {
    main {
        resources {
            setSrcDirs(listOf("src/main/no-resources"))
        }
    }
}

mill {
    description = "Mill SQL dialect schema resources and typed model foundation."
    publishArtifacts = true
}

dependencies {
    api(project(":core:mill-core"))
    api(kotlin("stdlib"))
    api(libs.jackson.dataformat.yaml)
    api(libs.jackson.databind)
    api(libs.jackson.datatype.jdk8)
    api(libs.jackson.datatype.jsr310)
    api(libs.jackson.module.kotlin)
}

testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                }
            }
        }
    }
}

tasks.processResources {
    from("src/main/resources/sql/dialects") {
        into("sql/v2/dialects")
    }
}
