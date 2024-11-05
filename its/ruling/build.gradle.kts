plugins {
  id("org.sonarsource.php.code-style-convention")
  id("org.sonarsource.php.java-conventions")
  id("org.sonarsource.php.integration-test")
}

description = "PHP :: Integration Tests :: Ruling"

dependencies {
  "integrationTestImplementation"(project(":sonar-php-plugin", configuration = "shadow"))
  "integrationTestImplementation"(libs.junit.jupiter)
  "integrationTestImplementation"(libs.assertj.core)
  "integrationTestImplementation"(libs.sonar.analyzer.commons)
  "integrationTestImplementation"(libs.sonar.orchestrator.junit5)
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
