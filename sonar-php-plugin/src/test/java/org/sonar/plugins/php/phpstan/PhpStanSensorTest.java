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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class PhpStanSensorTest {

  private static final String PHPSTAN_PROPERTY = "sonar.php.phpstan.reportPaths";
  private static final Path PROJECT_DIR = Paths.get("src", "test", "resources", "org", "sonar", "plugins", "php", "phpstan");

  private static PhpStanSensor phpStanSensor = new PhpStanSensor();

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
    List<ExternalIssue> externalIssues = executeSensorImporting("phpstan-report.json");
    assertThat(externalIssues).hasSize(3);

    ExternalIssue first = externalIssues.get(0);
    assertThat(first.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(first.severity()).isEqualTo(Severity.MAJOR);
    IssueLocation firstPrimaryLoc = first.primaryLocation();
    assertThat(firstPrimaryLoc.inputComponent().key()).isEqualTo("php-project:phpstan/file1.php");
    assertThat(firstPrimaryLoc.message())
      .isEqualTo("Parameter #1 $i of function foo expects int, string given.");
    TextRange firstTextRange = firstPrimaryLoc.textRange();
    assertThat(firstTextRange).isNotNull();
    assertThat(firstTextRange.start().line()).isEqualTo(5);

    ExternalIssue second = externalIssues.get(1);
    assertThat(second.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(second.severity()).isEqualTo(Severity.MAJOR);
    IssueLocation secondPrimaryLoc = second.primaryLocation();
    assertThat(secondPrimaryLoc.inputComponent().key()).isEqualTo("php-project:phpstan/file2.php");
    assertThat(secondPrimaryLoc.message())
      .isEqualTo("Parameter $date of method HelloWorld::sayHello() has invalid typehint type DateTimeImutable.");
    TextRange secondTextRange = secondPrimaryLoc.textRange();
    assertThat(secondTextRange).isNotNull();
    assertThat(secondTextRange.start().line()).isEqualTo(5);

    ExternalIssue third = externalIssues.get(2);
    assertThat(third.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(third.severity()).isEqualTo(Severity.MAJOR);
    IssueLocation thirdPrimaryLoc = third.primaryLocation();
    assertThat(thirdPrimaryLoc.inputComponent().key()).isEqualTo("php-project:phpstan/file2.php");
    assertThat(thirdPrimaryLoc.message())
      .isEqualTo("Call to method format() on an unknown class DateTimeImutable.");
    TextRange thirdTextRange = thirdPrimaryLoc.textRange();
    assertThat(thirdTextRange).isNotNull();
    assertThat(thirdTextRange.start().line()).isEqualTo(7);

    assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  public void no_issues_without_report_paths_property() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting(null);
    assertThat(externalIssues).isEmpty();
    assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  public void no_issues_with_invalid_report_path() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("invalid-path.txt");
    assertThat(externalIssues).isEmpty();
    assertThat(onlyOneLogElement(logTester.logs(LoggerLevel.ERROR)))
      .startsWith("No issues information will be saved as the report file '")
      .contains("invalid-path.txt' can't be read.");
  }

  @Test
  public void no_issues_with_invalid_phpstan_file() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("not-phpstan-report.json");
    assertThat(externalIssues).isEmpty();
    assertThat(onlyOneLogElement(logTester.logs(LoggerLevel.ERROR)))
      .startsWith("No issues information will be saved as the report file '")
      .contains("not-phpstan-report.json' can't be read.");
  }

  @Test
  public void no_issues_with_empty_phpstan_file() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("phpstan-report-empty.json");
    assertThat(externalIssues).isEmpty();
    assertNoErrorWarnDebugLogs(logTester);
  }

  @Test
  public void issues_when_phpstan_file_has_errors() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("phpstan-report-with-error.json");
    assertThat(externalIssues).hasSize(1);

    ExternalIssue first = externalIssues.get(0);
    assertThat(first.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(first.severity()).isEqualTo(Severity.MAJOR);
    IssueLocation firstPrimaryLoc = first.primaryLocation();
    assertThat(firstPrimaryLoc.inputComponent().key()).isEqualTo("php-project:phpstan/file1.php");
    assertThat(firstPrimaryLoc.message())
      .isEqualTo("Parameter #1 $i of function foo expects int, string given.");
    TextRange firstTextRange = firstPrimaryLoc.textRange();
    assertThat(firstTextRange).isNull();

    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
    assertThat(onlyOneLogElement(logTester.logs(LoggerLevel.WARN)))
      .isEqualTo("Failed to resolve 21 file path(s) in PHPStan report. No issues imported related to file(s): " +
        "phpstan/file10.php;phpstan/file11.php;phpstan/file12.php;phpstan/file13.php;phpstan/file14.php;" +
        "phpstan/file15.php;phpstan/file16.php;phpstan/file17.php;phpstan/file18.php;phpstan/file19.php;" +
        "phpstan/file20.php;phpstan/file21.php;phpstan/file22.php;phpstan/file23.php;phpstan/file24.php;" +
        "phpstan/file25.php;phpstan/file4.php;phpstan/file6.php;phpstan/file7.php;phpstan/file8.php;...");
    assertThat(logTester.logs(LoggerLevel.DEBUG)).containsExactly(
      "Missing information for filePath:'', message:'Parameter $date of method HelloWorld::sayHello() has invalid typehint type DateTimeImutable.'",
      "Missing information for filePath:'phpstan/file3.php', message:''"
    );
  }

  @Test
  public void issues_when_phpstan_line_errors() throws IOException {
    List<ExternalIssue> externalIssues = executeSensorImporting("phpstan-report-with-line-and-message-error.json");
    assertThat(externalIssues).isEmpty();

    assertThat(onlyOneLogElement(logTester.logs(LoggerLevel.ERROR)))
      .contains("100 is not a valid line for pointer. File phpstan/file1.php has 6 line(s)");
    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
    assertThat(logTester.logs(LoggerLevel.DEBUG)).isEmpty();
  }

  private static List<ExternalIssue> executeSensorImporting(@Nullable String fileName) throws IOException {
    Path baseDir = PROJECT_DIR.getParent();
    SensorContextTester context = SensorContextTester.create(baseDir);
    try (Stream<Path> fileStream = Files.list(PROJECT_DIR)) {
      fileStream.forEach(file -> addFileToContext(context, baseDir, file));
      context.setRuntime(SonarRuntimeImpl.forSonarQube(Version.create(8, 9), SonarQubeSide.SERVER, SonarEdition.DEVELOPER));
      if (fileName != null) {
        String path = PROJECT_DIR.resolve(fileName).toAbsolutePath().toString();
        context.settings().setProperty("sonar.php.phpstan.reportPaths", path);
      }
      phpStanSensor.execute(context);
      return new ArrayList<>(context.allExternalIssues());
    }
  }

  private static void addFileToContext(SensorContextTester context, Path projectDir, Path file) {
    try {
      String projectId = projectDir.getFileName().toString() + "-project";
      context.fileSystem().add(TestInputFileBuilder.create(projectId, projectDir.toFile(), file.toFile())
        .setCharset(UTF_8)
        .setLanguage(language(file))
        .setContents(new String(Files.readAllBytes(file), UTF_8))
        .setType(InputFile.Type.MAIN)
        .build());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String language(Path file) {
    String path = file.toString();
    return path.substring(path.lastIndexOf('.') + 1);
  }

  public static String onlyOneLogElement(List<String> elements) {
    assertThat(elements).hasSize(1);
    return elements.get(0);
  }

  public static void assertNoErrorWarnDebugLogs(LogTester logTester) {
    org.assertj.core.api.Assertions.assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
    org.assertj.core.api.Assertions.assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
    org.assertj.core.api.Assertions.assertThat(logTester.logs(LoggerLevel.DEBUG)).isEmpty();
  }
}
