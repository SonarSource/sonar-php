import org.sonarsource.php.enforceJarSize
import org.sonarsource.php.registerCleanupTask

plugins {
  id("org.sonarsource.php.java-conventions")
  id("org.sonarsource.php.artifactory-configuration")
  id("org.sonarsource.php.code-style-convention")
  alias(libs.plugins.shadow)
}

dependencies {
  implementation(libs.sonar.plugin.api)
  implementation(project(":sonar-php-plugin"))
  implementation(project(":php-frontend"))

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.sonar.plugin.api.impl)
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
        "Plugin-Key" to "php",
        "Plugin-License" to "GNU LGPL 3",
        "Plugin-Name" to "PHP Custom Rules",
        "Plugin-Organization" to "SonarSource",
        "Plugin-OrganizationUrl" to "https://www.sonarsource.com",
        "Plugin-SourcesUrl" to "https://github.com/SonarSource/sonar-php",
        "Plugin-Version" to project.version,
        "Sonar-Version" to "9.9",
        "SonarLint-Supported" to "true",
        "Version" to project.version.toString(),
        "Jre-Min-Version" to java.sourceCompatibility.majorVersion,
        "Plugin-Api-Min-Version" to "9.13",
        "Requiered-For-Languages" to "php",
        "Skip-Dependencies-Packaging" to "true",
      ),
    )
  }
}

val cleanupTask = registerCleanupTask()

tasks.shadowJar {
  dependsOn(cleanupTask)

  minimize()
  exclude("**/*.php")

  doLast {
    enforceJarSize(tasks.shadowJar.get().archiveFile.get().asFile, 2_500_000L, 3_000_000L)
  }
}

artifacts {
  archives(tasks.shadowJar)
}

publishing {
  publications.withType<MavenPublication> {
    artifact(tasks.shadowJar) {
      // remove `-all` suffix from the fat jar
      classifier = null
    }
    artifact(tasks.sourcesJar)
    artifact(tasks.javadocJar)
  }
}
