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
package org.sonar.plugins.php.phpunit;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.ExternalReportProvider;
import org.sonarsource.analyzer.commons.xml.ParseException;

public abstract class PhpUnitReportImporter implements ReportImporter {
  private static final int MAX_LOGGED_FILE_NAMES = 5;

  protected final Set<String> unresolvedInputFiles = new LinkedHashSet<>();

  protected AnalysisWarningsWrapper analysisWarningsWrapper;

  protected PhpUnitReportImporter(AnalysisWarningsWrapper analysisWarningsWrapper) {
    this.analysisWarningsWrapper = analysisWarningsWrapper;
  }

  @Override
  public final void execute(SensorContext context) {
    List<File> reportFiles = reportFiles(context);
    reportFiles.forEach(report -> {
      unresolvedInputFiles.clear();
      importExternalReport(report, context);
      logUnresolvedInputFiles(report);
    });
    if (reportFiles.isEmpty()) {
      logger().info("No {} reports provided (see '{}' property)", reportName(), reportPathKey());
    }
  }

  protected void importExternalReport(File coverageReportFile, SensorContext context) {
    try {
      importReport(coverageReportFile, context);
    } catch (IOException | ParseException e) {
      logFileCantBeRead(e, coverageReportFile.getPath());
    }
  }

  abstract void importReport(File coverageReportFile, SensorContext context) throws IOException, ParseException;

  protected void logUnresolvedInputFiles(File reportPath) {
    if (unresolvedInputFiles.isEmpty()) {
      return;
    }
    String fileList = unresolvedInputFiles.stream().sorted().limit(MAX_LOGGED_FILE_NAMES).collect(Collectors.joining(";"));
    if (unresolvedInputFiles.size() > MAX_LOGGED_FILE_NAMES) {
      fileList += ";...";
    }
    String msg = String.format("Failed to resolve %s file path(s) in %s %s report. Nothing is imported related to file(s): %s",
      unresolvedInputFiles.size(), reportName(), reportPath.getName(), fileList);
    logger().warn(msg);
    analysisWarningsWrapper.addWarning(msg);
  }

  protected void logFileCantBeRead(Exception e, String reportPath) {
    String msg = String.format("An error occurred when reading report file '%s', nothing will be imported from this report. %s: %s"
      , reportPath, e.getClass().getSimpleName(), e.getMessage());
    logger().error(msg);
    analysisWarningsWrapper.addWarning(msg);
  }

  protected List<File> reportFiles(SensorContext context) {
    return ExternalReportProvider.getReportFiles(context, reportPathKey());
  }

  abstract String reportPathKey();

  abstract String reportName();

  abstract Logger logger();

}
