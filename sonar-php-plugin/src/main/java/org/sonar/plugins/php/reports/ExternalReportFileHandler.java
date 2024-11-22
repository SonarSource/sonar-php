/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.reports;

import java.io.File;
import java.nio.file.Path;
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
  public String relativePath(String fileName) {
    // If given path is known by the file system, we don't need to adjust the path
    if (isKnownFile(fileName)) {
      return fileName;
    }

    String normalizedPath = separatorsToSystem(fileName);
    if (normalizedPath == null) {
      return fileName;
    }

    Path path = Path.of(normalizedPath);
    int pathNameCount = path.getNameCount();
    // If we already calculated the offset of the relative path we can apply it to the other paths
    if (relativePathOffset > 0) {
      if (path.getNameCount() > relativePathOffset) {
        path = path.subpath(relativePathOffset, pathNameCount);
        if (isKnownFile(path)) {
          return path.toString();
        }
      }
      return fileName;
    }

    // Reduce the file path by directories until the path is relative to the project directory and known by the file system
    for (int i = 1; i < pathNameCount; i++) {
      // Skip possible first file separator because it could be part of some absolute paths
      Path subpath = path.subpath(i, pathNameCount);
      if (isKnownFile(subpath)) {
        relativePathOffset = i;
        return subpath.toString();
      }
    }

    return fileName;
  }

  /**
   * Checks whether a file exists in the analyzer file system for the specified path.
   */
  private boolean isKnownFile(Path path) {
    return isKnownFile(path.toString());
  }

  private boolean isKnownFile(String path) {
    return fileSystem.hasFiles(fileSystem.predicates().hasPath(path));
  }

  private static String separatorsToSystem(String path) {
    return isSystemWindows() ? separatorsToWindows(path) : separatorsToUnix(path);
  }

  private static boolean isSystemWindows() {
    return File.separatorChar == '\\';
  }

  private static String separatorsToUnix(String path) {
    return path.indexOf(92) != -1 ? path.replace('\\', '/') : path;
  }

  private static String separatorsToWindows(String path) {
    return path.indexOf(47) != -1 ? path.replace('/', '\\') : path;
  }

}
