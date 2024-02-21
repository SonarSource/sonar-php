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
package org.sonar.plugins.php.reports.phpunit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.api.utils.Version;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.PhpTestUtils.inputFile;

class PhpUnitSensorTest {

  @RegisterExtension
  public final LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  private final List<String> analysisWarnings = new ArrayList<>();
  private final AnalysisWarningsWrapper analysisWarningsWrapper = analysisWarnings::add;
  private final SensorContextTester context = SensorContextTester.create(new File("src/test/resources").getAbsoluteFile());
  private static final SonarRuntime SONARQUBE_9_9 = SonarRuntimeImpl.forSonarQube(Version.create(9, 9), SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);
  @TempDir
  private Path reportsDir;

  private static final String EXPECTED_MESSAGE = "PHPUnit test cases are detected. Make sure to specify test sources via `sonar.test` to get more precise analysis results.";

  @Test
  void shouldProvideDescription() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    createSensor().describe(descriptor);

    assertThat(descriptor.name()).isEqualTo("PHPUnit report sensor");
    assertThat(descriptor.languages()).containsOnly("php");
    assertThat(descriptor.type()).isNull();
  }

  @Test
  void shouldLogMessageWhenNoReportsAreProvided() {
    createSensor().execute(context);
    assertThat(logTester.logs()).contains(
      "No PHPUnit tests reports provided (see 'sonar.php.tests.reportPath' property)",
      "No PHPUnit coverage reports provided (see 'sonar.php.coverage.reportPaths' property)");
    assertThat(analysisWarnings).isEmpty();
  }

  @Test
  void shouldUseMultiPathCoverage() throws IOException {
    context.setRuntime(SONARQUBE_9_9);

    List<String> reportPaths = Stream.of(
      PhpTestUtils.GENERATED_UT_COVERAGE_REPORT_RELATIVE_PATH,
      PhpTestUtils.GENERATED_IT_COVERAGE_REPORT_RELATIVE_PATH,
      // should not fail with empty path, it should be ignored
      "",
      PhpTestUtils.GENERATED_OVERALL_COVERAGE_REPORT_RELATIVE_PATH)
      .map(f -> reportsDir.resolve(f).toAbsolutePath().toString())
      .collect(Collectors.toList());

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

    assertThat(logTester.logs(Level.ERROR)).hasSize(1);
    assertThat(logTester.logs(Level.ERROR).get(0))
      .startsWith("An error occurred when reading report file")
      .contains(reportsDir + "', nothing will be imported from this report.");
  }

  @Test
  void shouldLogWarningWhenTestCaseIsDetectedWithoutDeclaration() {
    Set.of("src/App.php", "src/EmailTest.php").forEach(
      file -> context.fileSystem().add(inputFile(file)));

    Sensor sensor = createSensor();
    sensor.execute(context);

    assertThat(logTester.logs(Level.WARN)).contains(EXPECTED_MESSAGE);
    assertThat(logTester.logs(Level.DEBUG)).hasSize(1);
    assertThat(logTester.logs(Level.DEBUG).get(0))
      .startsWith("Detected and undeclared test case in").endsWith("src/EmailTest.php");
  }

  @Test
  void shouldNotLogWarningWhenTestCaseIsDetectedWithDeclaration() {
    Set.of("src/App.php", "src/EmailTest.php").forEach(
      file -> context.fileSystem().add(inputFile(file)));

    context.settings().setProperty("sonar.tests", "src");

    Sensor sensor = createSensor();
    sensor.execute(context);

    assertThat(logTester.logs(Level.WARN)).doesNotContain(EXPECTED_MESSAGE);
    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }

  @Test
  void shouldNotLogWarningWhenNoTestCaseIsDetectedWithoutDeclaration() {
    context.fileSystem().add(inputFile("src/App.php"));

    Sensor sensor = createSensor();
    sensor.execute(context);

    assertThat(logTester.logs(Level.WARN)).doesNotContain(EXPECTED_MESSAGE);
  }

  @Test
  void shouldNotCrashWhenReadingInvalidFile() throws IOException {
    InputFile corruptInputFile = spy(inputFile("src/App.php"));
    when(corruptInputFile.inputStream()).thenThrow(IOException.class);
    context.fileSystem().add(corruptInputFile);

    Sensor sensor = createSensor();
    sensor.execute(context);

    assertThat(logTester.logs(Level.WARN)).doesNotContain(EXPECTED_MESSAGE);
    assertThat(logTester.logs(Level.DEBUG)).hasSize(1);
    assertThat(logTester.logs(Level.DEBUG).get(0))
      .startsWith("Can not read file").endsWith("src/App.php");
  }

  private PhpUnitSensor createSensor() {
    return new PhpUnitSensor(analysisWarningsWrapper);
  }

  /**
   * Creates a file name with absolute path in coverage report.
   * This hack allow to have this unit test, as only absolute path
   * in report is supported.
   */
  private void createReportWithAbsolutePath(String generatedReportRelativePath, String relativeReportPath, InputFile inputFile) throws IOException {
    Path tempReport = reportsDir.resolve(generatedReportRelativePath);
    Files.createDirectories(tempReport.getParent());
    Files.createFile(tempReport);
    File originalReport = new File(context.fileSystem().baseDir(), relativeReportPath);

    String newReportContent = Files.readAllLines(Path.of(originalReport.getPath()), StandardCharsets.UTF_8)
      .stream()
      .map(line -> line.replace(inputFile.relativePath(), inputFile.absolutePath()))
      .collect(Collectors.joining("\n"));
    Files.writeString(tempReport, newReportContent, StandardOpenOption.APPEND);
  }
}
