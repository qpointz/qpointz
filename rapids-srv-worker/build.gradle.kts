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
	executableDir = "bin"
}


dependencies {
	implementation(project(":rapids-core"))

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

	runtimeOnly(libs.postgresql)
}

val bootstrapAppTask = tasks.register("bootstrapApp") {

	dependsOn(tasks.findByPath("installDist"))

	outputs.upToDateWhen { false }
	//ugly hack
	doLast {

		val appDir = "$buildDir/rapids-app"

		delete(
				fileTree("$appDir")
		)

		copy {
			from(layout.projectDirectory.file("$buildDir/install/rapids-srv-worker/"))
			into("$appDir")
		}

		copy {
			from(layout.projectDirectory.file("etc"))
			into("$appDir/etc")
			rename("model.json", "model.json.sample")
		}

		copy {
			from(layout.projectDirectory.file("src/main/resources/application.yaml"))
			into("$appDir/etc/")
		}
	}

}


tasks.withType<Test> {
	useJUnitPlatform()
}
