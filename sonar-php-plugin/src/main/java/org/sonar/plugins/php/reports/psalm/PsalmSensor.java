/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.php.reports.psalm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.sonar.api.batch.sensor.SensorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;
import org.sonar.plugins.php.reports.ExternalIssuesSensor;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

public class PsalmSensor extends ExternalIssuesSensor {

  private static final Logger LOG = LoggerFactory.getLogger(PsalmSensor.class);

  public static final String PSALM_REPORT_KEY = "psalm";
  public static final String PSALM_REPORT_NAME = "Psalm";
  public static final String PSALM_REPORT_PATH_KEY = "sonar.php.psalm.reportPaths";

  public PsalmSensor(AnalysisWarningsWrapper analysisWarningsWrapper) {
    super(analysisWarningsWrapper);
  }

  @Override
  public void importReport(File report, SensorContext context) throws IOException, ParseException {
    InputStream in = new FileInputStream(report);
    LOG.info("Importing {}", report);
    PsalmJsonReportReader.read(in, issue -> saveIssue(context, issue));
  }

  @Override
  public String reportName() {
    return PSALM_REPORT_NAME;
  }

  @Override
  protected String reportKey() {
    return PSALM_REPORT_KEY;
  }

  @Override
  public String reportPathKey() {
    return PSALM_REPORT_PATH_KEY;
  }

  @Override
  public Logger logger() {
    return LOG;
  }

  @Override
  protected ExternalRuleLoader externalRuleLoader() {
    return PsalmRulesDefinition.RULE_LOADER;
  }
}
