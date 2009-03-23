/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
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

package org.sonar.plugins.php.phpdepend;

import org.junit.Test;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import org.sonar.commons.Metric;
import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.api.metrics.CoreMetrics;
import org.sonar.plugins.php.Php;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PhpDependResultsParserTest {

  private ProjectContext context;
  private PhpDependConfiguration config;
  private Metric metric;

  private void init() {
    try {
      File xmlReport = new File(getClass().getResource("/org/sonar/plugins/php/phpdepend/PhpDependResultsParserTest/phpunit-report.xml").toURI());
      context = mock(ProjectContext.class);
      config = mock(PhpDependConfiguration.class);
      when(config.getReportFile()).thenReturn(xmlReport);

      Set<Metric> metrics = new HashSet<Metric>();
      metrics.add(metric);
      PhpDependResultsParser parser = new PhpDependResultsParser(config, context, Arrays.asList("C:\\projets\\PHP\\Money"), metrics);
      parser.parse();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test(expected = PhpDependExecutionException.class)
  public void shouldThrowAnExceptionWhenReportNotFound() {
    config = mock(PhpDependConfiguration.class);
    when(config.getSourceDir()).thenReturn(new File("C:\\projets\\PHP\\Money"));
    when(config.getReportFile()).thenReturn(new File("path/to/nowhere"));
    PhpDependResultsParser parser = new PhpDependResultsParser(config, null);
    parser.parse();
  }

  @Test
  public void shouldNotThrowAnExceptionIfMetricNotFound() {
    metric = new Metric("not a true metric");
    init();
    verify(context, never()).addMeasure(eq(metric), anyDouble());
  }

  @Test
  public void shouldGenerateLocMeasures() {
    metric = CoreMetrics.LOC;
    init();

    verify(context).addMeasure(metric, 768.0);

    verify(context).addMeasure(Php.newDirectory("Sources"), metric, 506.0);
    verify(context).addMeasure(Php.newDirectory("Sources/Common"), metric, 74.0);

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 188.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 251.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 255.0);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), metric, 74.0);
  }

  @Test
  public void shouldGenerateNclocMeasures() {
    metric = CoreMetrics.NCLOC;
    init();

    verify(context).addMeasure(metric, 411.0);

    verify(context).addMeasure(Php.newDirectory("Sources"), metric, 302.0);
    verify(context).addMeasure(Php.newDirectory("Sources/Common"), metric, 15.0);

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 94.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 150.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 152.0);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), metric, 15.0);
  }


  @Test
  public void shouldGenerateFunctionsCountMeasure() {
    metric = CoreMetrics.FUNCTIONS_COUNT;
    init();

    verify(context).addMeasure(metric, 66.0);

    verify(context).addMeasure(Php.newDirectory("Sources"), metric, 42.0);
    verify(context).addMeasure(Php.newDirectory("Sources/Common"), metric, 8.0);

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 17.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 18.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 24.0);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), metric, 8.0);
  }

  @Test
  public void shouldGenerateClassesCountMeasure() {
    metric = CoreMetrics.CLASSES_COUNT;
    init();

    verify(context).addMeasure(metric, 5.0);

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 2.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 1.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 1.0);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), metric, 1.0);

    verify(context).addMeasure(Php.newDirectory("Sources"), metric, 2.0);
    verify(context).addMeasure(Php.newDirectory("Sources/Common"), metric, 1.0);
  }

  @Test
  public void shouldGenerateFilesCountMeasure() {
    metric = CoreMetrics.FILES_COUNT;
    init();

    verify(context).addMeasure(metric, 4.0);
    verify(context).addMeasure(Php.newDirectory("Sources"), metric, 2.0);
    verify(context).addMeasure(Php.newDirectory("Sources/Common"), metric, 1.0);
  }

  @Test
  public void shouldGenerateCommentsRatioCountMeasure() {
    metric = CoreMetrics.COMMENT_RATIO;
    init();

    verify(context).addMeasure(metric, 32.68229166666667);

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 36.17021276595745);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 22.31075697211155);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 27.84313725490196);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), metric, 75.67567567567568);

    verify(context).addMeasure(Php.newDirectory("Sources"), metric, 50.15389422701351);
    verify(context).addMeasure(Php.newDirectory("Sources/Common"), metric, 75.67567567567568);
  }

  @Test
  public void shouldGenerateComplextyMeasure() {
    metric = CoreMetrics.COMPLEXITY;
    init();

    verify(context).addMeasure(metric, 85.0);

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 22.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 39.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 24.0);
    verify(context, never()).addMeasure(eq(Php.newFile("Sources/Common/IMoney.php")), eq(metric), anyDouble());

    verify(context).addMeasure(Php.newDirectory("Sources"), metric, 63.0);
    verify(context, never()).addMeasure(eq(Php.newDirectory("Sources/Common")), eq(metric), anyDouble());
  }

  @Test
  public void shouldGenerateComplextyPerMethodMeasure() {
    metric = CoreMetrics.COMPLEXITY_AVG_BY_FUNCTION;
    init();

    verify(context).addMeasure(metric, 1.2878787878787878);

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 1.2941176470588236);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 2.1666666666666665);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 1.0);
    verify(context, never()).addMeasure(eq(Php.newFile("Sources/Common/IMoney.php")), eq(metric), anyDouble());

    verify(context).addMeasure(Php.newDirectory("Sources"), metric, 1.5);
    verify(context, never()).addMeasure(eq(Php.newDirectory("Sources/Common")), eq(metric), anyDouble());
  }

  @Test
  public void shouldGenerateComplextyPerClassMeasure() {
    metric = CoreMetrics.COMPLEXITY_AVG_BY_CLASS;
    init();

    verify(context).addMeasure(metric, 17.0);

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 11.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 39.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 24.0);
    verify(context, never()).addMeasure(eq(Php.newFile("Sources/Common/IMoney.php")), eq(metric), anyDouble());

    verify(context).addMeasure(Php.newDirectory("Sources"), metric, 31.5);
    verify(context, never()).addMeasure(eq(Php.newDirectory("Sources/Common")), eq(metric), anyDouble());
  }

}