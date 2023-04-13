plugins {
    java
}

dependencies {
  implementation(project(":rapids-core"))
}

tasks {

    register("copyToLib", Copy::class) {
        from(configurations.runtimeClasspath)
        into("$buildDir/output/libs")
    }

}
