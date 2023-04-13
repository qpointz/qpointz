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

    implementation(libs.graphql.java)

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
}

tasks {

    register("copyToLib", Copy::class) {
        from(configurations.runtimeClasspath)
        into("$buildDir/output/libs")
    }

}