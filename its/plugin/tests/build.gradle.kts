plugins {
  id("org.sonarsource.php.code-style-convention")
  id("org.sonarsource.php.java-conventions")
  id("org.sonarsource.php.integration-test")
}

description = "PHP :: Integration Tests :: Plugin"

dependencies {
  "integrationTestImplementation"(project(":sonar-php-plugin", configuration = "shadow"))
  "integrationTestImplementation"(libs.sonar.orchestrator.junit5)
  "integrationTestImplementation"(libs.junit.platform)
  "integrationTestImplementation"(libs.sonar.ws)
  "integrationTestImplementation"(libs.sonarlint.core)
  "integrationTestImplementation"(libs.sonarlint.plugin.api)
  "integrationTestImplementation"(libs.sonar.plugin.api)
  "integrationTestImplementation"(libs.junit.jupiter)
  "integrationTestImplementation"(libs.assertj.core)
  "integrationTestCompileOnly"(libs.jsr305)
}

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
