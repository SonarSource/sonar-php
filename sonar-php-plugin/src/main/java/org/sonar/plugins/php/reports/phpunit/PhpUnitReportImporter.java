/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.plugins.php.reports.phpunit;

import java.io.File;
import java.util.List;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.php.reports.AbstractReportImporter;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.ExternalReportProvider;

public abstract class PhpUnitReportImporter extends AbstractReportImporter {

  private static final String UNRESOLVED_INPUT_FILE_MESSAGE_FORMAT = "Failed to resolve %s file path(s) in %s %s report. Nothing is imported related to file(s): %s";

  protected PhpUnitReportImporter(AnalysisWarningsWrapper analysisWarningsWrapper) {
    super((analysisWarningsWrapper));
  }

  @Override
  public final void execute(SensorContext context) {
    super.execute(context);
    if (getReportFiles(context).isEmpty()) {
      logger().info("No {} reports provided (see '{}' property)", reportName(), reportPathKey());
    }
  }

  public String getUnresolvedInputFileMessageFormat() {
    return UNRESOLVED_INPUT_FILE_MESSAGE_FORMAT;
  }

  public String getFileReadErrorMessage(Exception e, File reportPath) {
    return String.format("An error occurred when reading report file '%s', nothing will be imported from this report. %s: %s"
      , reportPath, e.getClass().getSimpleName(), e.getMessage());
  }

  public List<File> getReportFiles(SensorContext context) {
    return ExternalReportProvider.getReportFiles(context, reportPathKey());
  }

}
