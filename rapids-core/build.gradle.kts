plugins {
    java
}

dependencies {
    implementation(libs.calcite.core)
    implementation(libs.calcite.csv)

    implementation(libs.avatica.core)
    implementation(libs.avatica.server)

    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(libs.bundles.quarkus.base.impl)
    testImplementation(libs.bundles.quarkus.base.test)

    //implementation(libs.graphql.java)

    implementation(libs.parquet.avro)
    //implementation(libs.parquet.common)
    //implementation(libs.parquet.column)
    //implementation(libs.parquet.hadoop)
    implementation(libs.avro)
    implementation(libs.avro.mapred)
    implementation(libs.hadoop.common) {
        exclude("javax.ws.rs")
    }
    implementation(libs.hadoop.client) {
        exclude("javax.ws.rs")
    }

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
    }
}
