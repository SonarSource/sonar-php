package org.sonarsource.php

import org.gradle.api.GradleException
import org.gradle.api.Project
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
