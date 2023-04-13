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

    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation(libs.bundles.quarkus.base.impl)
    testImplementation(libs.bundles.quarkus.base.test)

    testImplementation(libs.rest.assured)
    implementation(libs.quarkus.vertx)
    implementation(libs.quarkus.resteasy.reactive)

    implementation(libs.graphql.java)

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}
