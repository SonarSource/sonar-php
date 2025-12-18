/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
import org.sonarsource.cloudnative.gradle.enforceJarSize


plugins {
  id("org.sonarsource.cloud-native.sonar-plugin")
  id("org.sonarsource.cloud-native.license-file-generator")
}

dependencies {
  compileOnly(libs.sonar.plugin.api)
  api(project(":php-frontend"))
  implementation(project(":php-checks"))
  implementation(libs.sonar.analyzer.commons)
  implementation(libs.woodstox)
  implementation(libs.staxmate)

  testImplementation(testFixtures(project(":php-frontend")))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.assertj.core)
  testImplementation(libs.mockito.core)
  testImplementation(libs.sonar.plugin.api.test.fixtures)
  testImplementation(libs.sonar.testing.harness)
  testImplementation(libs.sonar.plugin.api.impl)
  testImplementation(libs.sslr.testing.harness)
  testRuntimeOnly(libs.junit.platform.launcher)
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
        "Plugin-License" to "SSALv1",
        "Plugin-Name" to "PHP Code Quality and Security",
        "Plugin-Organization" to "SonarSource",
        "Plugin-OrganizationUrl" to "https://www.sonarsource.com",
        "Plugin-RequiredForLanguages" to "php",
        "Plugin-SourcesUrl" to "https://github.com/SonarSource/sonar-php",
        "Plugin-Version" to project.version,
        "Sonar-Version" to "9.9",
        "SonarLint-Supported" to "true",
        "Version" to project.version.toString(),
        "Jre-Min-Version" to java.sourceCompatibility.majorVersion
      )
    )
  }
}

tasks.shadowJar {
  minimizeJar = true
  exclude("META-INF/LICENSE*")
  exclude("META-INF/NOTICE*")
  exclude("META-INF/*.RSA")
  exclude("META-INF/*.SF")
  exclude("LICENSE*")
  exclude("NOTICE*")
  exclude("**/*.php")

  val logger = project.logger
  doLast {
    enforceJarSize(tasks.shadowJar.get().archiveFile.get().asFile, 2_900_000L, 3_500_000L, logger)
  }
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

publishingConfiguration {
  pomName = "SonarSource PHP Analyzer"
  scmUrl = "https://github.com/SonarSource/sonar-php"

  license {
    name = "SSALv1"
    url = "https://sonarsource.com/license/ssal/"
    distribution = "repo"
  }
}
