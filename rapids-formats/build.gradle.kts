import org.gradle.kotlin.dsl.libs

plugins {
    java
}

dependencies {
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.parquet.avro)

    implementation(libs.azure.storage.file.datalake)

    implementation(project(":rapids-core"))

    //testImplementation(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

tasks {
    register("copyToLib", Copy::class) {
        from(configurations.runtimeClasspath)
        into("$buildDir/output/libs")
    }
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
