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

import com.thoughtworks.xstream.XStreamException;
import java.io.File;
import java.util.HashMap;
import org.junit.Test;
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class PhpUnitResultParserTest {

  private PhpUnitResultParser parser;

  private SensorContextTester setUpForSensorContextTester() {
    SensorContextTester context = SensorContextTester.create(new File("src/test/resources"));
    parser = new PhpUnitResultParser(PhpTestUtils.getDefaultFileSystem());
    return context;
  }

  private SensorContext setUpForMockedSensorContext() {
    SensorContext context = Mockito.mock(SensorContext.class);
    parser = new PhpUnitResultParser(PhpTestUtils.getDefaultFileSystem());
    return context;
  }

  /**
   * Should throw an exception when report is invalid.
   */
  @Test(expected = XStreamException.class)
  public void shouldThrowAnExceptionWhenReportIsInvalid() {
    SensorContext context = setUpForMockedSensorContext();
    parser.parse(TestUtils.getResource(PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-invalid.xml"), context, new HashMap<File, Integer>());

    verify(context, never()).newMeasure();
  }

  @Test
  public void shouldNotFailIfNoFileName() {
    SensorContext context = setUpForMockedSensorContext();
    parser.parse(TestUtils.getResource(PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-no-filename.xml"), context, new HashMap<File, Integer>());

    verify(context, never()).newMeasure();
  }

  @Test
  public void shouldNotFailWithEmptyTestSuites() {
    SensorContext context = setUpForMockedSensorContext();
    parser.parse(TestUtils.getResource(PhpTestUtils.PHPUNIT_REPORT_DIR + "phpunit-with-empty-testsuites.xml"), context, new HashMap<File, Integer>());

    verify(context, never()).newMeasure();
  }

  /**
   * Should generate tests metrics.
   */
  @Test()
  public void shouldGenerateTestsMeasures() {
    SensorContextTester context = setUpForSensorContextTester();
    File baseDir = TestUtils.getResource("/org/sonar/plugins/php/phpunit/sensor/src/");
    DefaultFileSystem fs = new DefaultFileSystem(baseDir);
    DefaultInputFile monkeyFile = new DefaultInputFile("moduleKey", "Monkey.php").setType(InputFile.Type.TEST).setLanguage(Php.KEY);
    DefaultInputFile bananaFile = new DefaultInputFile("moduleKey", "Banana.php").setType(InputFile.Type.TEST).setLanguage(Php.KEY);

    fs.add(monkeyFile);
    fs.add(bananaFile);

    String monkey = "moduleKey:" + "Monkey.php";
    String banana = "moduleKey:" + "Banana.php";

    parser = new PhpUnitResultParser(fs);
    parser.parse(TestUtils.getResource(PhpTestUtils.PHPUNIT_REPORT_NAME), context, new HashMap<File, Integer>());

    PhpTestUtils.assertMeasure(context, monkey, CoreMetrics.TESTS, 3);
    PhpTestUtils.assertMeasure(context, banana, CoreMetrics.TESTS, 1);

    PhpTestUtils.assertMeasure(context, monkey, CoreMetrics.TEST_FAILURES, 2);
    PhpTestUtils.assertMeasure(context, banana, CoreMetrics.TEST_FAILURES, 0);

    PhpTestUtils.assertMeasure(context, monkey, CoreMetrics.TEST_ERRORS, 1);
    PhpTestUtils.assertMeasure(context, banana, CoreMetrics.TEST_ERRORS, 1);

    // Test execution time:
    PhpTestUtils.assertMeasure(context, monkey, CoreMetrics.TEST_EXECUTION_TIME, 447L);
    PhpTestUtils.assertMeasure(context, monkey, CoreMetrics.TESTS, 3);
    PhpTestUtils.assertMeasure(context, monkey, CoreMetrics.TEST_ERRORS, 1);
    PhpTestUtils.assertMeasure(context, monkey, CoreMetrics.TEST_SUCCESS_DENSITY, 0.0);
    PhpTestUtils.assertMeasure(context, banana, CoreMetrics.TEST_EXECUTION_TIME, 570L);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetTestSuitesWithUnexistingFile() throws Exception {
    setUpForSensorContextTester();

    parser.getTestSuites(new File("target/unexistingFile.xml"));
  }

}
