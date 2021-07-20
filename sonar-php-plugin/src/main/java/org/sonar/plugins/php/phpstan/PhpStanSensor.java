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
package org.sonar.plugins.php.phpstan;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.php.ExternalIssuesSensor;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

public class PhpStanSensor extends ExternalIssuesSensor {

  private static final Logger LOG = Loggers.get(PhpStanSensor.class);

  public static final String REPORT_KEY = "phpstan";
  public static final String REPORT_NAME = "PHPStan";
  public static final String REPORT_PATH_KEY = "sonar.php.phpstan.reportPaths";
  public static final String DEFAULT_RULE_ID = "phpstan.finding";

  @Override
  protected void importReport(File reportPath, SensorContext context, Set<String> unresolvedInputFiles) throws IOException, ParseException {
    InputStream in = new FileInputStream(reportPath);
    LOG.info("Importing {}", reportPath);
    PhpStanJsonReportReader.read(in, issue -> saveIssue(context, issue, unresolvedInputFiles));
  }

  private static void saveIssue(SensorContext context, PhpStanJsonReportReader.Issue issue, Set<String> unresolvedInputFiles) {
    if (isEmpty(issue.filePath) || isEmpty(issue.message)) {
      LOG.debug("Missing information for filePath:'{}', message:'{}'", issue.filePath, issue.message);
      return;
    }

    InputFile inputFile = context.fileSystem().inputFile(context.fileSystem().predicates().hasPath(issue.filePath));
    if (inputFile == null) {
      unresolvedInputFiles.add(issue.filePath);
      return;
    }

    NewExternalIssue newExternalIssue = context.newExternalIssue();
    newExternalIssue
      .type(RuleType.CODE_SMELL)
      .severity(Severity.MAJOR)
      .remediationEffortMinutes(DEFAULT_CONSTANT_DEBT_MINUTES);

    NewIssueLocation primaryLocation = newExternalIssue.newLocation()
      .message(issue.message)
      .on(inputFile);
    if (issue.lineNumber != null) {
      primaryLocation.at(inputFile.selectLine(issue.lineNumber));
    }
    newExternalIssue.at(primaryLocation);

    newExternalIssue.engineId(REPORT_KEY).ruleId(DEFAULT_RULE_ID);
    newExternalIssue.save();
  }

  private static boolean isEmpty(@Nullable String str) {
    return str == null || str.length() == 0;
  }

  @Override
  protected boolean shouldExecute(Configuration conf) {
    return conf.hasKey(REPORT_PATH_KEY);
  }

  @Override
  protected String reportName() {
    return REPORT_NAME;
  }

  @Override
  protected String reportPathKey() {
    return REPORT_PATH_KEY;
  }

  @Override
  protected Logger logger() {
    return LOG;
  }
}
