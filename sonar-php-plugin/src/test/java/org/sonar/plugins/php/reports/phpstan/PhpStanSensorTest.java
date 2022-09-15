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
package org.sonar.plugins.php.reports.phpstan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.plugins.php.reports.ExternalIssuesSensor;
import org.sonar.plugins.php.reports.ReportSensorTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PhpStanSensorTest extends ReportSensorTest {

  private static final String PHPSTAN_PROPERTY = "sonar.php.phpstan.reportPaths";
  private static final Path PROJECT_DIR = Paths.get("src", "test", "resources", "org", "sonar", "plugins", "php", "reports", "phpstan");
  private static final char SEPARATOR_CHAR = File.separatorChar;
  protected final PhpStanSensor phpStanSensor = new PhpStanSensor(analysisWarnings);

  @Rule
  public LogTester logTester = new LogTester();

  @Test
  public void test_descriptor() {
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    phpStanSensor.describe(sensorDescriptor);
    assertThat(sensorDescriptor.name()).isEqualTo("Import of PHPStan issues");
    assertThat(sensorDescriptor.languages()).containsOnly("php");
    assertThat(sensorDescriptor.configurationPredicate()).isNotNull();
    assertNoErrorWarnDebugLogs(logTester);

    Path baseDir = PROJECT_DIR.getParent();
    SensorContextTester context = SensorContextTester.create(baseDir);
    context.settings().setProperty(PHPSTAN_PROPERTY, "path/to/report");
    assertThat(sensorDescriptor.configurationPredicate().test(context.config())).isTrue();
  }

  @Test
  public void raise_issue() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting(reportPath("phpstan-report.json"));
    assertThat(externalIssues).hasSize(3);

    List<Integer> orderOfIssues = SEPARATOR_CHAR == '\\' ? List.of(2, 0, 1) : List.of(0, 1, 2);
    ExternalIssue first = externalIssues.get(orderOfIssues.get(0));
    assertThat(first.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(first.severity()).isEqualTo(Severity.MAJOR);
    IssueLocation firstPrimaryLoc = first.primaryLocation();
    assertThat(firstPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:phpstan/file1.php");
    assertThat(firstPrimaryLoc.message())
      .isEqualTo("Parameter #1 $i of function foo expects int, string given.");
    TextRange firstTextRange = firstPrimaryLoc.textRange();
    assertThat(firstTextRange).isNotNull();
    assertThat(firstTextRange.start().line()).isEqualTo(5);

    ExternalIssue second = externalIssues.get(orderOfIssues.get(1));
    assertThat(second.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(second.severity()).isEqualTo(Severity.MAJOR);
    IssueLocation secondPrimaryLoc = second.primaryLocation();
    assertThat(secondPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:phpstan/file2.php");
    assertThat(secondPrimaryLoc.message())
      .isEqualTo("Parameter $date of method HelloWorld::sayHello() has invalid typehint type DateTimeImutable.");
    TextRange secondTextRange = secondPrimaryLoc.textRange();
    assertThat(secondTextRange).isNotNull();
    assertThat(secondTextRange.start().line()).isEqualTo(5);

    ExternalIssue third = externalIssues.get(orderOfIssues.get(2));
    assertThat(third.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(third.severity()).isEqualTo(Severity.MAJOR);
    IssueLocation thirdPrimaryLoc = third.primaryLocation();
    assertThat(thirdPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:phpstan/file2.php");
    assertThat(thirdPrimaryLoc.message())
      .isEqualTo("Call to method format() on an unknown class DateTimeImutable.");
    TextRange thirdTextRange = thirdPrimaryLoc.textRange();
    assertThat(thirdTextRange).isNotNull();
    assertThat(thirdTextRange.start().line()).isEqualTo(7);

    assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  public void issues_when_phpstan_file_has_fqn_paths() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting(reportPath("phpstan-report-fqn.json"));
    assertThat(externalIssues).hasSize(3);

    assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  public void issues_when_phpstan_file_has_errors() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting(reportPath("phpstan-report-with-error.json"));
    assertThat(externalIssues).hasSize(1);

    ExternalIssue first = externalIssues.get(0);
    assertThat(first.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(first.severity()).isEqualTo(Severity.MAJOR);
    IssueLocation firstPrimaryLoc = first.primaryLocation();
    assertThat(firstPrimaryLoc.inputComponent().key()).isEqualTo("reports-project:phpstan/file1.php");
    assertThat(firstPrimaryLoc.message())
      .isEqualTo("Parameter #1 $i of function foo expects int, string given.");
    TextRange firstTextRange = firstPrimaryLoc.textRange();
    assertThat(firstTextRange).isNull();

    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
    assertThat(onlyOneLogElement(logTester.logs(LoggerLevel.WARN))).isEqualTo("Failed to resolve 22 file path(s) in PHPStan "+reportPath("phpstan-report-with-error.json")+" report. " +
      "No issues imported related to file(s): "+osIndependentPath("phpstan/notExistingFile1.php;phpstan/notExistingFile10.php;phpstan/notExistingFile11.php;phpstan/notExistingFile12.php;phpstan/notExistingFile13.php;..."));
    assertThat(onlyOneLogElement(logTester.logs(LoggerLevel.DEBUG)))
      .isEqualTo("Missing information for filePath:'', message:'Parameter $date of method HelloWorld::sayHello() has invalid typehint type DateTimeImutable.'");

    verify(analysisWarnings, times(1))
      .addWarning("Failed to resolve 22 file path(s) in PHPStan "+reportPath("phpstan-report-with-error.json")+" report. " +
        "No issues imported related to file(s): "+osIndependentPath("phpstan/notExistingFile1.php;phpstan/notExistingFile10.php;phpstan/notExistingFile11.php;phpstan/notExistingFile12.php;phpstan/notExistingFile13.php;..."));
  }

  @Test
  public void excluded_files_will_not_be_logged() throws IOException {
    executeSensorImporting("phpstan-report-with-error.json", Map.of("sonar.exclusion", "*/**/notExisting*.php"));

    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
    verify(analysisWarnings, never()).addWarning(anyString());
  }

  @Test
  public void issues_when_phpstan_with_line_and_message_errors() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("phpstan-report-with-line-and-message-error.json");
    assertThat(externalIssues).isEmpty();

    assertThat(onlyOneLogElement(logTester.logs(LoggerLevel.ERROR)))
      .contains("100 is not a valid line for pointer. File phpstan/file2.php has 10 line(s)");
    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
    assertThat(logTester.logs(LoggerLevel.DEBUG)).containsExactly(
      "Missing information for filePath:'phpstan/file2.php', message:'null'",
      "Missing information for filePath:'phpstan/file2.php', message:''");
  }

  @Test
  public void no_object_as_root() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("no-object-as-root.php");
    assertThat(externalIssues).isEmpty();
    assertThat(onlyOneLogElement(logTester().logs(LoggerLevel.ERROR)))
      .startsWith("An error occurred when reading report file '")
      .contains("no issue will be imported from this report.\nThe content of the file probably does not have the expected format.");
  }

  @Test
  public void report_without_issue() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("phpstan-report-no-issue.json");
    assertThat(externalIssues).isEmpty();
    assertThat(logTester().logs(LoggerLevel.ERROR)).isEmpty();
  }

  @Test
  public void file_path_is_cleaned_when_it_contains_additional_context() throws Exception {
    List<ExternalIssue> externalIssues = executeSensorImporting("phpstan-with-context-in-path.json");
    assertThat(externalIssues).hasSize(1);
    assertThat(externalIssues.get(0).primaryLocation().inputComponent().key()).isEqualTo("reports-project:phpstan/file3.php");
  }

  @Override
  protected Path projectDir() {
    return PROJECT_DIR;
  }

  @Override
  protected ExternalIssuesSensor sensor() {
    return phpStanSensor;
  }

  @Override
  protected LogTester logTester() {
    return logTester;
  }

  private static String osIndependentPath(String path){
    if (SEPARATOR_CHAR == '\\'){
      path =  path.replace("/", "\\");
    }
    return path;
  }

  private static String reportPath(String path){
    if (SEPARATOR_CHAR == '\\'){
      path =  path.replace("/", "\\");
      StringBuilder builder = new StringBuilder(path);
      int index = builder.lastIndexOf(".");
      builder.replace(index, index+1, "_win.");
      return builder.toString();
    }
    return path;
  }
}
