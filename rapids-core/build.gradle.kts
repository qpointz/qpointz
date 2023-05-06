plugins {
    `java-library`
    jacoco
    id("jacoco-report-aggregation")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(libs.calcite.core)
    implementation(libs.calcite.csv)
    implementation(libs.avatica.core)
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

    api(libs.azure.storage.file.datalake)
    implementation(libs.azure.storage.blob.nio)

    implementation(libs.olingo.odata.server.core)
    implementation(libs.olingo.odata.server.api)
    implementation(libs.olingo.odata.commons.core)
    implementation(libs.olingo.odata.commons.api)

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.9.2")
        }


        register<JvmTestSuite>("testIT") {
            useJUnitJupiter("5.9.2")

            dependencies {
                implementation(project())
                implementation(libs.lombok)
                annotationProcessor(libs.lombok)
            }

            testType.set(TestSuiteType.INTEGRATION_TEST)
        }
    }
}