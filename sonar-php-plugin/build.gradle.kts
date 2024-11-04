import org.sonarsource.php.enforceJarSize
import org.sonarsource.php.registerCleanupTask

plugins {
  id("org.sonarsource.php.java-conventions")
  id("org.sonarsource.php.artifactory-configuration")
  id("org.sonarsource.php.code-style-convention")
  alias(libs.plugins.shadow)
}

dependencies {
  implementation(project(":php-frontend"))
  implementation(project(":php-checks"))
  implementation(libs.sonar.plugin.api)
  implementation(libs.sonar.analyzer.commons)
  implementation(libs.sonar.xml.parsing)
  implementation(libs.staxmate)
  implementation(libs.commons.lang)

  testImplementation(testFixtures(project(":php-frontend")))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.assertj.core)
  testImplementation(libs.mockito.core)
  testImplementation(libs.sonar.plugin.api.test.fixtures)
  testImplementation(libs.sonar.testing.harness)
  testImplementation(libs.sonar.plugin.api.impl)
  testImplementation(libs.sslr.testing.harness)
}

description = "SonarSource PHP Analyzer :: Sonar Plugin"

tasks.jar {
  manifest {
    attributes(
      mapOf(
        "Plugin-ChildFirstClassLoader" to "false",
        "Plugin-Class" to "org.sonar.plugins.php.PhpPlugin",
        "Plugin-Description" to "Analyzer for PHP Files",
        "Plugin-Developers" to "SonarSource Team",
        "Plugin-Display-Version" to version,
        "Plugin-Homepage" to "https://sonarsource.atlassian.net/browse/SONARPHP",
        "Plugin-IssueTrackerUrl" to "https://sonarsource.atlassian.net/browse/SONARPHP",
        "Plugin-Key" to "php",
        "Plugin-License" to "GNU LGPL 3",
        "Plugin-Name" to "PHP Code Quality and Security",
        "Plugin-Organization" to "SonarSource",
        "Plugin-OrganizationUrl" to "https://www.sonarsource.com",
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

val cleanupTask = registerCleanupTask()

tasks.shadowJar {
  dependsOn(cleanupTask)

  minimize()
  exclude("META-INF/LICENSE*")
  exclude("META-INF/NOTICE*")
  exclude("META-INF/*.RSA")
  exclude("META-INF/*.SF")
  exclude("LICENSE*")
  exclude("NOTICE*")
  exclude("**/*.php")

  doLast {
    enforceJarSize(tasks.shadowJar.get().archiveFile.get().asFile, 5_000_000L, 5_500_000L)
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
