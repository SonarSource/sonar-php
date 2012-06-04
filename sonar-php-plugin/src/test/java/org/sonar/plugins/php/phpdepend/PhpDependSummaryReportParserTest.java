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
package org.sonar.plugins.php.phpdepend;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.File;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.PhpPlugin;
import org.sonar.plugins.php.api.Php;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.sonar.plugins.php.MockUtils.getMockProject;

/**
 * The Class PhpDependSummaryReportParserTest.
 */
public class PhpDependSummaryReportParserTest {

  /** The context. */
  private SensorContext context;

  /** The metric. */
  private Metric metric;

  private Project project;

  private static final String SUMMARY_RESULT = "/org/sonar/plugins/php/phpdepend/sensor/parser/summary.xml";

  /**
   * Inits the result parser.
   */
  private void init(String pdependResultFile) {
    try {
      java.io.File xmlReport = new java.io.File(getClass().getResource(pdependResultFile).toURI());
      context = mock(SensorContext.class);
      project = mock(Project.class);

      ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
      when(project.getFileSystem()).thenReturn(fileSystem);
      when(fileSystem.getSourceDirs()).thenReturn(Arrays.asList(new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src")));
      when(fileSystem.getTestDirs()).thenReturn(Arrays.asList(new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/tests")));

      java.io.File f1 = new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src/Math3.php");
      java.io.File f2 = new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src/Bar/Math4.php");
      java.io.File f3 = new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src/Foo/Math5.php");
      java.io.File f4 = new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src/Math.php");
      java.io.File f5 = new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src/Math2.php");
      java.io.File f6 = new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src/Mail.php");
      java.io.File f7 = new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/tests/AllTests.php");
      java.io.File f8 = new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/tests/AllTests2.php");

      when(fileSystem.mainFiles(Php.KEY)).thenReturn(
          InputFileUtils.create(new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src"), Arrays.asList(f1, f2, f3, f4, f5, f6)));
      when(fileSystem.testFiles(Php.KEY)).thenReturn(
          InputFileUtils.create(new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/tests"), Arrays.asList(f7, f8)));

      Set<Metric> metrics = new HashSet<Metric>();
      metrics.add(metric);
      PhpDependSummaryReportParser parser = new PhpDependSummaryReportParser(project, context);

      Configuration configuration = mock(Configuration.class);

      when(configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY)).thenReturn(null);
      Php.PHP.setConfiguration(configuration);
      parser.parse(xmlReport);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test(expected = SonarException.class)
  public void shouldThrowAnExceptionWhenReportNotFound() {
    /*Configuration c = mock(Configuration.class);
    Project project = getMockProject("/path/to/sources", c);
    when(c.getString(PDEPEND_REPORT_FILE_NAME_KEY, PDEPEND_REPORT_FILE_NAME_DEFVALUE)).thenReturn(PDEPEND_REPORT_FILE_NAME_DEFVALUE);
    when(c.getString(PDEPEND_REPORT_TYPE, PDEPEND_REPORT_TYPE_DEFVALUE)).thenReturn("summary-xml");
    when(c.getString(PDEPEND_REPORT_FILE_RELATIVE_PATH_KEY, PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn(
        PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE);
    when(project.getConfiguration()).thenReturn(c);*/
    Project project = getMockProject();

    PhpDependSummaryReportParser parser = new PhpDependSummaryReportParser(project, null);
    parser.parse(new java.io.File("path/to/nowhere"));
  }

  @Test
  public void testShouldGenerateLocMeasure() {
    metric = CoreMetrics.LINES;
    init(SUMMARY_RESULT);
    verify(context).saveMeasure(new File("Math3.php"), metric, 227.0);
    verify(context).saveMeasure(new File("Bar/Math4.php"), metric, 211.0);
    verify(context).saveMeasure(new File("Foo/Math5.php"), metric, 211.0);
    verify(context).saveMeasure(new File("Math.php"), metric, 215.0);
    verify(context).saveMeasure(new File("Math2.php"), metric, 211.0);
    verify(context).saveMeasure(new File("Mail.php"), metric, 1262.0);
  }

  @Test
  public void testShouldGenerateClassesCountMeasure() {
    metric = CoreMetrics.CLASSES;
    init(SUMMARY_RESULT);
    verify(context).saveMeasure(new File("Math3.php"), metric, 3.0);
    verify(context).saveMeasure(new File("Bar/Math4.php"), metric, 1.0);
    verify(context).saveMeasure(new File("Foo/Math5.php"), metric, 1.0);
    verify(context).saveMeasure(new File("Math.php"), metric, 1.0);
    verify(context).saveMeasure(new File("Math2.php"), metric, 1.0);
    verify(context).saveMeasure(new File("Mail.php"), metric, 1.0);
  }
}
