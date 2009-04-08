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

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 188.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 251.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 255.0);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), metric, 74.0);
  }

  @Test
  public void shouldGenerateNclocMeasures() {
    metric = CoreMetrics.NCLOC;
    init();

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 94.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 150.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 152.0);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), metric, 15.0);
  }


  @Test
  public void shouldGenerateFunctionsCountMeasure() {
    metric = CoreMetrics.FUNCTIONS_COUNT;
    init();

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 17.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 18.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 24.0);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), metric, 8.0);
  }

  @Test
  public void shouldGenerateClassesCountMeasure() {
    metric = CoreMetrics.CLASSES_COUNT;
    init();

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 2.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 1.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 1.0);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), metric, 1.0);
  }

  @Test
  public void shouldGenerateComplextyMeasure() {
    metric = CoreMetrics.COMPLEXITY;
    init();

    verify(context).addMeasure(Php.newFile("Money.php"), metric, 22.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), metric, 39.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), metric, 24.0);
    verify(context, never()).addMeasure(eq(Php.newFile("Sources/Common/IMoney.php")), eq(metric), anyDouble());
  }

  @Test
  public void shouldNotGenerateDirOrProjectMeasures() {
    metric = CoreMetrics.LOC;
    init();

    verify(context, never()).addMeasure(eq(metric), anyDouble());

    verify(context, never()).addMeasure(eq(Php.newDirectory("Sources")), eq(metric), anyDouble());
    verify(context, never()).addMeasure(eq(Php.newDirectory("Sources/Common")), eq(metric), anyDouble());
  }

}