/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.plugins.php.api.Php;
import org.sonarsource.analyzer.commons.ExternalReportProvider;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

public abstract class ExternalIssuesSensor implements Sensor {

  private static final int MAX_LOGGED_FILE_NAMES = 20;
  protected static final Long DEFAULT_CONSTANT_DEBT_MINUTES = 5L;

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyWhenConfiguration(this::shouldExecute)
      .onlyOnLanguage(Php.KEY)
      .name("Import of " + reportName() + " issues");
  }

  @Override
  public void execute(SensorContext context) {
    Set<String> unresolvedInputFiles = new HashSet<>();
    List<File> reportFiles = ExternalReportProvider.getReportFiles(context, reportPathKey());
    reportFiles.forEach(report -> importExternalReport(report, context, unresolvedInputFiles));
    logUnresolvedInputFiles(unresolvedInputFiles);
  }

  private void importExternalReport(File reportPath, SensorContext context, Set<String> unresolvedInputFiles) {
    try {
      importReport(reportPath, context, unresolvedInputFiles);
    } catch (IOException | ParseException | RuntimeException e) {
      logFileCantBeRead(e, reportPath);
    }
  }

  private void logUnresolvedInputFiles(Set<String> unresolvedInputFiles) {
    if (unresolvedInputFiles.isEmpty()) {
      return;
    }
    String fileList = unresolvedInputFiles.stream().sorted().limit(MAX_LOGGED_FILE_NAMES).collect(Collectors.joining(";"));
    if (unresolvedInputFiles.size() > MAX_LOGGED_FILE_NAMES) {
      fileList += ";...";
    }
    logger().warn("Failed to resolve {} file path(s) in " + reportName() + " report. No issues imported related to file(s): {}", unresolvedInputFiles.size(), fileList);
  }

  private void logFileCantBeRead(Exception e, File reportPath) {
    logger().error("No issues information will be saved as the report file '{}' can't be read. {}: {}"
      , reportPath, e.getClass().getSimpleName(), e.getMessage());
  }

  protected abstract void importReport(File reportPath, SensorContext context, Set<String> unresolvedInputFiles) throws IOException, ParseException;

  protected abstract boolean shouldExecute(Configuration conf);

  protected abstract String reportName();

  protected abstract String reportPathKey();

  protected abstract Logger logger();

}
