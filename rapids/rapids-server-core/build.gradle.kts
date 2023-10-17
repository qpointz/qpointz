plugins {
    `java-library`
    id("com.google.protobuf") version "0.9.4"
}

buildscript {
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    }
}

dependencies {
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.bundles.logging)

    implementation(libs.vertx.grpc.server)
    implementation(libs.vertx.grpc.client)
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.24.4"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.58.0"
        }
        create("vertx") {
            artifact = "io.vertx:vertx-grpc-protoc-plugin2:4.4.5"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins{
                create("grpc")
                create("vertx")
            }
        }
    }
}