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

public class ExternalReportFileHandler {

  private final FileSystem fileSystem;

  private int relativePathOffset = 0;

  private ExternalReportFileHandler(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  public static ExternalReportFileHandler create(SensorContext context) {
    return new ExternalReportFileHandler(context.fileSystem());
  }

  public String relativePath(String path) {
    // If given path is known by the file system, we don't need to adjust the path
    if (knownFile(path)) {
      return path;
    }

    String newPath;
    // If we already calculated the offset of the relative path we can apply it to the other paths
    if (relativePathOffset > 0) {
      newPath = path.substring(relativePathOffset);
      return knownFile(newPath) ? newPath : path;
    }

    newPath = path;
    // Reduce the file path until the path is relative to the project directory and known by the file system
    do {
      // Skip possible first file separator because it could be part of some absolute paths
      newPath = newPath.substring(newPath.indexOf(File.separatorChar, 1) + 1);
      if (knownFile(newPath)) {
        relativePathOffset = path.indexOf(newPath);
        return newPath;
      }
    } while (newPath.contains(File.separator));

    return path;
  }

  private boolean knownFile(String path) {
    return fileSystem.hasFiles(fileSystem.predicates().hasPath(path));
  }

}
