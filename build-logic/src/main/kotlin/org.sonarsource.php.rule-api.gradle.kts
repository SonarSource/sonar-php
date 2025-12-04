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
repositories {
  maven {
    url = project.uri("https://repox.jfrog.io/repox/sonarsource-private-releases")
    authentication {
      credentials {
        val artifactoryUsername: String? by project
        val artifactoryPassword: String? by project
        username = artifactoryUsername
        password = artifactoryPassword
      }
    }
  }
  mavenCentral()
}

val ruleApi: Configuration = configurations.create("ruleApi")
val ruleApiVersion = "2.9.0.4061"

dependencies {
  ruleApi("com.sonarsource.rule-api:rule-api:$ruleApiVersion")
}

tasks.register<JavaExec>("ruleApiUpdate") {
  description = "Update PHP rules description"
  group = "Rule API"
  workingDir = file("$projectDir")
  classpath = configurations.getByName("ruleApi")

  args("com.sonarsource.ruleapi.Main", "update")
}

val rule = providers.gradleProperty("rule")
val branch = providers.gradleProperty("branch")

tasks.register<JavaExec>("ruleApiGenerateRule") {
  description = "Generate PHP rule description"
  group = "Rule API"
  workingDir = file("$projectDir")
  classpath = configurations.getByName("ruleApi")

  args(
    buildList {
      add("com.sonarsource.ruleapi.Main")
      add("generate")
      add("-rule")
      add(rule.getOrElse(""))
      if (branch.isPresent) {
        add("-branch")
        add(branch.get())
      }
    },
  )
}
