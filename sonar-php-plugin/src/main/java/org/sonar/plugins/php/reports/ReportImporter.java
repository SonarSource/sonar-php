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
import java.util.List;
import org.slf4j.Logger;
import org.sonar.api.batch.sensor.SensorContext;

public interface ReportImporter {

  int MAX_LOGGED_FILE_NAMES = 5;

  void execute(SensorContext context);

  String reportPathKey();

  String reportName();

  Logger logger();

  List<File> getReportFiles(SensorContext context);

  void importReport(File report, SensorContext context) throws Exception;

  String getFileReadErrorMessage(Exception e, File reportPath);

  String getUnresolvedInputFileMessageFormat();

}
