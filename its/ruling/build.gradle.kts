plugins {
  id("org.sonarsource.php.code-style-convention")
  id("org.sonarsource.php.java-conventions")
}

description = "PHP :: Integration Tests :: Ruling"

dependencies {
  testImplementation(project(":sonar-php-plugin", configuration = "shadow"))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.assertj.core)
  testImplementation(libs.sonar.analyzer.commons)
  testImplementation(libs.sonar.orchestrator.junit5)
}
