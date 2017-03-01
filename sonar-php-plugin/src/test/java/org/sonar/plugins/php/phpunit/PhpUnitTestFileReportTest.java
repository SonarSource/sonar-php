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

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.php.PhpTestUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.phpunit.xml.TestCase;
import org.sonar.test.TestUtils;

public class PhpUnitTestFileReportTest {

  private String componentKey;
  private String testFileName;
  private SensorContextTester context;

  @Before
  public void setUp() throws Exception {
    File baseDir = TestUtils.getResource("/org/sonar/plugins/php/phpunit/sensor/src/");
    DefaultFileSystem fs = new DefaultFileSystem(baseDir);
    testFileName = "testfile.php";
    DefaultInputFile testFile = new DefaultInputFile("moduleKey", testFileName).setType(InputFile.Type.TEST).setLanguage(Php.KEY);
    context.fileSystem().add(testFile);
    context = SensorContextTester.create(new File("src/test/resources"));
    context.setFileSystem(fs);
    componentKey = testFile.key();
  }

  @Test
  public void shouldReportStatusCounts() throws Exception {
    final PhpUnitTestFileReport report = new PhpUnitTestFileReport(testFileName, 3d);
    report.addTestCase(new TestCase(TestCase.Status.SKIPPED));
    report.addTestCase(new TestCase(TestCase.Status.ERROR));
    report.addTestCase(new TestCase(TestCase.Status.FAILURE));
    report.addTestCase(new TestCase(TestCase.Status.FAILURE));
    report.saveTestMeasures(context);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TEST_EXECUTION_TIME, 3000l);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.SKIPPED_TESTS, 1);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TEST_ERRORS, 1);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TEST_FAILURES, 2);
  }

  @Test
  public void shouldReportZeroTestsIfEmpty() throws Exception {
    final PhpUnitTestFileReport report = new PhpUnitTestFileReport(testFileName, 0d);
    report.saveTestMeasures(context);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TESTS, 0);
  }

  @Test
  public void shouldNotCountSkippedTests() throws Exception {
    final PhpUnitTestFileReport report = new PhpUnitTestFileReport(testFileName, 1d);
    report.addTestCase(new TestCase());
    report.addTestCase(new TestCase(TestCase.Status.SKIPPED));
    report.addTestCase(new TestCase(TestCase.Status.FAILURE));
    report.addTestCase(new TestCase(TestCase.Status.ERROR));
    report.saveTestMeasures(context);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TESTS, 3);
    PhpTestUtils.assertMeasure(context, componentKey, CoreMetrics.TEST_SUCCESS_DENSITY, 33.33d);
  }

  @Test
  public void shouldReportNoSuccessDensityIfNoLiveTests() throws Exception {
    final PhpUnitTestFileReport report = new PhpUnitTestFileReport(testFileName, 1d);
    report.addTestCase(new TestCase(TestCase.Status.SKIPPED));
    report.addTestCase(new TestCase(TestCase.Status.SKIPPED));
    report.addTestCase(new TestCase(TestCase.Status.SKIPPED));
    report.saveTestMeasures(context);
    PhpTestUtils.assertNoMeasure(context, componentKey, CoreMetrics.TEST_SUCCESS_DENSITY);
  }

}
