/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.plugins.php.reports.phpunit;

import java.io.File;
import java.util.List;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.php.reports.AbstractReportImporter;
import org.sonar.plugins.php.reports.ExternalReportWildcardProvider;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;

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
    return String.format("An error occurred when reading report file '%s', nothing will be imported from this report. %s: %s", reportPath, e.getClass().getSimpleName(),
      e.getMessage());
  }

  public List<File> getReportFiles(SensorContext context) {
    return ExternalReportWildcardProvider.getReportFiles(context, reportPathKey());
  }

  protected void createWarning(String message, File file) {
    var warning = String.format(message, file.getAbsolutePath());
    logger().warn(warning);
    analysisWarningsWrapper.addWarning(warning);
  }

}
