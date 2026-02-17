plugins {
    alias(libs.plugins.kotlin)
    id("io.qpointz.plugins.mill")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

mill {
    description = "Mill source format — Parquet"
    publishArtifacts = true
}

dependencies {
    api(project(":data:mill-data-source-core"))
    implementation(project(":data:formats:mill-source-format-avro"))
    implementation(libs.apache.parquet.avro)
    // parquet-hadoop requires hadoop-common (provided scope in parquet pom).
    // We include it with aggressive exclusions to strip ~400MB of transitive bloat
    // (Jetty, Curator, Zookeeper, Jersey, Kerby, etc.). Only core Hadoop classes
    // (Configuration, Configurable, codecs) are needed by parquet-hadoop internals.
    implementation(libs.apache.hadoop.common) {
        exclude(group = "org.slf4j")
        exclude(group = "org.eclipse.jetty")
        exclude(group = "javax.servlet")
        exclude(group = "org.apache.curator")
        exclude(group = "org.apache.zookeeper")
        exclude(group = "org.apache.kerby")
        exclude(group = "org.apache.hadoop", module = "hadoop-auth")
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "org.apache.httpcomponents.client5")
        exclude(group = "org.apache.httpcomponents.core5")
        exclude(group = "com.google.protobuf")
        exclude(group = "com.google.code.gson")
        exclude(group = "com.google.re2j")
        exclude(group = "com.sun.jersey")
        exclude(group = "commons-cli")
        exclude(group = "org.codehaus.jettison")
        exclude(group = "io.dropwizard.metrics")
        exclude(group = "dnsjava")
    }
    // hadoop-mapreduce-client-core: needed by parquet-hadoop's ParquetReadOptions
    // for FileInputFormat class reference (~1.8MB).
    implementation("org.apache.hadoop:hadoop-mapreduce-client-core:3.4.1") {
        exclude(group = "org.slf4j")
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.apache.hadoop", module = "hadoop-yarn-common")
        exclude(group = "org.apache.hadoop", module = "hadoop-yarn-client")
        exclude(group = "org.apache.hadoop", module = "hadoop-hdfs-client")
        exclude(group = "io.dropwizard.metrics")
        exclude(group = "com.google.inject")
        exclude(group = "com.google.inject.extensions")
    }
    implementation(libs.apache.avro)
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
