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
package org.sonarsource.php

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.io.File

fun enforceJarSize(
  file: File,
  minSize: Long,
  maxSize: Long,
) {
  val size = file.length()
  if (size < minSize) {
    throw GradleException("${file.path} size ($size) too small. Min is $minSize")
  } else if (size > maxSize) {
    throw GradleException("${file.path} size ($size) too large. Max is $maxSize")
  }
}

fun Project.signingCondition(): Boolean {
  val branch = System.getenv("GITHUB_REF_NAME") ?: System.getenv("CIRRUS_BRANCH") ?: ""
  return (branch == "master" || branch.matches("branch-.+".toRegex())) &&
    gradle.taskGraph.hasTask(":artifactoryPublish")
}

fun Project.registerCleanupTask(): TaskProvider<Delete> {
  return tasks.register<Delete>("cleanupOldVersion") {
    group = "build"
    description = "Clean up jars of old plugin version"

    delete(
      fileTree(project.layout.buildDirectory.dir("libs")).matching {
        include("${project.name}-*.jar")
        exclude("${project.name}-${project.version}-*.jar")
      },
    )
  }
}
