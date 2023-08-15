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
package org.sonar.plugins.php.reports;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rules.RuleType;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.ExternalReportProvider;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

public abstract class ExternalIssuesSensor extends AbstractReportImporter implements Sensor {
  protected static final Long DEFAULT_CONSTANT_DEBT_MINUTES = 5L;

  private static final RuleType DEFAULT_RULE_TYPE = RuleType.CODE_SMELL;
  private static final Severity DEFAULT_SEVERITY = Severity.MAJOR;
  private static final String READ_ERROR_MSG_FORMAT = "An error occurred when reading report file '%s', no issue will be imported from this report.\n%s";

  private static final String UNRESOLVED_INPUT_FILE_MESSAGE_FORMAT = "Failed to resolve %s file path(s) in %s %s report. No issues imported related to file(s): %s";
  public final String defaultRuleId = reportKey() + ".finding";

  protected ExternalIssuesSensor(AnalysisWarningsWrapper analysisWarningsWrapper) {
    super(analysisWarningsWrapper);
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyWhenConfiguration(this::shouldExecute)
      .onlyOnLanguage(Php.KEY)
      .name("Import of " + reportName() + " issues");
  }

  @Override
  public List<File> getReportFiles(SensorContext context) {
    return ExternalReportProvider.getReportFiles(context, reportPathKey());
  }

  public String getUnresolvedInputFileMessageFormat() {
    return UNRESOLVED_INPUT_FILE_MESSAGE_FORMAT;
  }

  public String getFileReadErrorMessage(Exception e, File reportPath) {
    String additionalMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
    if (e instanceof ParseException || e instanceof ClassCastException) {
      additionalMsg = "The content of the file probably does not have the expected format.";
    } else if (e instanceof FileNotFoundException) {
      additionalMsg = "The file was not found.";
    }

    return String.format(READ_ERROR_MSG_FORMAT, reportPath, additionalMsg);
  }

  private static boolean isEmpty(@Nullable String str) {
    return str == null || str.trim().length() == 0;
  }

  @CheckForNull
  private InputFile inputFile(SensorContext context, String filePath) {
    String relativePath = fileHandler.relativePath(filePath);
    return context.fileSystem().inputFile(context.fileSystem().predicates().hasPath(relativePath));
  }

  protected void saveIssue(SensorContext context, JsonReportReader.Issue issue) {
    if (isEmpty(issue.filePath) || isEmpty(issue.message)) {
      logger().debug("Missing information for filePath:'{}', message:'{}'", issue.filePath, issue.message);
      return;
    }

    InputFile inputFile = inputFile(context, issue.filePath);
    if (inputFile == null) {
      addUnresolvedInputFile(issue.filePath);
      return;
    }

    NewExternalIssue newExternalIssue = context.newExternalIssue();
    newExternalIssue
      .type(toType(issue.type))
      .severity(toSeverity(issue.severity))
      .remediationEffortMinutes(DEFAULT_CONSTANT_DEBT_MINUTES);

    NewIssueLocation primaryLocation = newExternalIssue.newLocation()
      .message(issue.message)
      .on(inputFile);

    refinePrimaryLocation(primaryLocation, issue, inputFile);

    newExternalIssue.at(primaryLocation);

    newExternalIssue.engineId(reportKey()).ruleId(toRuleId(issue.ruleId));
    newExternalIssue.save();
  }

  private static RuleType toType(@Nullable String type) {
    if (type != null) {
      switch (type) {
        case "BUG":
          return RuleType.BUG;
        case "SECURITY_HOTSPOT":
          return RuleType.SECURITY_HOTSPOT;
        case "VULNERABILITY":
          return RuleType.VULNERABILITY;
        case "CODE_SMELL":
          return RuleType.CODE_SMELL;
      }
    }
    return DEFAULT_RULE_TYPE;
  }

  private static Severity toSeverity(@Nullable String severity) {
    if (severity != null) {
      switch (severity) {
        case "INFO":
          return Severity.INFO;
        case "MINOR":
          return Severity.MINOR;
        case "MAJOR":
          return Severity.MAJOR;
        case "CRITICAL":
          return Severity.CRITICAL;
        case "BLOCKER":
          return Severity.BLOCKER;
      }
    }
    return DEFAULT_SEVERITY;
  }

  private String toRuleId(@Nullable String ruleId) {
    return ruleId != null && externalRuleLoader().ruleKeys().contains(ruleId) ? ruleId : defaultRuleId;
  }

  private static void refinePrimaryLocation(NewIssueLocation primaryLocation, JsonReportReader.Issue issue, InputFile inputFile) {
    if (issue.startLine == null) {
      return;
    }
    if (issue.startColumn != null && issue.startColumn < inputFile.selectLine(issue.startLine).end().lineOffset()) {
      int endLine = issue.startLine;
      int endColumn = issue.startColumn + 1;
      if (issue.endLine != null && issue.endColumn != null && issue.endColumn <= inputFile.selectLine(issue.endLine).end().lineOffset()) {
        endLine = issue.endLine;
        endColumn = issue.endColumn;
      }
      primaryLocation.at(inputFile.newRange(issue.startLine, issue.startColumn, endLine, endColumn));
    } else {
      primaryLocation.at(inputFile.selectLine(issue.startLine));
    }
  }

  protected boolean shouldExecute(Configuration conf) {
    return conf.hasKey(reportPathKey());
  }

  protected abstract String reportKey();

  protected abstract ExternalRuleLoader externalRuleLoader();

}
