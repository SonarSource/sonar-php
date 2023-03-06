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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.warning.AnalysisWarningsWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CoverageResultImporterTest {

  private static final String BASE_DIR = "/reports/phpunit/";
  private static final String MONKEY_FILE_NAME = "src/Monkey.php";
  private static final String SRC_TEST_RESOURCES = "src/test/resources/";
  private final AnalysisWarningsWrapper analysisWarnings = spy(AnalysisWarningsWrapper.class);

  private CoverageResultImporter importer;
  private SensorContextTester context;

  @Rule
  public LogTester logTester = new LogTester();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    context = SensorContextTester.create(new File(SRC_TEST_RESOURCES + BASE_DIR).getAbsoluteFile());
    DefaultInputFile monkeyFile = TestInputFileBuilder.create("moduleKey", MONKEY_FILE_NAME)
      .setType(InputFile.Type.MAIN)
      .setLanguage(Php.KEY)
      .setCharset(Charset.defaultCharset())
      .setLines(50)
      .build();
    context.fileSystem().add(monkeyFile);

    importer = new CoverageResultImporter(analysisWarnings);
  }

  @Test
  public void should_add_waring_and_log_when_report_not_found() throws IOException {
    executeSensorImporting(new File("notfound.txt"));
    assertThat(logTester.logs(LoggerLevel.ERROR)).hasSize(1);
    assertThat((logTester.logs(LoggerLevel.ERROR).get(0)))
      .startsWith("An error occurred when reading report file '")
      .contains("notfound.txt', nothing will be imported from this report.");

    verify(analysisWarnings, times(1))
      .addWarning(startsWith("An error occurred when reading report file '"));
  }

  @Test
  public void should_parse_even_with_package_node() throws Exception {
    String componentKey = componentKey(MONKEY_FILE_NAME);

    executeSensorImporting(getReportFile("phpunit.coverage-with-package.xml"));

    assertCoverageLineHits(context, componentKey, 34, 1);
  }

  @Test
  public void should_generate_coverage_measures_also_with_missing_files() throws Exception {
    executeSensorImporting(getReportFile("phpunit.coverage.xml"));
    String componentKey = componentKey(MONKEY_FILE_NAME);
    assertReport(componentKey);
    assertThat(logTester.logs(LoggerLevel.WARN)).hasSize(3);
    assertThat(logTester.logs(LoggerLevel.WARN).get(0))
      .isEqualTo("Line with number 0 doesn't belong to file Monkey.php");
    assertThat(logTester.logs(LoggerLevel.WARN).get(1))
      .isEqualTo("Line with number 100 doesn't belong to file Monkey.php");
    assertThat((logTester.logs(LoggerLevel.WARN).get(2)))
      .startsWith("Failed to resolve 2 file path(s) in PHPUnit coverage")
      .contains("Nothing is imported related to file(s):");

    verify(analysisWarnings, times(1))
      .addWarning(startsWith("Failed to resolve 2 file path(s) in PHPUnit coverage"));
  }

  @Test
  public void should_not_raise_warning_for_excluded_files() throws Exception {
    context.settings().setProperty("sonar.exclusion", "**/IndexControllerTest.php,**/Banana.php");
    executeSensorImporting(getReportFile("phpunit.coverage.xml"));
    assertThat(logTester.logs(LoggerLevel.WARN)).hasSize(2);
    verify(analysisWarnings,never()).addWarning(anyString());
  }

  @Test
  public void should_generate_coverage_measures_with_fqn_paths() {
    String warning = "Failed to resolve 2 file path(s) in PHPUnit coverage phpunit.coverage-fqn.xml report. " +
      "Nothing is imported related to file(s): /foo/bar/src/Banana.php;/foo/bar/src/IndexControllerTest.php";

    executeSensorImporting(getReportFile("phpunit.coverage-fqn.xml"));
    assertReport(componentKey(MONKEY_FILE_NAME));
    assertThat(logTester.logs(LoggerLevel.WARN)).containsExactly(warning);

    verify(analysisWarnings, times(1)).addWarning(warning);
  }

  @Test
  public void should_generate_coverage_measures_with_windows_fqn_paths() {
    String warning = "Failed to resolve 2 file path(s) in PHPUnit coverage phpunit.coverage-fqn_win.xml report. " +
      "Nothing is imported related to file(s): C:\\foo\\bar\\src\\Banana.php;C:\\foo\\bar\\src\\IndexControllerTest.php";

    executeSensorImporting(getReportFile("phpunit.coverage-fqn_win.xml"));
    assertReport(componentKey(MONKEY_FILE_NAME));
    assertThat(logTester.logs(LoggerLevel.WARN)).containsExactly(warning);

    verify(analysisWarnings, times(1)).addWarning(warning);
  }

  private void assertReport(String componentKey) {
    // UNCOVERED_LINES is implicitly stored in the NewCoverage
    PhpTestUtils.assertNoMeasure(context, componentKey, CoreMetrics.UNCOVERED_LINES);

    assertCoverageLineHits(context, componentKey, 34, 1);
    assertCoverageLineHits(context, componentKey, 35, 1);
    assertCoverageLineHits(context, componentKey, 38, 1);
    assertCoverageLineHits(context, componentKey, 40, 0);
    assertCoverageLineHits(context, componentKey, 45, 1);
    assertCoverageLineHits(context, componentKey, 46, 1);
  }

  /**
   * SONARPLUGINS-1591
   */
  @Test
  public void should_not_fail_if_no_statement_count() throws Exception {
    String componentKey = componentKey(MONKEY_FILE_NAME);

    executeSensorImporting(getReportFile("phpunit.coverage-with-no-statements-covered.xml"));

    assertCoverageLineHits(context, componentKey, 31, 0);
  }

  /**
   * SONARPLUGINS-1675
   */
  @Test
  public void should_not_fail_if_no_line_for_file_node() throws Exception {
    try {
      executeSensorImporting(getReportFile("phpunit.coverage-with-filenode-without-line.xml"));
    } catch (Exception e) {
      fail("Should never happen");
    }
  }

  @Test
  public void should_not_set_metrics_to_ncloc_for_missing_files_sq_62() throws Exception {
    String componentKey = componentKey("Monkey.php");

    executeSensorImporting(getReportFile("phpunit.coverage-empty.xml"));

    // since SQ 6.2 these are not saved
    assertThat(context.measure(componentKey, CoreMetrics.LINES_TO_COVER)).isNull();
    assertThat(context.measure(componentKey, CoreMetrics.UNCOVERED_LINES)).isNull();
  }

  private static File getReportFile(String file) {
    return new File(SRC_TEST_RESOURCES + BASE_DIR + file);
  }

  private void executeSensorImporting(File fileName) {
    context.settings().setProperty(importer.reportPathKey(), fileName.getAbsolutePath());
    importer.execute(context);
  }

  private static void assertCoverageLineHits(SensorContextTester context, String componentKey, int line, int expectedHits) {
    assertThat(context.lineHits(componentKey, line)).as("coverage line hits for line: " + line).isEqualTo(expectedHits);
  }

  private static String componentKey(String componentName) {
    return "moduleKey:" + componentName;
  }
}
