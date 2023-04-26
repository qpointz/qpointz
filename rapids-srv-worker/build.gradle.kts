plugins {
	java
	application
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

application {
	mainClass.set("io.qpointz.rapids.server.worker.RapidsWorker")
	executableDir = "./"
}


dependencies {
	implementation(libs.lombok)
	annotationProcessor(libs.lombok)

	implementation("io.vertx:vertx-core:4.4.1")
	implementation(project(":rapids-core"))


	implementation("io.smallrye.config:smallrye-config:3.2.1")
	implementation("io.smallrye.config:smallrye-config-source-yaml:3.2.1")
	implementation("org.eclipse.microprofile.config:microprofile-config-api:3.0.2")



	// https://mvnrepository.com/artifact/org.springframework/spring-context
	implementation("org.springframework:spring-context:6.0.8")
	implementation("org.slf4j:slf4j-api:2.0.7")
	implementation("ch.qos.logback:logback-classic:1.4.6")


	implementation(libs.calcite.core)
	implementation(libs.calcite.csv)
	implementation(libs.avatica.core)
	implementation(libs.avatica.server)
}

tasks.withType<Test> {
	useJUnitPlatform()
}
