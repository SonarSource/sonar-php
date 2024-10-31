plugins {
  alias(libs.plugins.spotless)
  id("org.sonarsource.php.artifactory-configuration")
  id("org.sonarsource.php.sonarqube")
  id("com.diffplug.blowdryer")
}

tasks.artifactoryPublish { skip = true }

artifactoryConfiguration {
  artifactsToPublish = "org.sonarsource.text:sonar-text-plugin:jar"
  artifactsToDownload = ""
  repoKeyEnv = "ARTIFACTORY_DEPLOY_REPO"
  usernameEnv = "ARTIFACTORY_DEPLOY_USERNAME"
  passwordEnv = "ARTIFACTORY_DEPLOY_PASSWORD"
}

spotless {
  encoding(Charsets.UTF_8)
  kotlinGradle {
    ktlint().setEditorConfigPath("$rootDir/.editorconfig")
    target("*.gradle.kts", "/build-logic/src/**/*.gradle.kts")
  }
}

// TODO Remove after SONARIAC-1521, since the ITs won't be in the test task anymore
// tasks.named<Test>("test") {
//  exclude("**/its/**")
// }
