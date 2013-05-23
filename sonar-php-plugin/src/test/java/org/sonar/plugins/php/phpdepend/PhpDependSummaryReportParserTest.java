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

import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.File;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.HasComplexityDistribution;
import org.sonar.plugins.php.MockUtils;
import org.sonar.plugins.php.api.PhpConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

      when(fileSystem.mainFiles(PhpConstants.LANGUAGE_KEY)).thenReturn(
          InputFileUtils.create(new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src"), Arrays.asList(f1, f2, f3, f4, f5, f6)));
      when(fileSystem.testFiles(PhpConstants.LANGUAGE_KEY)).thenReturn(
          InputFileUtils.create(new java.io.File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/tests"), Arrays.asList(f7, f8)));

      Set<Metric> metrics = new HashSet<Metric>();
      metrics.add(metric);
      PhpDependSummaryReportParser parser = new PhpDependSummaryReportParser(project, context);

      parser.parse(xmlReport);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test(expected = SonarException.class)
  public void shouldThrowAnExceptionWhenReportNotFound() {
    PhpDependSummaryReportParser parser = new PhpDependSummaryReportParser(MockUtils.createMockProject(), null);
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

  @Test
  public void testShouldGenerateDitMeasure() {
    metric = CoreMetrics.DEPTH_IN_TREE;
    init(SUMMARY_RESULT);
    verify(context).saveMeasure(new File("Math3.php"), metric, 0.0);
    verify(context).saveMeasure(new File("Bar/Math4.php"), metric, 0.0);
    verify(context).saveMeasure(new File("Foo/Math5.php"), metric, 0.0);
    verify(context).saveMeasure(new File("Math.php"), metric, 0.0);
    verify(context).saveMeasure(new File("Math2.php"), metric, 0.0);
    verify(context).saveMeasure(new File("Mail.php"), metric, 2.0);
  }

  @Test
  public void testShouldGenerateNoccMeasure() {
    metric = CoreMetrics.NUMBER_OF_CHILDREN;
    init(SUMMARY_RESULT);
    verify(context).saveMeasure(new File("Math3.php"), metric, 0.0);
    verify(context).saveMeasure(new File("Bar/Math4.php"), metric, 0.0);
    verify(context).saveMeasure(new File("Foo/Math5.php"), metric, 0.0);
    verify(context).saveMeasure(new File("Math.php"), metric, 0.0);
    verify(context).saveMeasure(new File("Math2.php"), metric, 0.0);
    verify(context).saveMeasure(new File("Mail.php"), metric, 0.0);
  }

  @Test
  public void testShouldGenerateComplexityMeasure() {
    metric = CoreMetrics.COMPLEXITY;
    init(SUMMARY_RESULT);
    verify(context).saveMeasure(new File("Math3.php"), metric, 1.0);
    verify(context).saveMeasure(new File("Bar/Math4.php"), metric, 24.0);
    verify(context).saveMeasure(new File("Foo/Math5.php"), metric, 24.0);
    verify(context).saveMeasure(new File("Math.php"), metric, 24.0);
    verify(context).saveMeasure(new File("Math2.php"), metric, 24.0);
    verify(context).saveMeasure(new File("Mail.php"), metric, 120.0);
  }

  @Test
  /**
   * It was hard to have a seperate test case for class and method complexity.
   * Only assume because saveMeasure() method call arguments types are the same
   */
  public void testShouldGenerateComplexityDistribution() {
    init(SUMMARY_RESULT);

    verify(context).saveMeasure(
        eq(new File("Math3.php")),
        (Measure) argThat(new HasComplexityDistribution(CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION, "0=1;5=0;10=0;20=0;30=0;60=0;90=0"))
        );

    verify(context).saveMeasure(
        eq(new File("Math3.php")),
        (Measure) argThat(new HasComplexityDistribution(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, "1=4;2=0;4=0;6=0;8=0;10=2;12=0"))
        );

    verify(context).saveMeasure(
        eq(new File("Bar/Math4.php")),
        (Measure) argThat(new HasComplexityDistribution(CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION, "0=0;5=0;10=0;20=1;30=0;60=0;90=0"))
        );
    verify(context).saveMeasure(
        eq(new File("Bar/Math4.php")),
        (Measure) argThat(new HasComplexityDistribution(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, "1=2;2=0;4=0;6=0;8=0;10=2;12=0"))
        );

    verify(context).saveMeasure(
        eq(new File("Foo/Math5.php")),
        (Measure) argThat(new HasComplexityDistribution(CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION, "0=0;5=0;10=0;20=1;30=0;60=0;90=0"))
        );
    verify(context).saveMeasure(
        eq(new File("Foo/Math5.php")),
        (Measure) argThat(new HasComplexityDistribution(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, "1=2;2=0;4=0;6=0;8=0;10=2;12=0"))
        );

    verify(context).saveMeasure(
        eq(new File("Math.php")),
        (Measure) argThat(new HasComplexityDistribution(CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION, "0=0;5=0;10=0;20=1;30=0;60=0;90=0"))
        );
    verify(context).saveMeasure(
        eq(new File("Math.php")),
        (Measure) argThat(new HasComplexityDistribution(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, "1=3;2=0;4=0;6=0;8=0;10=2;12=0"))
        );

    verify(context).saveMeasure(
        eq(new File("Math2.php")),
        (Measure) argThat(new HasComplexityDistribution(CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION, "0=0;5=0;10=0;20=1;30=0;60=0;90=0"))
        );
    verify(context).saveMeasure(
        eq(new File("Math2.php")),
        (Measure) argThat(new HasComplexityDistribution(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, "1=2;2=0;4=0;6=0;8=0;10=2;12=0"))
        );

    verify(context).saveMeasure(
        eq(new File("Mail.php")),
        (Measure) argThat(new HasComplexityDistribution(CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION, "0=0;5=0;10=0;20=0;30=0;60=0;90=1"))
        );
    verify(context).saveMeasure(
        eq(new File("Mail.php")),
        (Measure) argThat(new HasComplexityDistribution(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, "1=38;2=18;4=4;6=1;8=2;10=0;12=0"))
        );
  }

  @Test
  public void shouldSupportTraitsInSummaryReport() {
    init("/org/sonar/plugins/php/phpdepend/sensor/parser/pdepend-traits-summary.xml");
  }
}
