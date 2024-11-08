import org.sonarsource.php.registerCleanupTask

plugins {
  id("java-library")
  id("jacoco")
  alias(libs.plugins.shadow)
}

dependencies {
  compileOnly(libs.sonar.plugin.api)
  compileOnly(project(":php-frontend"))

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.sonar.plugin.api.impl)
  testImplementation(testFixtures(project(":php-frontend")))
}

description = "PHP Custom Rules Example for SonarQube"

tasks.jar {
  manifest {
    attributes(
      mapOf(
        "Plugin-ChildFirstClassLoader" to "false",
        "Plugin-Class" to "org.sonar.samples.php.PHPCustomRulesPlugin",
        "Plugin-Description" to "PHP Custom Rules Example for SonarQube",
        "Plugin-Developers" to "SonarSource Team",
        "Plugin-Display-Version" to version,
        "Plugin-Homepage" to "https://sonarsource.atlassian.net/browse/SONARPHP",
        "Plugin-IssueTrackerUrl" to "https://sonarsource.atlassian.net/browse/SONARPHP",
        "Plugin-Key" to "custom",
        "Plugin-License" to "GNU LGPL 3",
        "Plugin-Name" to "PHP Custom Rules",
        "Plugin-Organization" to "SonarSource",
        "Plugin-OrganizationUrl" to "https://www.sonarsource.com",
        "Plugin-RequiredForLanguages" to "php",
        "Plugin-SourcesUrl" to "https://github.com/SonarSource/sonar-php",
        "Plugin-Version" to project.version,
        "Sonar-Version" to "9.9",
        "SonarLint-Supported" to "true",
        "Version" to project.version.toString(),
        "Jre-Min-Version" to java.sourceCompatibility.majorVersion,
      ),
    )
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

val cleanupTask = registerCleanupTask()

tasks.shadowJar {
  dependsOn(cleanupTask)
  exclude("**/*.php")
}

artifacts {
  archives(tasks.shadowJar)
}
