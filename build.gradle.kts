plugins {
  alias(libs.plugins.spotless)
  id("org.sonarsource.php.artifactory-configuration")
  id("org.sonarsource.php.sonarqube")
}

spotless {
  encoding(Charsets.UTF_8)
  kotlinGradle {
    ktlint().setEditorConfigPath("$rootDir/.editorconfig")
    target("*.gradle.kts", "/build-logic/src/**/*.gradle.kts")
  }
}
