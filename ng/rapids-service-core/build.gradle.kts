import com.google.protobuf.gradle.*

plugins {
    java
    `java-library`
    id("com.google.protobuf") version ("0.9.4")
}

buildscript {
  dependencies {
        classpath ("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
  }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
        }
        id("vertx") {
            artifact = "io.vertx:vertx-grpc-protoc-plugin2:4.4.5"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
                id("vertx")
            }
        }
    }
}

dependencies {
    implementation(libs.vertx.grpc.server)
    implementation(libs.vertx.grpc.client)
    implementation(libs.google.protobuf.java)
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.bundles.logging)
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}