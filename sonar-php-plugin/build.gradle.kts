import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.sonarsource.php.enforceJarSize

plugins {
  id("org.sonarsource.php.java-conventions")
  id("org.sonarsource.php.artifactory-configuration")
  id("org.sonarsource.php.code-style-convention")
  jacoco
  id("com.github.johnrengelman.shadow") version "8.1.1"
  id("java-library")
  id("java-test-fixtures")
}

dependencies {
  implementation(project(":php-frontend")) {
    exclude(group = "junit", module = "junit")
  }
  implementation(project(":php-checks"))
  implementation(libs.sonar.plugin.api)
  implementation(libs.sonar.testing.harness) {
    exclude(group = "junit", module = "junit")
  }
  implementation(libs.sonar.plugin.api.impl) {
    exclude(group = "junit", module = "junit")
  }
  implementation(libs.sslr.testing.harness) {
    exclude(group = "junit", module = "junit")
  }
  implementation(libs.sonar.analyzer.commons)
  implementation(libs.sonar.xml.parsing)
  implementation(libs.staxmate)
  compileOnly(libs.slf4j.api)

  testImplementation(testFixtures(project(":php-frontend")))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.assertj.core)
  testImplementation(libs.mockito.core)
  testImplementation(libs.sonar.plugin.api.test.fixtures)
}

description = "SonarSource PHP Analyzer :: Sonar Plugin"

tasks.test {
  useJUnitPlatform()
  // pass the filename property to SecretsRegexTest if it is set
  System.getProperty("filename")?.let { systemProperty("filename", it) }
  testLogging {
    // log the full stack trace (default is the 1st line of the stack trace)
    exceptionFormat = TestExceptionFormat.FULL
    // verbose log for failed and skipped tests (by default the name of the tests are not logged)
    events(SKIPPED, FAILED)
  }
}

jacoco {
  toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
    csv.required.set(false)
    html.required.set(false)
  }
}

// when subproject has Jacoco plugin applied we want to generate XML report for coverage
plugins.withType<JacocoPlugin> {
  tasks["test"].finalizedBy("jacocoTestReport")
}

// used to be done by sonar-packaging maven plugin
tasks.jar {
  manifest {
    attributes(
      mapOf(
        "Plugin-ChildFirstClassLoader" to "false",
        "Plugin-Class" to "org.sonar.plugins.common.TextAndSecretsPlugin",
        "Plugin-Description" to "Analyzer for PHP Files",
        "Plugin-Developers" to "SonarSource Team",
        "Plugin-Display-Version" to version,
        "Plugin-Homepage" to "https://sonarsource.atlassian.net/browse/SONARPHP",
        "Plugin-IssueTrackerUrl" to "https://sonarsource.atlassian.net/browse/SONARPHP",
        "Plugin-Key" to "text",
        "Plugin-License" to "GNU LGPL 3",
        "Plugin-Name" to "Text Code Quality and Security",
        "Plugin-Organization" to "SonarSource",
        "Plugin-OrganizationUrl" to "https://www.sonarsource.com",
        "Plugin-SourcesUrl" to "https://github.com/SonarSource/sonar-text",
        "Plugin-Version" to project.version,
        "Sonar-Version" to "9.8",
        "SonarLint-Supported" to "true",
        "Version" to project.version.toString(),
        "Jre-Min-Version" to java.sourceCompatibility.majorVersion,
      ),
    )
  }
}

val cleanupTask =
  tasks.register<Delete>("cleanupOldVersion") {
    group = "build"
    description = "Clean up jars of old plugin version"

    delete(
      fileTree(project.layout.buildDirectory.dir("libs")).matching {
        include("${project.name}-*.jar")
        exclude("${project.name}-${project.version}-*.jar")
        exclude("${project.name}-${project.version}.jar")
      },
    )
  }

tasks.shadowJar {
  dependsOn(cleanupTask)

  minimize()
  exclude("META-INF/LICENSE*")
  exclude("META-INF/NOTICE*")
  exclude("META-INF/*.RSA")
  exclude("META-INF/*.SF")
  exclude("LICENSE*")
  exclude("NOTICE*")

  doLast {
    enforceJarSize(tasks.shadowJar.get().archiveFile.get().asFile, 6_500_000L, 12_500_000L)
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

artifactoryConfiguration {
  license {
    name.set("GNU LPGL 3")
    url.set("http://www.gnu.org/licenses/lgpl.txt")
    distribution.set("repo")
  }
  repoKeyEnv = "ARTIFACTORY_DEPLOY_REPO"
  usernameEnv = "ARTIFACTORY_DEPLOY_USERNAME"
  passwordEnv = "ARTIFACTORY_DEPLOY_PASSWORD"
}

codeStyleConvention {
  licenseHeaderFile.set(rootProject.file("LICENSE_HEADER"))
}
