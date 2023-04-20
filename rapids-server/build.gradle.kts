import io.quarkus.gradle.tasks.QuarkusBuild

plugins {
    java
    id("io.quarkus")
}

dependencies {
    implementation(project(":rapids-core"))


    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.calcite.core)
    implementation(libs.calcite.csv)
    implementation(libs.avatica.core)
    implementation(libs.avatica.server)

    implementation(libs.arrow.flight.core)
    implementation(libs.arrow.flight.grpc)

    implementation(libs.apache.mina.ftpserver.core)
    implementation(libs.apache.mina.ftpserver.ftplet.api)

    //implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:2.16.6.Final"))
    implementation(libs.bundles.quarkus.base.impl)
    testImplementation(libs.bundles.quarkus.base.test)

    testImplementation(libs.rest.assured)
    implementation(libs.quarkus.vertx)
    implementation(libs.quarkus.container.image.docker)

    implementation(libs.quarkus.resteasy.reactive)
    implementation(libs.quarkus.resteasy.reactive.jackson)

    //implementation(libs.graphql.java)

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val bootstrapSampleTask = tasks.register("bootstrapSampleServer") {
    //ugly hack
    outputs.upToDateWhen { false }

    delete(
            fileTree("$buildDir/quarkus-app-sample/etc")
    )

    copy {
        from(layout.projectDirectory.file("etc/model.json"))
        into("$buildDir/quarkus-app-sample/etc")
    }

    copy {
        from(layout.projectDirectory.file("etc/application.yaml"))
        into(file("$buildDir/quarkus-app-sample/config"))
        //rename ("application.yaml.sample","application.yaml")
    }

    copy {
        from(layout.projectDirectory.dir("../etc/testModels/example/"))
        into("$buildDir/quarkus-app-sample/etc/testModels/example")
    }

    copy {
        from(layout.projectDirectory.dir("../etc/testModels/formats/parquet/airlines/"))
        into("$buildDir/quarkus-app-sample/etc/testModels/airlines/data")
    }

    copy {
        from(layout.projectDirectory.dir("../etc/testModels/formats/parquet/partitioned/"))
        into("$buildDir/quarkus-app-sample/etc/testModels/partitioned/data")
    }

    copy {
        from("$buildDir/quarkus-app")
        into("$buildDir/quarkus-app-sample")
    }

}

tasks.withType<QuarkusBuild> {
    dependsOn(bootstrapSampleTask)
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}
