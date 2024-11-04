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
  val branch = System.getenv()["CIRRUS_BRANCH"] ?: ""
  return (branch == "master" || branch.matches("branch-[\\d.]+".toRegex())) &&
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
      }
    )
  }
}
