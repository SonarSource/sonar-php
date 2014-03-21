/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.MockUtils;
import org.sonar.test.TestUtils;

import java.io.File;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PhpUnitResultParserTest {

  private SensorContext context;
  private Project project;
  private PhpUnitResultParser parser;
  private ModuleFileSystem fs;

  @Before
  public void setUp() throws Exception {
    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    when(fs.sourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\main")));
    when(fs.testDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));

    context = mock(SensorContext.class);
    project = mock(Project.class);

    parser = new PhpUnitResultParser(project, context, fs);
  }

  /**
   * Should not throw an exception when report not found.
   */
  @Test
  public void shouldNotThrowAnExceptionWhenReportNotFound() {
    project = mock(Project.class);
    context = mock(SensorContext.class);
    PhpUnitResultParser parser = new PhpUnitResultParser(project, context, fs);
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
    parser = spy(parser);
    doReturn(new org.sonar.api.resources.File("Monkey.php")).when(parser).getUnitTestResource("C:\\projets\\PHP\\Monkey\\Sources\\test\\Monkey.php");
    doReturn(new org.sonar.api.resources.File("Banana.php")).when(parser).getUnitTestResource("C:\\projets\\PHP\\Monkey\\Sources\\test\\Banana.php");

    parser.parse(TestUtils.getResource(MockUtils.PHPUNIT_REPORT));

    verify(context).saveMeasure(new org.sonar.api.resources.File("Monkey.php"), CoreMetrics.TESTS, 3.0);
    verify(context).saveMeasure(new org.sonar.api.resources.File("Banana.php"), CoreMetrics.TESTS, 1.0);

    verify(context).saveMeasure(new org.sonar.api.resources.File("Monkey.php"), CoreMetrics.TEST_FAILURES, 2.0);
    verify(context).saveMeasure(new org.sonar.api.resources.File("Banana.php"), CoreMetrics.TEST_FAILURES, 0.0);

    verify(context).saveMeasure(new org.sonar.api.resources.File("Monkey.php"), CoreMetrics.TEST_ERRORS, 1.0);
    verify(context).saveMeasure(new org.sonar.api.resources.File("Banana.php"), CoreMetrics.TEST_ERRORS, 1.0);

    shouldGenerateTestExecutionTimeMeasures();
  }

  public void shouldGenerateTestExecutionTimeMeasures() {
    org.sonar.api.resources.File monkey = new org.sonar.api.resources.File("Monkey.php");
    verify(context).saveMeasure(monkey, CoreMetrics.TEST_EXECUTION_TIME, 447.0);
    verify(context).saveMeasure(monkey, CoreMetrics.TESTS, 3.0);
    verify(context).saveMeasure(monkey, CoreMetrics.TEST_ERRORS, 1.0);
    verify(context).saveMeasure(monkey, CoreMetrics.TEST_FAILURES, 2.0);
    verify(context).saveMeasure(monkey, CoreMetrics.TEST_FAILURES, 2.0);
    verify(context).saveMeasure(monkey, CoreMetrics.TEST_SUCCESS_DENSITY, 0.0);
    verify(context).saveMeasure(new org.sonar.api.resources.File("Banana.php"), CoreMetrics.TEST_EXECUTION_TIME, 570.0);
  }


  @Test(expected = SonarException.class)
  public void testGetTestSuitesWithUnexistingFile() throws Exception {
    parser.getTestSuites(new File("target/unexistingFile.xml"));
  }

}
