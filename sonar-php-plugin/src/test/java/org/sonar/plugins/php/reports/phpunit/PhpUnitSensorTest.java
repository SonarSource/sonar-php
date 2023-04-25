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

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Test;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.PhpTestUtils.inputFile;

public class PhpUnitSensorTest {

  @org.junit.Rule
  public LogTester logTester = new LogTester();
  private final List<String> analysisWarnings = new ArrayList<>();
  private final AnalysisWarningsWrapper analysisWarningsWrapper = analysisWarnings::add;
  private final SensorContextTester context = SensorContextTester.create(new File("src/test/resources").getAbsoluteFile());
  private static final SonarRuntime SONARQUBE_9_9 = SonarRuntimeImpl.forSonarQube(Version.create(9,9), SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);
  private final Set<File> tempReports = new HashSet<>();

  private static final String EXPECTED_MESSAGE = "PHPUnit test cases are detected. Make sure to specify test sources via `sonar.test` to get more precise analysis results.";

  @Test
  public void shouldProvideDescription() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    createSensor().describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("PHPUnit report sensor");
    assertThat(descriptor.languages()).containsOnly("php");
    assertThat(descriptor.type()).isNull();
  }

  @Test
  public void shouldLogMessageWhenNoReportsAreProvided() {
    createSensor().execute(context);
    assertThat(logTester.logs()).contains(
      "No PHPUnit tests reports provided (see 'sonar.php.tests.reportPath' property)",
      "No PHPUnit coverage reports provided (see 'sonar.php.coverage.reportPaths' property)");
    assertThat(analysisWarnings).isEmpty();
  }

  @Test
  public void shouldUseMultiPathCoverage() throws IOException {
    context.setRuntime(SONARQUBE_9_9);

    List<String> reportPaths = List.of(
      PhpTestUtils.GENERATED_UT_COVERAGE_REPORT_RELATIVE_PATH,
      PhpTestUtils.GENERATED_IT_COVERAGE_REPORT_RELATIVE_PATH,
      // should not fail with empty path, it should be ignored
      " ",
      PhpTestUtils.GENERATED_OVERALL_COVERAGE_REPORT_RELATIVE_PATH
    );

    context.settings().setProperty("sonar.php.coverage.reportPaths", String.join(", ", reportPaths));

    DefaultInputFile inputFile = inputFile("src/App.php");

    createReportWithAbsolutePath(PhpTestUtils.GENERATED_UT_COVERAGE_REPORT_RELATIVE_PATH, PhpTestUtils.UT_COVERAGE_REPORT_RELATIVE_PATH, inputFile);
    createReportWithAbsolutePath(PhpTestUtils.GENERATED_IT_COVERAGE_REPORT_RELATIVE_PATH, PhpTestUtils.IT_COVERAGE_REPORT_RELATIVE_PATH, inputFile);
    createReportWithAbsolutePath(PhpTestUtils.GENERATED_OVERALL_COVERAGE_REPORT_RELATIVE_PATH, PhpTestUtils.OVERALL_COVERAGE_REPORT_RELATIVE_PATH, inputFile);

    String mainFileKey = inputFile.key();
    context.fileSystem().add(inputFile);

    createSensor().execute(context);

    assertThat(context.lineHits(mainFileKey, 3)).isEqualTo(3);
    assertThat(context.lineHits(mainFileKey, 6)).isEqualTo(2);
    assertThat(context.lineHits(mainFileKey, 7)).isEqualTo(1);

    assertThat(logTester.logs(LoggerLevel.ERROR)).hasSize(1);
    String resourcesFolder = FilenameUtils.separatorsToSystem("/src/test/resources");
    assertThat(logTester.logs(LoggerLevel.ERROR).get(0))
      .startsWith("An error occurred when reading report file")
      .contains(resourcesFolder + "', nothing will be imported from this report.");
  }

  @Test
  public void shouldLogWarningWhenTestCaseIsDetectedWithoutDeclaration() {
    Set.of("src/App.php", "src/EmailTest.php").forEach(
      file -> context.fileSystem().add(inputFile(file))
    );

    Sensor sensor = createSensor();
    sensor.execute(context);

    assertThat(logTester.logs(LoggerLevel.WARN)).contains(EXPECTED_MESSAGE);
    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.DEBUG).get(0))
      .startsWith("Detected and undeclared test case in").endsWith("src/EmailTest.php");
  }

  @Test
  public void shouldNotLogWarningWhenTestCaseIsDetectedWithDeclaration() {
    Set.of("src/App.php", "src/EmailTest.php").forEach(
      file -> context.fileSystem().add(inputFile(file))
    );

    context.settings().setProperty("sonar.tests", "src");

    Sensor sensor = createSensor();
    sensor.execute(context);

    assertThat(logTester.logs(LoggerLevel.WARN)).doesNotContain(EXPECTED_MESSAGE);
    assertThat(logTester.logs(LoggerLevel.DEBUG)).isEmpty();
  }

  @Test
  public void shouldNotLogWarningWhenNoTestCaseIsDetectedWithoutDeclaration() {
    context.fileSystem().add(inputFile("src/App.php"));

    Sensor sensor = createSensor();
    sensor.execute(context);

    assertThat(logTester.logs(LoggerLevel.WARN)).doesNotContain(EXPECTED_MESSAGE);
  }

  @Test
  public void shouldNotCrashWhenReadingInvalidFile() throws IOException {
    InputFile corruptInputFile = spy(inputFile("src/App.php"));
    when(corruptInputFile.inputStream()).thenThrow(IOException.class);
    context.fileSystem().add(corruptInputFile);

    Sensor sensor = createSensor();
    sensor.execute(context);

    assertThat(logTester.logs(LoggerLevel.WARN)).doesNotContain(EXPECTED_MESSAGE);
    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(1);
    assertThat(logTester.logs(LoggerLevel.DEBUG).get(0))
      .startsWith("Can not read file").endsWith("src/App.php");
  }

  private PhpUnitSensor createSensor() {
    return new PhpUnitSensor(analysisWarningsWrapper);
  }

  /**
   * Creates a file name with absolute path in coverage report.
   * This hack allow to have this unit test, as only absolute path
   * in report is supported.
   * */
  private void createReportWithAbsolutePath(String generatedReportRelativePath, String relativeReportPath, InputFile inputFile) throws IOException {
    File tempReport = new File(context.fileSystem().baseDir(), generatedReportRelativePath);
    if (tempReport.createNewFile()) {
      File originalReport = new File(context.fileSystem().baseDir(), relativeReportPath);

      String content = Files.readLines(originalReport, StandardCharsets.UTF_8)
        .stream()
        .collect(Collectors.joining("\n"))
        .replace(inputFile.relativePath(), inputFile.absolutePath());

      Files.asCharSink(tempReport, StandardCharsets.UTF_8).write(content);
    }
    tempReports.add(tempReport);
  }

  @After
  public void tearDown() {
    tempReports.forEach(File::deleteOnExit);
  }
}
