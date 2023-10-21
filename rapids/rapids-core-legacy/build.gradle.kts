plugins {
    `java-library`
    jacoco
}



dependencies {
    api(libs.calcite.core)
    implementation(libs.calcite.csv)
    implementation(libs.avatica.server)
    api(libs.parquet.avro)
    implementation(libs.avro)
    implementation(libs.avro.mapred)
    implementation(libs.hadoop.common) {
        exclude("javax.ws.rs")
    }
    implementation(libs.hadoop.client) {
        exclude("javax.ws.rs")
    }

    implementation("com.google.guava:guava:31.1-jre")

    api(libs.azure.storage.file.datalake)
    implementation(libs.azure.storage.blob.nio)

    implementation(libs.olingo.odata.server.core)
    implementation(libs.olingo.odata.server.api)
    implementation(libs.olingo.odata.commons.core)
    implementation(libs.olingo.odata.commons.api)

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.bundles.logging)
}

tasks.withType<Test> {
    testLogging.showStandardStreams = true
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            testType.set(TestSuiteType.INTEGRATION_TEST)
        }

        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter(libs.versions.junit.get())

                dependencies {
                    implementation(project())
                    implementation(libs.mockito.core)
                    implementation(libs.mockito.junit.jupiter)
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}