/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.plugins.php.phpunit;

import java.io.File;
import java.util.Optional;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public abstract class SingleFileReportImporter implements ReportImporter {

  private static final Logger LOG = Loggers.get(SingleFileReportImporter.class);

  private String reportPathKey;
  private String msg;

  protected SingleFileReportImporter(String reportPathKey, String msg) {
    this.reportPathKey = reportPathKey;
    this.msg = msg;
  }

  @Override
  public final void importReport(SensorContext context) {
    Optional<String> reportPath = context.config().get(reportPathKey);
    if (reportPath.isPresent()) {
      importReport(reportPath.get(), msg, context);
    } else {
      LOG.info("No PHPUnit {} report provided (see '{}' property)", msg, reportPathKey);
    }
  }

  final void importReport(String reportPath, String msg, SensorContext context) {
    Optional<File> maybeFile = getIOFile(reportPath, context);
    maybeFile.ifPresent(file -> {
      LOG.info("Analyzing PHPUnit {} report: {}", msg, reportPath);
      importReport(file, context);
    });
  }

  protected abstract void importReport(File coverageReportFile, SensorContext context);

  /*
   * Returns a java.io.File for the given path.
   * If path is not absolute, returns a File with module base directory as parent path.
   */
  private Optional<File> getIOFile(String path, SensorContext context) {
    File file = new File(path);
    if (!file.isAbsolute()) {
      file = new File(context.fileSystem().baseDir(), path);
    }
    if (file.exists()) {
      return Optional.of(file);
    } else {
      LOG.warn("PHPUnit xml {} report not found: {}", msg, path);
      return Optional.empty();
    }
  }

}
