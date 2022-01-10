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
package org.sonar.plugins.php.phpunit;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
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
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CoverageResultImporterTest {

  private static final String BASE_DIR = "/org/sonar/plugins/php/phpunit/sensor/";
  private static final String MONKEY_FILE_NAME = "src/Monkey.php";
  private static final String BANANA_FILE_NAME = "src/Banana.php";
  private static final String SRC_TEST_RESOURCES = "src/test/resources/";
  private static final File MONKEY_FILE = new File(SRC_TEST_RESOURCES + BASE_DIR + MONKEY_FILE_NAME);
  private static final File BANANA_FILE = new File(SRC_TEST_RESOURCES + BASE_DIR + BANANA_FILE_NAME);
  private final AnalysisWarningsWrapper analysisWarnings = spy(AnalysisWarningsWrapper.class);

  private CoverageResultImporter importer;
  private SensorContextTester context;

  @Rule
  public LogTester logTester = new LogTester();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

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

    importer.importReport(getReportsWithAbsolutePath("phpunit.coverage-with-package.xml"), context);

    assertCoverageLineHits(context, componentKey, 34, 1);
  }

  @Test
  public void should_generate_coverage_measures_also_with_missing_files() throws Exception {
    executeSensorImporting(getReportsWithAbsolutePath("phpunit.coverage.xml"));
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
  public void should_work_with_relative_paths() throws Exception {
    String componentKey = componentKey(MONKEY_FILE_NAME);

    String reportName = "phpunit.coverage.xml";
    File reportFile = Paths.get(SRC_TEST_RESOURCES, PhpTestUtils.PHPUNIT_REPORT_DIR, reportName).toFile();
    importer.importReport(reportFile, context);
    assertReport(componentKey);
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

    importer.importReport(getReportsWithAbsolutePath("phpunit.coverage-with-no-statements-covered.xml"), context);

    assertCoverageLineHits(context, componentKey, 31, 0);
  }

  /**
   * SONARPLUGINS-1675
   */
  @Test
  public void should_not_fail_if_no_line_for_file_node() throws Exception {
    try {
      importer.importReport(getReportsWithAbsolutePath("phpunit.coverage-with-filenode-without-line.xml"), context);
    } catch (Exception e) {
      fail("Shound never happen");
    }
  }

  @Test
  public void should_not_set_metrics_to_ncloc_for_missing_files_sq_62() throws Exception {
    String componentKey = componentKey("Monkey.php");

    importer.importReport(getReportsWithAbsolutePath("phpunit.coverage-empty.xml"), context);

    // since SQ 6.2 these are not saved
    assertThat(context.measure(componentKey, CoreMetrics.LINES_TO_COVER)).isNull();
    assertThat(context.measure(componentKey, CoreMetrics.UNCOVERED_LINES)).isNull();
  }

  /**
   * Replace file name with absolute path in coverage report.
   *
   * This hack allow to have this unit test, as only absolute path
   * in report is supported.
   * */
  private File getReportsWithAbsolutePath(String reportName) throws Exception {
    File fileWIthAbsolutePaths = folder.newFile("report_with_absolute_paths.xml");

    Files.write(
      Files.toString(new File(SRC_TEST_RESOURCES + PhpTestUtils.PHPUNIT_REPORT_DIR + reportName), StandardCharsets.UTF_8)
        .replace(MONKEY_FILE_NAME, MONKEY_FILE.getAbsolutePath())
        .replace(BANANA_FILE_NAME, BANANA_FILE.getAbsolutePath()),
      fileWIthAbsolutePaths, StandardCharsets.UTF_8);

    return fileWIthAbsolutePaths;
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
