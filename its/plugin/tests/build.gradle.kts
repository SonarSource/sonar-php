plugins {
  id("org.sonarsource.php.code-style-convention")
  id("org.sonarsource.php.java-conventions")
}

description = "PHP :: Integration Tests :: Plugin"

dependencies {
  testImplementation(project(":sonar-php-plugin", configuration = "shadow"))
  testImplementation(libs.sonar.orchestrator.junit5)
  testImplementation(libs.junit.platform)
  testImplementation(libs.sonar.ws)
  testImplementation(libs.sonarlint.core)
  testImplementation(libs.sonarlint.plugin.api)
  testImplementation(libs.sonar.plugin.api)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.assertj.core)
  testCompileOnly(libs.jsr305)
}

tasks.test {
  include("**/Tests.class")
}
