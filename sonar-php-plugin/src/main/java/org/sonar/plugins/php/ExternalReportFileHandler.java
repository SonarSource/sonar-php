/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
package org.sonar.plugins.php;

import java.io.File;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;

/**
 * This handler allows to convert file paths from external reports into a path relative to the project.
 * This allows processing the report and linking files in the analyzer context file system and the files named in the report.
 * This step is necessary because the context in which the report was created and the analyzer context may differ.
 */
public class ExternalReportFileHandler {

  private final FileSystem fileSystem;

  private int relativePathOffset = 0;

  private ExternalReportFileHandler(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  public static ExternalReportFileHandler create(SensorContext context) {
    return new ExternalReportFileHandler(context.fileSystem());
  }

  /**
   * First, the file path is adjusted to the file system of the analyzer. If a file with the file name is known to the
   * file system of the analyzer context, it will not be adjusted further. If it is not known, the file name is decreased
   * by one folder level to get the correct relative path to the project. This adjustment is then also tried to be applied
   * to subsequent file paths.
   */
  public String relativePath(String path) {
    if (isKnownFile(path)) {
      return path;
    }

    // Adapt file path separator to the analyzer context separator
    String separatorsAdjustedPath = separatorsToSystem(path);

    // If given path is known by the file system, we don't need to adjust the path
    if (isKnownFile(separatorsAdjustedPath)) {
      return separatorsAdjustedPath;
    }

    String newPath;
    // If we already calculated the offset of the relative path we can apply it to the other paths
    if (relativePathOffset > 0) {
      if (separatorsAdjustedPath.length() < relativePathOffset) {
        return path;
      }
      newPath = separatorsAdjustedPath.substring(relativePathOffset);
      return isKnownFile(newPath) ? newPath : path;
    }

    newPath = separatorsAdjustedPath;
    // Reduce the file path by directories until the path is relative to the project directory and known by the file system
    do {
      // Skip possible first file separator because it could be part of some absolute paths
      newPath = newPath.substring(newPath.indexOf(File.separatorChar, 1) + 1);
      if (isKnownFile(newPath)) {
        relativePathOffset = separatorsAdjustedPath.indexOf(newPath);
        return newPath;
      }
    } while (newPath.contains(File.separator));

    return path;
  }

  /**
   * Changes the path from the report accordingly for the file system of the analyzer context.
   * Thus, analyzer and report creation can take place on different file systems.
   * Inspired by <a href="https://commons.apache.org/proper/commons-io/apidocs/org/apache/commons/io/FilenameUtils.html#separatorsToSystem-java.lang.String-">
   * org.apache.commons.io.FilenameUtils::separatorsToSystem</a>
   */
  private static String separatorsToSystem(String path) {
    if (File.separatorChar=='\\') {
      // From Windows to Linux/Mac
      return path.replace('/', File.separatorChar);
    } else {
      // From Linux/Mac to Windows
      return path.replace('\\', File.separatorChar);
    }
  }

  /**
   * Checks whether a file exists in the analyzer file system for the specified path.
   */
  private boolean isKnownFile(String path) {
    return fileSystem.hasFiles(fileSystem.predicates().hasPath(path));
  }

}
