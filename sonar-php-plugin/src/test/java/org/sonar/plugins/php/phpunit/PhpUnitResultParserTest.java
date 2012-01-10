/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.PhpPlugin;

import com.thoughtworks.xstream.XStreamException;

/**
 * The Class PhpUnitResultParserTest.
 */
public class PhpUnitResultParserTest {

  /** The context. */
  private SensorContext context;

  /** The config. */
  private PhpUnitConfiguration config;

  /** The project. */
  private Project project;

  /** The metric. */
  private Metric metric;

  /**
   * Inits the.
   */
  private void init() {
    Configuration configuration = mock(Configuration.class);
    config = mock(PhpUnitConfiguration.class);
    project = mock(Project.class);
    context = mock(SensorContext.class);

    when(project.getConfiguration()).thenReturn(configuration);
    when(config.getProject()).thenReturn(project);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    File reportFile = new File(getClass().getResource("/org/sonar/plugins/php/phpunit/sensor/phpunit.xml").getFile());
    when(config.getReportFile()).thenReturn(reportFile);

    when(configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY)).thenReturn(null);

    PhpUnitResultParser parser = new PhpUnitResultParser(project, context);
    parser.parse(config.getReportFile());
  }

  /**
   * Should not throw an exception when report not found.
   */
  @Test
  public void shouldNotThrowAnExceptionWhenReportNotFound() {
    config = mock(PhpUnitConfiguration.class);
    project = mock(Project.class);
    context = mock(SensorContext.class);
    when(config.getReportFile()).thenReturn(new File("path/to/nowhere"));
    PhpUnitResultParser parser = new PhpUnitResultParser(project, context);
    parser.parse(null);
  }

  /**
   * Should save zero value when report not found.
   */
  @Test
  public void shouldSaveZeroValueWhenReportNotFound() {
    metric = CoreMetrics.TESTS;
    config = mock(PhpUnitConfiguration.class);
    project = mock(Project.class);
    context = mock(SensorContext.class);
    PhpUnitResultParser parser = new PhpUnitResultParser(project, context);
    parser.parse(null);
    verify(context).saveMeasure(metric, 0.0);
  }

  /**
   * Should throw an exception when report is invalid.
   */
  @Test(expected = XStreamException.class)
  public void shouldNotThrowAnExceptionWhenReportIsInvalid() {
    config = mock(PhpUnitConfiguration.class);
    project = mock(Project.class);
    context = mock(SensorContext.class);
    when(config.getReportFile()).thenReturn(
        new File(getClass().getResource("/org/sonar/plugins/php/phpunit/sensor/phpunit-invalid.xml").getFile()));
    PhpUnitResultParser parser = new PhpUnitResultParser(project, context);
    parser.parse(config.getReportFile());
  }

  /**
   * Should generate tests metrics.
   */
  @Test()
  public void shouldGenerateTestsMeasures() {
    metric = CoreMetrics.TESTS;
    init();
    verify(context).saveMeasure(new org.sonar.api.resources.File("Monkey.php"), metric, 3.0);
    verify(context).saveMeasure(new org.sonar.api.resources.File("Banana.php"), metric, 1.0);
  }

  /**
   * Should generate failure test metrics.
   */
  @Test()
  public void shouldGenerateFailedMeasures() {
    metric = CoreMetrics.TEST_FAILURES;
    init();
    verify(context).saveMeasure(new org.sonar.api.resources.File("Monkey.php"), metric, 2.0);
    verify(context).saveMeasure(new org.sonar.api.resources.File("Banana.php"), metric, 0.0);
  }

  /**
   * Should generate error test metrics.
   */
  @Test()
  public void shouldGenerateErrorMeasures() {
    metric = CoreMetrics.TEST_ERRORS;
    init();
    verify(context).saveMeasure(new org.sonar.api.resources.File("Monkey.php"), metric, 1.0);
    verify(context).saveMeasure(new org.sonar.api.resources.File("Banana.php"), metric, 1.0);
  }

  /**
   * Should generate execution time test metrics.
   */
  @Test()
  public void shouldGenerateTestExecutionTimeMeasures() {
    metric = CoreMetrics.TEST_EXECUTION_TIME;
    init();
    org.sonar.api.resources.File monkey = new org.sonar.api.resources.File("Monkey.php");
    verify(context).saveMeasure(monkey, metric, 447.0);
    verify(context).saveMeasure(monkey, CoreMetrics.TESTS, 3.0);
    verify(context).saveMeasure(monkey, CoreMetrics.TEST_ERRORS, 1.0);
    verify(context).saveMeasure(monkey, CoreMetrics.TEST_FAILURES, 2.0);
    verify(context).saveMeasure(monkey, CoreMetrics.TEST_FAILURES, 2.0);
    verify(context).saveMeasure(monkey, CoreMetrics.TEST_SUCCESS_DENSITY, 0.0);
    verify(context).saveMeasure(new org.sonar.api.resources.File("Banana.php"), metric, 570.0);
  }

  @Test
  public void shouldNotSaveTestReportMeasuresIfReportInvalid() throws Exception {
    context = mock(SensorContext.class);
    PhpUnitResultParser parser = new PhpUnitResultParser(null, context);
    parser.saveTestReportMeasures(new PhpUnitTestReport());
    verify(context, never()).saveMeasure(any(org.sonar.api.resources.File.class), any(Metric.class), anyDouble());
  }

  @Test(expected = SonarException.class)
  public void testGetTestSuitesWithUnexistingFile() throws Exception {
    PhpUnitResultParser parser = new PhpUnitResultParser(null, null);
    parser.getTestSuites(new File("target/unexistingFile.xml"));
  }

}
