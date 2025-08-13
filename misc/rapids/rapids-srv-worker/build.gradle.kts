plugins {
	java
	application
	jacoco
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

application {
	mainClass.set("io.qpointz.rapids.server.worker.RapidsWorker")
	executableDir = "bin"
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
}

dependencies {
	implementation(project(":rapids-core-legacy"))

	implementation(libs.lombok)
	annotationProcessor(libs.lombok)


	implementation(libs.vertx.core)
	implementation(libs.smallrye.config)
	implementation(libs.smallrye.config.source.yaml)

	implementation(libs.microprofile.config.api)

	implementation(libs.spring.context)

	implementation(libs.bundles.logging)

	implementation(libs.calcite.core)
	implementation(libs.calcite.csv)
	implementation(libs.avatica.core)
	implementation(libs.avatica.server)

	implementation(libs.jetty.server)
	implementation(libs.jetty.servlet)
	implementation(libs.jetty.security)
	implementation(libs.jetty.openid)

	implementation(libs.olingo.odata.server.core)
	implementation(libs.olingo.odata.server.api)
	implementation(libs.olingo.odata.commons.core)
	implementation(libs.olingo.odata.commons.api)

	runtimeOnly(libs.postgresql)
	runtimeOnly(libs.fusesource.jansi)
}

val bootstrapAppTask = tasks.register("bootstrapApp") {

	dependsOn(tasks.findByPath("installDist"))

	outputs.upToDateWhen { false }
	//ugly hack
	doLast {

		val appDir = layout.buildDirectory.dir("rapids-app")
		val etcDir = layout.buildDirectory.dir("rapids-app/etc")
		val installDir = layout.buildDirectory.dir("install/rapids-srv-worker/")

		delete(
				fileTree(appDir)
		)

		copy {
			from(installDir)
			into(appDir)
		}

		copy {
			from(layout.projectDirectory.file("etc"))
			into(etcDir)
			rename("model.json", "model.json.sample")
		}

		copy {
			from(layout.projectDirectory.file("src/main/resources/application.yaml"))
			into(etcDir)
		}
	}

}


tasks.withType<Test> {
	useJUnitPlatform()
}
