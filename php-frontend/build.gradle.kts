plugins {
  id("org.sonarsource.php.java-conventions")
  id("org.sonarsource.php.code-style-convention")
  id("java-library")
  id("java-test-fixtures")
  id("maven-publish")
}

description = "SonarSource PHP Analyzer :: Frontend"

dependencies {
  api(libs.sonar.analyzer.test.commons)
  api(libs.sslr.core)
  api(libs.sonar.regex.parsing)

  implementation(libs.sonar.plugin.api)
  implementation(libs.commons.lang)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.mockito.core)
  testImplementation(libs.assertj.core)
  testImplementation(libs.sonar.plugin.api.test.fixtures)
  testImplementation(libs.sonar.plugin.api.impl)
  testImplementation(libs.sslr.testing.harness)
  testImplementation(libs.sonar.testing.harness)

  testFixturesImplementation(libs.sonar.plugin.api.impl)
}

publishing {
  repositories {
    mavenLocal()
  }
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
    }
  }
}
