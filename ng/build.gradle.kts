plugins {
    id("org.sonarqube") version "4.0.0.2929"
}
allprojects {
    group = "io.qpointz.rapids"
    version = "1.0.0-SNAPSHOT"

   /* configurations.all {
        resolutionStrategy {
            //failOnVersionConflict()

            force("org.eclipse.jetty:jetty-server:9.4.48.v20220622", "org.eclipse.jetty:jetty-server:9.4.44.v20210927")
            force("org.eclipse.jetty:jetty-util:9.4.48.v20220622", "org.eclipse.jetty:jetty-util:9.4.44.v20210927")
            force("javax.servlet:javax.servlet-api:4.0.1", "javax.servlet:javax.servlet-api:3.1.0")
        }
    }*/

}

