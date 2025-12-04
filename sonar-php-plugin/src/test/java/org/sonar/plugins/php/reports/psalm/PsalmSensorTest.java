/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.reports.psalm;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.rules.RuleType;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.plugins.php.reports.ExternalIssuesSensor;
import org.sonar.plugins.php.reports.ReportSensorTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PsalmSensorTest extends ReportSensorTest {

  private static final String PSALM_PROPERTY = "sonar.php.psalm.reportPaths";
  private static final Path PROJECT_DIR = Paths.get("src", "test", "resources", "reports", "psalm");
  private final PsalmRulesDefinition psalmRulesDefinition = new PsalmRulesDefinition(SONAR_RUNTIME);
  private final PsalmSensor psalmSensor = new PsalmSensor(psalmRulesDefinition, analysisWarnings);

  @RegisterExtension
  public final LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @Test
  void testDescriptor() {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    psalmSensor.describe(sensorDescriptor);
    assertThat(sensorDescriptor.name()).isEqualTo("Import of Psalm issues");
    assertThat(sensorDescriptor.languages()).containsOnly("php");
    assertThat(sensorDescriptor.configurationPredicate()).isNotNull();
    assertNoErrorWarnDebugLogs(logTester);

    Path baseDir = PROJECT_DIR.getParent();
    SensorContextTester context = SensorContextTester.create(baseDir);
    context.settings().setProperty(PSALM_PROPERTY, "path/to/report");
    assertThat(sensorDescriptor.configurationPredicate().test(context.config())).isTrue();
  }

  @Test
  void raiseIssue() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("psalm-report.json");
    assertThat(externalIssues).hasSize(3);

    verifyPsalmReportIssue1(externalIssues.get(0));
    verifyPsalmReportIssue2(externalIssues.get(0), externalIssues.get(1));
    verifyPsalmReportIssue3(externalIssues.get(2));

    assertNoErrorWarnDebugLogs(logTester);
  }

  private static void verifyPsalmReportIssue1(ExternalIssue first) {
    assertThat(first.impacts()).containsOnly(entry(SoftwareQuality.RELIABILITY, Severity.HIGH));
    assertThat(first.type()).isEqualTo(RuleType.BUG);
    assertThat(first.severity()).isEqualTo(org.sonar.api.batch.rule.Severity.CRITICAL);
    assertThat(first.ruleId()).isEqualTo("InvalidScalarArgument");
    IssueLocation firstPrimaryLoc = first.primaryLocation();
    assertThat(firstPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:psalm/file1.php");
    assertThat(firstPrimaryLoc.message()).isEqualTo("Argument 1 of foo expects int, \"not an int\" provided");
    TextRange firstTextRange = firstPrimaryLoc.textRange();
    assertThat(firstTextRange).isNotNull();
    assertThat(firstTextRange.start().line()).isEqualTo(5);
    assertThat(firstTextRange.start().lineOffset()).isEqualTo(4);
    assertThat(firstTextRange.end().line()).isEqualTo(5);
    assertThat(firstTextRange.end().lineOffset()).isEqualTo(16);
  }

  private static void verifyPsalmReportIssue2(ExternalIssue first, ExternalIssue second) {
    assertThat(second.impacts()).containsOnly(entry(SoftwareQuality.RELIABILITY, Severity.HIGH));
    assertThat(second.type()).isEqualTo(RuleType.BUG);
    assertThat(second.severity()).isEqualTo(org.sonar.api.batch.rule.Severity.CRITICAL);
    assertThat(first.ruleId()).isEqualTo("InvalidScalarArgument");
    IssueLocation secondPrimaryLoc = second.primaryLocation();
    assertThat(secondPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:psalm/file1.php");
    assertThat(secondPrimaryLoc.message()).isEqualTo("Second issue on file1.php");
    TextRange secondTextRange = secondPrimaryLoc.textRange();
    assertThat(secondTextRange).isNotNull();
    assertThat(secondTextRange.start().line()).isEqualTo(2);
    assertThat(secondTextRange.start().lineOffset()).isEqualTo(1);
    assertThat(secondTextRange.end().line()).isEqualTo(2);
    assertThat(secondTextRange.end().lineOffset()).isEqualTo(10);
  }

  private static void verifyPsalmReportIssue3(ExternalIssue third) {
    assertThat(third.impacts()).containsOnly(entry(SoftwareQuality.RELIABILITY, Severity.MEDIUM));
    assertThat(third.type()).isEqualTo(RuleType.BUG);
    assertThat(third.severity()).isEqualTo(org.sonar.api.batch.rule.Severity.MAJOR);
    IssueLocation thirdPrimaryLoc = third.primaryLocation();
    assertThat(thirdPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:psalm/file2.php");
    assertThat(thirdPrimaryLoc.message()).isEqualTo("Argument 1 of foo expects int, \"not an int\" provided");
    TextRange thirdTextRange = thirdPrimaryLoc.textRange();
    assertThat(thirdTextRange).isNotNull();
    assertThat(thirdTextRange.start().line()).isEqualTo(5);
    assertThat(thirdTextRange.start().lineOffset()).isEqualTo(4);
    assertThat(thirdTextRange.end().line()).isEqualTo(5);
    assertThat(thirdTextRange.end().lineOffset()).isEqualTo(16);
  }

  @Test
  void raiseIssueFileHasUnixAbsolutePaths() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("psalm-report-abs.json");
    assertThat(externalIssues).hasSize(3);

    assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  void raiseIssueFileHasWindowsAbsolutePaths() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("psalm-report-abs_win.json");
    assertThat(externalIssues).hasSize(3);

    assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  void raiseIssueWithMissingFields() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("psalm-report-with-missing-fields.json");
    assertThat(externalIssues).hasSize(5);

    verifyIssue1(externalIssues.get(0));
    verifyIssue2(externalIssues.get(1));
    verifyIssue3(externalIssues.get(2));
    verifyIssue4(externalIssues.get(3));
    verifyIssue5(externalIssues.get(4));

    assertNoErrorWarnDebugLogs(logTester);
  }

  private static void verifyIssue1(ExternalIssue first) {
    assertThat(first.impacts()).containsOnly(entry(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM));
    assertThat(first.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(first.severity()).isEqualTo(org.sonar.api.batch.rule.Severity.MAJOR);
    assertThat(first.ruleId()).isEqualTo("InvalidScalarArgument");
    IssueLocation firstPrimaryLoc = first.primaryLocation();
    assertThat(firstPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:psalm/file1.php");
    assertThat(firstPrimaryLoc.message()).isEqualTo("Issue without type and severity");
    TextRange firstTextRange = firstPrimaryLoc.textRange();
    assertThat(firstTextRange).isNotNull();
    assertThat(firstTextRange.start().line()).isEqualTo(5);
    assertThat(firstTextRange.start().lineOffset()).isEqualTo(4);
    assertThat(firstTextRange.end().line()).isEqualTo(5);
    assertThat(firstTextRange.end().lineOffset()).isEqualTo(16);
  }

  private static void verifyIssue2(ExternalIssue second) {
    assertThat(second.impacts()).containsOnly(entry(SoftwareQuality.SECURITY, Severity.LOW));
    assertThat(second.type()).isEqualTo(RuleType.SECURITY_HOTSPOT);
    assertThat(second.severity()).isEqualTo(org.sonar.api.batch.rule.Severity.INFO);
    assertThat(second.ruleId()).isEqualTo("InvalidScalarArgument");
    IssueLocation secondPrimaryLoc = second.primaryLocation();
    assertThat(secondPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:psalm/file1.php");
    assertThat(secondPrimaryLoc.message()).isEqualTo("Issue without textRange");
    TextRange secondTextRange = secondPrimaryLoc.textRange();
    assertThat(secondTextRange).isNull();
  }

  private static void verifyIssue3(ExternalIssue third) {
    assertThat(third.impacts()).containsOnly(entry(SoftwareQuality.SECURITY, Severity.HIGH));
    assertThat(third.type()).isEqualTo(RuleType.VULNERABILITY);
    assertThat(third.severity()).isEqualTo(org.sonar.api.batch.rule.Severity.BLOCKER);
    assertThat(third.ruleId()).isEqualTo("InvalidScalarArgument");
    IssueLocation thirdPrimaryLoc = third.primaryLocation();
    assertThat(thirdPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:psalm/file1.php");
    assertThat(thirdPrimaryLoc.message()).isEqualTo("Issue without endColumns and endLine");
    TextRange thirdTextRange = thirdPrimaryLoc.textRange();
    assertThat(thirdTextRange).isNotNull();
    assertThat(thirdTextRange.start().line()).isEqualTo(2);
    assertThat(thirdTextRange.start().lineOffset()).isEqualTo(4);
    assertThat(thirdTextRange.end().line()).isEqualTo(2);
    assertThat(thirdTextRange.end().lineOffset()).isEqualTo(5);
  }

  private static void verifyIssue4(ExternalIssue fourth) {
    assertThat(fourth.impacts()).containsOnly(entry(SoftwareQuality.MAINTAINABILITY, Severity.LOW));
    assertThat(fourth.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(fourth.severity()).isEqualTo(org.sonar.api.batch.rule.Severity.MINOR);
    assertThat(fourth.ruleId()).isEqualTo("InvalidScalarArgument");
    IssueLocation fourthPrimaryLoc = fourth.primaryLocation();
    assertThat(fourthPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:psalm/file1.php");
    assertThat(fourthPrimaryLoc.message()).isEqualTo("Issue without columns and endLine");
    TextRange fourthTextRange = fourthPrimaryLoc.textRange();
    assertThat(fourthTextRange).isNotNull();
    assertThat(fourthTextRange.start().line()).isEqualTo(2);
    assertThat(fourthTextRange.start().lineOffset()).isZero();
    assertThat(fourthTextRange.end().line()).isEqualTo(2);
    assertThat(fourthTextRange.end().lineOffset()).isEqualTo(22);
  }

  private static void verifyIssue5(ExternalIssue fifth) {
    assertThat(fifth.impacts()).containsOnly(entry(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM));
    assertThat(fifth.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(fifth.severity()).isEqualTo(org.sonar.api.batch.rule.Severity.MAJOR);
    assertThat(fifth.ruleId()).isEqualTo("psalm.finding");
    IssueLocation fifthPrimaryLoc = fifth.primaryLocation();
    assertThat(fifthPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:psalm/file1.php");
    assertThat(fifthPrimaryLoc.message()).isEqualTo("Issue without ruleId");
    TextRange fifthTextRange = fifthPrimaryLoc.textRange();
    assertThat(fifthTextRange).isNotNull();
    assertThat(fifthTextRange.start().line()).isEqualTo(2);
    assertThat(fifthTextRange.start().lineOffset()).isEqualTo(1);
    assertThat(fifthTextRange.end().line()).isEqualTo(2);
    assertThat(fifthTextRange.end().lineOffset()).isEqualTo(10);
  }

  @Test
  void raiseIssueWithErrors() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("psalm-report-with-errors.json");
    assertThat(externalIssues).hasSize(2);

    ExternalIssue first = externalIssues.get(0);
    assertThat(first.ruleId()).isEqualTo("psalm.finding");
    IssueLocation firstPrimaryLoc = first.primaryLocation();
    assertThat(firstPrimaryLoc.message()).isEqualTo("Issue with to long startColumn and unknown ruleId");
    TextRange firstTextRange = firstPrimaryLoc.textRange();
    assertThat(firstTextRange).isNotNull();
    assertThat(firstTextRange.start().line()).isEqualTo(2);
    assertThat(firstTextRange.start().lineOffset()).isZero();
    assertThat(firstTextRange.end().line()).isEqualTo(2);
    assertThat(firstTextRange.end().lineOffset()).isEqualTo(22);

    ExternalIssue second = externalIssues.get(1);
    assertThat(second.ruleId()).isEqualTo("InvalidScalarArgument");
    IssueLocation secondPrimaryLoc = second.primaryLocation();
    assertThat(secondPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:psalm/file1.php");
    assertThat(secondPrimaryLoc.message()).isEqualTo("Issue with to long endColumn");
    TextRange secondTextRange = secondPrimaryLoc.textRange();
    assertThat(secondTextRange).isNotNull();
    assertThat(secondTextRange.start().line()).isEqualTo(2);
    assertThat(secondTextRange.start().lineOffset()).isEqualTo(1);
    assertThat(secondTextRange.end().line()).isEqualTo(2);
    assertThat(secondTextRange.end().lineOffset()).isEqualTo(2);

    assertThat(logTester.logs(Level.ERROR)).isEmpty();
    assertThat(logTester.logs(Level.DEBUG)).containsExactly(
      "Missing information for filePath:'psalm/file1.php', message:'  '",
      "Missing information for filePath:'psalm/file1.php', message:'null'",
      "Missing information for filePath:'null', message:'Issue without filePath'");
    assertThat(onlyOneLogElement(logTester().logs(Level.WARN))).isEqualTo(
      "Failed to resolve 1 file path(s) in Psalm psalm-report-with-errors.json report. No issues imported related to file(s): psalm/unknown.php");

    verify(analysisWarnings, times(1))
      .addWarning("Failed to resolve 1 file path(s) in Psalm psalm-report-with-errors.json report. No issues imported related to file(s): psalm/unknown.php");
  }

  @Test
  void excludedFilesWillNotBeLogged() throws IOException {
    executeSensorImporting("psalm-report-with-errors.json", Map.of("sonar.exclusion", "*/**/unknown.php"));

    assertThat(logTester.logs(Level.ERROR)).isEmpty();
    assertThat(logTester.logs(Level.WARN)).isEmpty();
    verify(analysisWarnings, never()).addWarning(anyString());
  }

  @Test
  void shouldLoadReportsByWildcard() throws IOException {
    var context = createContext(null, PROJECT_DIR, Map.of());
    context.settings().setProperty(PSALM_PROPERTY, "psalm/*.json");
    sensor().execute(context);

    var externalIssues = context.allExternalIssues();

    if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
      // the file psalm-report-abs_win.json is imported successfully and contains 3 additional issues
      assertThat(externalIssues).hasSize(16);
    } else {
      assertThat(externalIssues).hasSize(13);
    }
  }

  @Override
  protected Path projectDir() {
    return PROJECT_DIR;
  }

  @Override
  protected ExternalIssuesSensor sensor() {
    return psalmSensor;
  }

  @Override
  protected LogTesterJUnit5 logTester() {
    return logTester;
  }
}
