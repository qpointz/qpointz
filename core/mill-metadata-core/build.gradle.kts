plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    id("io.qpointz.plugins.mill")
}

mill {
    description = "Mill metadata core library"
    publishArtifacts = true
}

dependencies {
    api(project(":mill-core"))    
    api(libs.boot.starter)    
    api(libs.jackson.dataformat.yaml)
    api(libs.jackson.datatype.jsr310)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

testing {
    suites {
        register<JvmTestSuite>("testIT") {
            useJUnitJupiter(libs.versions.junit.get())
            
            dependencies {
                implementation(project())
                implementation(libs.boot.starter.test)
                implementation(libs.boot.starter.web)
                implementation(libs.lombok)
                annotationProcessor(libs.lombok)
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
                    implementation(libs.lombok)
                    annotationProcessor(libs.lombok)
                }
            }
        }
    }
}
