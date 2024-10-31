plugins {
  id("org.sonarsource.php.java-conventions")
  id("org.sonarsource.php.code-style-convention")
  id("java-library")
}

description = "SonarSource PHP Analyzer :: Checks"

dependencies {
  implementation(project(":php-frontend"))
  implementation(libs.sonar.plugin.api)
  implementation(libs.sonar.analyzer.commons)
  implementation(libs.commons.lang)
  compileOnly(libs.slf4j.api)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.assertj.core)
  testImplementation(libs.mockito.core)
  testImplementation(libs.sonar.plugin.api.test.fixtures)
}
