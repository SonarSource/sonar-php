/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.phpdepend.sensor;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.PhpPlugin;
import org.sonar.plugins.php.core.resources.PhpFile;

/**
 * The Class PhpDependResultsParserTest.
 */
public class PhpDependResultsParserTest {

  /** The context. */
  private SensorContext context;

  /** The metric. */
  private Metric metric;

  private Project project;

  private static final String PDEPEND_RESULT = "/org/sonar/plugins/php/phpdepend/sensor/PhpDependResultsParserTest/pdepend.xml";
  private static final String PDEPEND_RESULT_SAMEFILE_DIFFERBYFSUFFIX = "/org/sonar/plugins/php/phpdepend/sensor/PhpDependResultsParserTest/pdepend.samefiledifferbysuffix.xml";

  /**
   * Inits the result parser.
   */
  private void init(String pdependResultFile) {
    try {
      File xmlReport = new File(getClass().getResource(pdependResultFile).toURI());
      context = mock(SensorContext.class);
      project = mock(Project.class);
      ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
      when(project.getFileSystem()).thenReturn(fileSystem);
      when(fileSystem.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Money\\Sources\\main")));
      when(fileSystem.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Money\\Sources\\test")));
      Set<Metric> metrics = new HashSet<Metric>();
      metrics.add(metric);
      PhpDependResultsParser parser = new PhpDependResultsParser(project, context, metrics);

      Configuration configuration = mock(Configuration.class);
      new Php(configuration);
      when(configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY)).thenReturn(null);

      parser.parse(xmlReport);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Should not throw an exception when report not found.
   */
  @Test
  public void shouldNotThrowAnExceptionWhenReportNotFound() {
    project = mock(Project.class);
    PhpDependResultsParser parser = new PhpDependResultsParser(project, null);
    parser.parse(new File("path/to/nowhere"));
  }

  /**
   * Should throw an exception when file isn't valid
   */
  @Test(expected = SonarException.class)
  public void shouldThrowAnExceptionWhenReportIsInvalid() {
    project = mock(Project.class);
    PhpDependResultsParser parser = new PhpDependResultsParser(project, null);
    parser.parse(new File(getClass().getResource("/org/sonar/plugins/php/phpdepend/sensor/PhpDependResultsParserTest/pdepend-invalid.xml")
        .getFile()));
  }

  /**
   * Should not throw an exception if metric not found.
   */
  @Test
  public void shouldNotThrowAnExceptionIfMetricNotFound() {
    metric = new Metric("not a true metric");
    init(PDEPEND_RESULT);
    verify(context, never()).saveMeasure(eq(metric), anyDouble());
  }

  /**
   * Should generate loc measures.
   */
  @Test
  public void shouldGenerateLocMeasures() {
    metric = CoreMetrics.LINES;
    init(PDEPEND_RESULT);
    verify(context).saveMeasure(new PhpFile("MoneyTest.php", true), metric, 255.0);
    verify(context).saveMeasure(new PhpFile("Money.php"), metric, 188.0);
    verify(context).saveMeasure(new PhpFile("MoneyBag.php"), metric, 251.0);
    verify(context).saveMeasure(new PhpFile("Common/IMoney.php"), metric, 74.0);
  }

  /**
   * Should generate ncloc measures.
   */
  @Test
  public void shouldGenerateNclocMeasures() {
    metric = CoreMetrics.NCLOC;
    init(PDEPEND_RESULT);
    verify(context).saveMeasure(new PhpFile("Money.php"), metric, 94.0);
    verify(context).saveMeasure(new PhpFile("MoneyBag.php"), metric, 150.0);
    verify(context).saveMeasure(new PhpFile("MoneyTest.php"), metric, 152.0);
    verify(context).saveMeasure(new PhpFile("Common/IMoney.php"), metric, 15.0);
  }

  /**
   * Should generate functions count measure.
   */
  @Test
  public void shouldGenerateFunctionsCountMeasure() {
    metric = CoreMetrics.FUNCTIONS;
    init(PDEPEND_RESULT);
    verify(context).saveMeasure(new PhpFile("Money.php"), metric, 16.0);
    verify(context).saveMeasure(new PhpFile("MoneyBag.php"), metric, 18.0);
    verify(context).saveMeasure(new PhpFile("MoneyTest.php"), metric, 24.0);
    verify(context).saveMeasure(new PhpFile("Common/IMoney.php"), metric, 8.0);
  }

  /**
   * Should generate classes count measure.
   */
  @Test
  public void shouldGenerateClassesCountMeasure() {
    metric = CoreMetrics.CLASSES;
    init(PDEPEND_RESULT);
    verify(context).saveMeasure(new PhpFile("Money.php"), metric, 2.0);
    verify(context).saveMeasure(new PhpFile("MoneyBag.php"), metric, 1.0);
    verify(context).saveMeasure(new PhpFile("MoneyTest.php"), metric, 1.0);
    verify(context).saveMeasure(new PhpFile("Common/IMoney.php"), metric, 1.0);
  }

  /**
   * Should generate complexty measure.
   */
  @Test
  public void shouldGenerateComplextyMeasure() {
    metric = CoreMetrics.COMPLEXITY;
    init(PDEPEND_RESULT);
    verify(context).saveMeasure(new PhpFile("Money.php"), metric, 21.0);
    verify(context).saveMeasure(new PhpFile("MoneyBag.php"), metric, 39.0);
    verify(context).saveMeasure(new PhpFile("MoneyTest.php"), metric, 24.0);
    verify(context).saveMeasure(new PhpFile("Common/IMoney.php"), metric, 0.0);
  }

  /**
   * Should not generate dir or project measures.
   */
  @Test
  public void shouldNotGenerateDirOrProjectMeasures() {
    metric = CoreMetrics.LINES;
    init(PDEPEND_RESULT);

    verify(context, never()).saveMeasure(eq(metric), anyDouble());

    verify(context, never()).saveMeasure(eq(new org.sonar.api.resources.Directory("Sources/main")), eq(metric), anyDouble());
    verify(context, never()).saveMeasure(eq(new org.sonar.api.resources.Directory("Sources/main/Common")), eq(metric), anyDouble());
  }

  @Test
  public void shouldGenerateValidMeasuresOnSameFileWithDifferentSuffix() {
    metric = CoreMetrics.COMPLEXITY;
    init(PDEPEND_RESULT_SAMEFILE_DIFFERBYFSUFFIX);
    verify(context).saveMeasure(new PhpFile("MoneyTest.php", true), metric, 24.0);
    verify(context).saveMeasure(new PhpFile("Money.php"), metric, 21.0);
    verify(context).saveMeasure(new PhpFile("Money.inc"), metric, 39.0);

  }

}