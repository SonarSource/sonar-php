import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
  `java-library`
  jacoco
}

java {
  withSourcesJar()
  withJavadocJar()
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
  options.encoding = "UTF-8"
  options {
    (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    // log the full stack trace (default is the 1st line of the stack trace)
    exceptionFormat = TestExceptionFormat.FULL
    // verbose log for failed and skipped tests (by default the name of the tests are not logged)
    events(SKIPPED, FAILED)
  }
}

jacoco {
  toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
    csv.required.set(false)
    html.required.set(providers.environmentVariable("CI").map { it.toBoolean().not() }.orElse(true))
  }
}

plugins.withType<JacocoPlugin> {
  tasks["test"].finalizedBy("jacocoTestReport")
}
