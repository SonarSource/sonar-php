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
package org.sonar.plugins.php.reports.phpstan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.plugins.php.reports.ExternalIssuesSensor;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

public class PhpStanSensor extends ExternalIssuesSensor {

  private static final Logger LOG = LoggerFactory.getLogger(PhpStanSensor.class);

  public static final String PHPSTAN_REPORT_KEY = "phpstan";
  public static final String PHPSTAN_REPORT_NAME = "PHPStan";
  public static final String PHPSTAN_REPORT_PATH_KEY = "sonar.php.phpstan.reportPaths";

  public PhpStanSensor(PhpStanRulesDefinition phpStanRulesDefinition, AnalysisWarningsWrapper analysisWarningsWrapper) {
    super(phpStanRulesDefinition, analysisWarningsWrapper);
  }

  @Override
  public void importReport(File report, SensorContext context) throws IOException, ParseException {
    InputStream in = new FileInputStream(report);
    LOG.info("Importing {}", report);
    PhpStanJsonReportReader.read(in, issue -> saveIssue(context, issue));
  }

  @Override
  public String reportName() {
    return PHPSTAN_REPORT_NAME;
  }

  @Override
  protected String reportKey() {
    return PHPSTAN_REPORT_KEY;
  }

  @Override
  public String reportPathKey() {
    return PHPSTAN_REPORT_PATH_KEY;
  }

  @Override
  public Logger logger() {
    return LOG;
  }
}
