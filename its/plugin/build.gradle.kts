plugins {
  id("org.sonarsource.php.code-style-convention")
  id("org.sonarsource.php.java-conventions")
  id("org.sonarsource.php.integration-test")
}

description = "PHP :: Integration Tests :: Plugin"

dependencies {
  "integrationTestCompileOnly"(project(":sonar-php-plugin", configuration = "shadow"))
  "integrationTestImplementation"(libs.sonar.orchestrator.junit5)
  "integrationTestImplementation"(libs.sonar.plugin.api)
  "integrationTestImplementation"(libs.sonar.ws)
  "integrationTestImplementation"(libs.sonar.lint.core)
  "integrationTestImplementation"(libs.sonar.lint.rpc.java.client)
  "integrationTestImplementation"(libs.sonar.lint.rpc.impl)
  "integrationTestImplementation"(libs.junit.platform)
  "integrationTestImplementation"(libs.junit.jupiter)
  "integrationTestImplementation"(libs.assertj.core)
  "integrationTestImplementation"(libs.awaitility)
  "integrationTestCompileOnly"(libs.jsr305)
}

val integrationTest by sourceSets.integrationTest

(tasks["integrationTest"] as Test).filter {
  setIncludePatterns("Tests")
}

// Mandatory for the orchestrator in the "Tests" class, since it requires the custom rules plugin JAR
tasks.named("integrationTest") {
  dependsOn(":php-custom-rules-plugin:shadowJar")
}

sonar {
  isSkipProject = true
}

codeStyleConvention {
  spotless {
    format("javaMisc") {
      targetExclude("**/integrationTest/**")
    }
  }
}
