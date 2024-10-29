/*
 * SonarQube Text Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.php

import org.gradle.api.Project
import java.time.LocalDate

const val GENERATED_SOURCES_DIR = "generated/sources/secrets/java/main"
const val lineSeparator = "\n"

fun Project.loadLicenseHeader() =
  rootProject.file("LICENSE_HEADER").readText(Charsets.UTF_8)
    .replace("\$YEAR", LocalDate.now().year.toString())
    .trimEnd()

fun Project.writeToFile(content: String, relativePath: String) {
  val directory = relativePath.substringBeforeLast('/')
  val filename = relativePath.substringAfterLast('/')
  val dir = layout.buildDirectory.dir("$GENERATED_SOURCES_DIR/$directory").get().asFile
  dir.mkdirs()
  file(dir.resolve(filename)).writeText(content + lineSeparator)
}
