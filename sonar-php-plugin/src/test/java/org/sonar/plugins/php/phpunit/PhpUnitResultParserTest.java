/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.php.phpunit;

import com.thoughtworks.xstream.XStreamException;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.MockUtils;
import org.sonar.plugins.php.api.Php;
import org.sonar.test.TestUtils;

import java.io.File;

public class PhpUnitResultParserTest {

  private SensorContext context;
  private PhpUnitResultParser parser;

  @Before
  public void setUp() throws Exception {
    context = mock(SensorContext.class);
    parser = new PhpUnitResultParser(context, new DefaultFileSystem());
  }

  /**
   * Should not throw an exception when report not found.
   */
  @Test
  public void shouldNotThrowAnExceptionWhenReportNotFound() {
    context = mock(SensorContext.class);
    PhpUnitResultParser parser = new PhpUnitResultParser(context, new DefaultFileSystem());
    parser.parse(null);

    verify(context).saveMeasure(CoreMetrics.TESTS, 0.0);
  }

  /**
   * Should throw an exception when report is invalid.
   */
  @Test(expected = XStreamException.class)
  public void shouldNotThrowAnExceptionWhenReportIsInvalid() {
    parser.parse(TestUtils.getResource(MockUtils.PHPUNIT_REPORT_DIR + "phpunit-invalid.xml"));

    verify(context, never()).saveMeasure(any(org.sonar.api.resources.File.class), any(Metric.class), anyDouble());
  }

  /**
   * Should generate tests metrics.
   */
  @Test()
  public void shouldGenerateTestsMeasures() {
    File baseDir = TestUtils.getResource("/org/sonar/plugins/php/phpunit/sensor/src/");
    DefaultFileSystem fs = new DefaultFileSystem();
    InputFile monkeyFile = new DefaultInputFile("Monkey.php").setAbsolutePath((new File(baseDir, "Monkey.php").getAbsolutePath())).setType(InputFile.Type.TEST).setLanguage(Php.KEY);
    InputFile bananaFile = new DefaultInputFile("Banana.php").setAbsolutePath((new File(baseDir, "Monkey.php").getAbsolutePath())).setType(InputFile.Type.TEST).setLanguage(Php.KEY);

    fs.setBaseDir(baseDir);
    fs.add(monkeyFile);
    fs.add(bananaFile);

    parser = new PhpUnitResultParser(context, fs);
    parser.parse(TestUtils.getResource(MockUtils.PHPUNIT_REPORT_NAME));

    verify(context).saveMeasure(monkeyFile, CoreMetrics.TESTS, 3.0);
    verify(context).saveMeasure(bananaFile, CoreMetrics.TESTS, 1.0);

    verify(context).saveMeasure(monkeyFile, CoreMetrics.TEST_FAILURES, 2.0);
    verify(context).saveMeasure(bananaFile, CoreMetrics.TEST_FAILURES, 0.0);

    verify(context).saveMeasure(monkeyFile, CoreMetrics.TEST_ERRORS, 1.0);
    verify(context).saveMeasure(bananaFile, CoreMetrics.TEST_ERRORS, 1.0);

    // Test execution time:
    verify(context).saveMeasure(monkeyFile, CoreMetrics.TEST_EXECUTION_TIME, 447.0);
    verify(context).saveMeasure(monkeyFile, CoreMetrics.TESTS, 3.0);
    verify(context).saveMeasure(monkeyFile, CoreMetrics.TEST_ERRORS, 1.0);
    verify(context).saveMeasure(monkeyFile, CoreMetrics.TEST_FAILURES, 2.0);
    verify(context).saveMeasure(monkeyFile, CoreMetrics.TEST_FAILURES, 2.0);
    verify(context).saveMeasure(monkeyFile, CoreMetrics.TEST_SUCCESS_DENSITY, 0.0);
    verify(context).saveMeasure(bananaFile, CoreMetrics.TEST_EXECUTION_TIME, 570.0);
  }

  @Test(expected = SonarException.class)
  public void testGetTestSuitesWithUnexistingFile() throws Exception {
    parser.getTestSuites(new File("target/unexistingFile.xml"));
  }

}
