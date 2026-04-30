plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
}

mill {
    description = "Mill metadata auto-configuration"
    publishArtifacts = true
}

dependencies {
    api(project(":metadata:mill-metadata-core"))
    api(project(":metadata:mill-metadata-service"))
    compileOnly(project(":metadata:mill-metadata-persistence"))
    /** Types only (e.g. [io.qpointz.mill.data.schema.MetadataEntityUrnCodec]); bean is [MetadataEntityUrnCodecAutoConfiguration] in mill-data-autoconfigure. */
    compileOnly(project(":data:mill-data-schema-core"))
    implementation(libs.boot.starter)
    implementation(libs.boot.starter.webmvc)
    implementation(libs.json.schema.validator)
    annotationProcessor(libs.boot.configuration.processor)
}

testing {
    suites {
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
