/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.test.TestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class PhpUnitCoverageResultParserTest {

  private static final String BASE_DIR = "/org/sonar/plugins/php/phpunit/sensor/src/";
  private static final String MONKEY_FILE_NAME = "Monkey.php";
  private static final String BANANA_FILE_NAME = "Banana.php";
  private static final File MONKEY_FILE = TestUtils.getResource(BASE_DIR + MONKEY_FILE_NAME);
  private static final File BANANA_FILE = TestUtils.getResource(BASE_DIR + BANANA_FILE_NAME);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private PhpUnitCoverageResultParser parser;

  private DefaultFileSystem fileSystem;

  private Map<File, Integer> numberOfLinesOfCode;

  private SensorContextTester setUpForSensorContextTester() {
    return SensorContextTester.create(new File("src/test/resources"));
  }

  private SensorContext setUpForMockedSensorContext() {
    return Mockito.mock(SensorContext.class);
  }

  @Before
  public void setUp() throws Exception {
    fileSystem = new DefaultFileSystem(TestUtils.getResource(BASE_DIR));
    DefaultInputFile monkeyFile = new DefaultInputFile("moduleKey", MONKEY_FILE_NAME)
        .setType(InputFile.Type.MAIN)
        .setLanguage(Php.KEY)
        .setLines(50);
    fileSystem.add(monkeyFile);

    numberOfLinesOfCode = new HashMap<File, Integer>();

    parser = new PhpUnitCoverageResultParser(fileSystem);
  }

  @Test
  public void shouldThrowAnExceptionWhenReportNotFound() {
    SensorContext context = setUpForMockedSensorContext();
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Can't read phpUnit report:");

    parser.parse(new File("notfound.txt"), context, numberOfLinesOfCode);
  }

  /**
   * Should parse even when there's a package node.
   */
  @Test
  public void shouldParseEvenWithPackageNode() throws Exception {
    SensorContextTester context = setUpForSensorContextTester();
    String componentKey = "moduleKey:Monkey.php"; // see call to method getReportsWithAbsolutePath below

    parser.parse(getReportsWithAbsolutePath("phpunit.coverage-with-package.xml"), context, numberOfLinesOfCode);

    assertCoverageLineHits(context, componentKey, 34, 1);
  }

  /**
   * Should generate coverage metrics.
   */
   @Test
   public void shouldGenerateCoverageMeasures() throws Exception {
     SensorContextTester context = setUpForSensorContextTester();
     String componentKey = "moduleKey:Monkey.php"; // see call to method getReportsWithAbsolutePath below

     parser.parse(getReportsWithAbsolutePath("phpunit.coverage.xml"), context, numberOfLinesOfCode);

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
  public void shouldNotFailIfNoStatementCount() throws Exception {
    SensorContextTester context = setUpForSensorContextTester();
    String componentKey = "moduleKey:Monkey.php"; // see call to method getReportsWithAbsolutePath below

    parser.parse(getReportsWithAbsolutePath("phpunit.coverage-with-no-statements-covered.xml"), context, numberOfLinesOfCode);

    assertCoverageLineHits(context, componentKey, 31, 0);
  }

  /**
   * SONARPLUGINS-1675
   */
  @Test
  public void shouldNotFailIfNoLineForFileNode() throws Exception {
    SensorContextTester context = setUpForSensorContextTester();

    parser.parse(getReportsWithAbsolutePath("phpunit.coverage-with-filenode-without-line.xml"), context, numberOfLinesOfCode);
  }

  @Test
  public void should_set_metrics_to_ncloc_for_missing_files() throws Exception {
    SensorContextTester context = setUpForSensorContextTester();
    String componentKey = "moduleKey:Monkey.php"; // see call to method getReportsWithAbsolutePath below

    numberOfLinesOfCode.put(MONKEY_FILE, 42);

    parser.parse(getReportsWithAbsolutePath("phpunit.coverage-empty.xml"), context, numberOfLinesOfCode);

    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.LINES_TO_COVER, 42);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.UNCOVERED_LINES, 42);
  }

//  @Test
//  public void should_skip_missing_files_with_no_ncloc() throws Exception {
//    SensorContextTester context = setUpForSensorContextTester();
//    String componentKey = "moduleKey:Monkey.php"; // see call to method getReportsWithAbsolutePath below
//
//    parser.parse(getReportsWithAbsolutePath("phpunit.coverage-empty.xml"), context, numberOfLinesOfCode);
//
//    // verify(context, Mockito.never()).saveMeasure(any(Resource.class), eq(CoreMetrics.LINES_TO_COVER), any(Double.class));
//  }

  /**
   * Replace file name with absolute path in coverage report.
   *
   * This hack allow to have this unit test, as only absolute path
   * in report is supported.
   * */
  private File getReportsWithAbsolutePath(String reportName) throws Exception {
    File fileWIthAbsolutePaths = folder.newFile("report_with_absolute_paths.xml");

    Files.write(
      Files.toString(TestUtils.getResource(PhpTestUtils.PHPUNIT_REPORT_DIR + reportName), Charsets.UTF_8)
        .replace("/" + MONKEY_FILE_NAME, MONKEY_FILE.getAbsolutePath())
        .replace("/" + BANANA_FILE_NAME, BANANA_FILE.getAbsolutePath()),
      fileWIthAbsolutePaths, Charsets.UTF_8);

    return fileWIthAbsolutePaths;
  }

  private void assertCoverageLineHits(SensorContextTester context, String componentKey, int line, int expectedHits) {
    assertThat(context.lineHits(componentKey, parser.coverageType, line)).as("coverage line hits for line: " + line).isEqualTo(expectedHits);
  }

}
