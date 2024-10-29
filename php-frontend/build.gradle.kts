plugins {
  id("org.sonarsource.php.java-conventions")
  id("org.sonarsource.php.code-style-convention")
  id("java-library")
  id("java-test-fixtures")
}

description = "SonarSource PHP Analyzer :: Frontend"

dependencies {
  api(libs.sonar.analyzer.test.commons)
  api(libs.sslr.core)
  api(libs.sonar.regex.parsing)

  implementation(libs.sonar.plugin.api)
  implementation(libs.sslr.testing.harness)
  implementation(libs.sonar.testing.harness)
  implementation(libs.commons.lang)
  implementation(libs.sonar.plugin.api.impl)
  compileOnly(libs.slf4j.api)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.mockito.core)
  testImplementation(libs.assertj.core)
  testImplementation(libs.sonar.plugin.api.test.fixtures)

  testFixturesImplementation(libs.sonar.plugin.api.impl)
}
