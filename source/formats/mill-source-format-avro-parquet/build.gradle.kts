plugins {
    kotlin("jvm")
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Mill source format — Avro, Parquet"
    publishArtifacts = true
}

dependencies {
    api(project(":source:mill-source-core"))
    implementation(libs.apache.avro)
    implementation(libs.apache.parquet.avro)
    implementation(libs.apache.hadoop.common) {
        exclude(group = "org.slf4j")
    }
    implementation(libs.apache.hadoop.client) {
        exclude(group = "org.slf4j")
    }
    implementation(libs.hadoop.bare.naked.local.fs.lib)
    compileOnly(libs.bundles.logging)
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
                    implementation(libs.slf4j.api)
                    implementation(libs.logback.core)
                    implementation(libs.logback.classic)
                }
            }
        }
    }
}
