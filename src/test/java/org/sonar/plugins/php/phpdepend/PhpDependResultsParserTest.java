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

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.sonar.commons.Metric;
import org.sonar.plugins.api.maven.ProjectContext;
import org.sonar.plugins.api.metrics.CoreMetrics;
import org.sonar.plugins.php.Php;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PhpDependResultsParserTest {

  private ProjectContext context;
  private PhpDependConfiguration config;
  private Map<Metric, String> attributeByMetrics;

  @Before
  public void before() {
    attributeByMetrics = new HashMap<Metric, String>();
  }

  private void init() {
    try {
      File xmlReport = new File(getClass().getResource("/org/sonar/plugins/php/phpdepend/PhpDependResultsParserTest/phpunit-report.xml").toURI());
      context = mock(ProjectContext.class);
      config = mock(PhpDependConfiguration.class);
      stub(config.getReportFile()).toReturn(xmlReport);

      PhpDependResultsParser parser = new PhpDependResultsParser(config, context, attributeByMetrics, Arrays.asList("C:\\projets\\PHP\\Money"));
      parser.parse();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test(expected = PhpDependExecutionException.class)
  public void shouldThrowAnExceptionWhenReportNotFound() {
    config = mock(PhpDependConfiguration.class);
    stub(config.getSourceDir()).toReturn(new File("C:\\projets\\PHP\\Money"));
    stub(config.getReportFile()).toReturn(new File("path/to/nowhere"));
    PhpDependResultsParser parser = new PhpDependResultsParser(config, null);
    parser.parse();
  }

  @Test
  public void shouldNotThrowExceptionIfAMetricIsNotPresent() {
    attributeByMetrics.put(new Metric("doesnt_exists"), "doesnt_exists");
    init();
  }

  @Test
  public void shouldGenerateLocMeasures() {
    Metric locMetric = CoreMetrics.LOC;
    attributeByMetrics.put(locMetric, "loc");
    init();

    verify(context).addMeasure(locMetric, 768.0);

    verify(context).addMeasure(Php.newDirectory("Sources"), locMetric, 506.0);
    verify(context).addMeasure(Php.newDirectory("Sources/Common"), locMetric, 74.0);

    verify(context).addMeasure(Php.newFile("Money.php"), locMetric, 188.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), locMetric, 251.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), locMetric, 255.0);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), locMetric, 74.0);
  }

  @Test
  public void shouldGenerateNclocMeasures() {
    Metric locMetric = CoreMetrics.NCLOC;
    attributeByMetrics.put(locMetric, "locExecutable");
    init();

    verify(context).addMeasure(locMetric, 411.0);

    verify(context).addMeasure(Php.newDirectory("Sources"), locMetric, 302.0);
    verify(context).addMeasure(Php.newDirectory("Sources/Common"), locMetric, 15.0);

    verify(context).addMeasure(Php.newFile("Money.php"), locMetric, 94.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), locMetric, 150.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), locMetric, 152.0);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), locMetric, 15.0);
  }


  @Test
  public void shouldGenerateFunctionsCountMeasure() {
    attributeByMetrics.put(CoreMetrics.FUNCTIONS_COUNT, "nom");
    init();
    verify(context).addMeasure(CoreMetrics.FUNCTIONS_COUNT, 66.0);
    verify(context).addMeasure(Php.newFile("Money.php"), CoreMetrics.FUNCTIONS_COUNT, 17.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), CoreMetrics.FUNCTIONS_COUNT, 18.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), CoreMetrics.FUNCTIONS_COUNT, 24.0);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), CoreMetrics.FUNCTIONS_COUNT, 8.0);

    verify(context).addMeasure(Php.newDirectory("Sources"), CoreMetrics.FUNCTIONS_COUNT, 42.0);
    verify(context).addMeasure(Php.newDirectory("Sources/Common"), CoreMetrics.FUNCTIONS_COUNT, 8.0);
  }

  @Test
  public void shouldGenerateClassesCountMeasure() {
    attributeByMetrics.put(CoreMetrics.CLASSES_COUNT, "classes");
    init();
    verify(context).addMeasure(CoreMetrics.CLASSES_COUNT, 5.0);
    verify(context).addMeasure(Php.newFile("Money.php"), CoreMetrics.CLASSES_COUNT, 2.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyBag.php"), CoreMetrics.CLASSES_COUNT, 1.0);
    verify(context).addMeasure(Php.newFile("Sources/MoneyTest.php"), CoreMetrics.CLASSES_COUNT, 1.0);
    verify(context).addMeasure(Php.newFile("Sources/Common/IMoney.php"), CoreMetrics.CLASSES_COUNT, 1.0);

    verify(context).addMeasure(Php.newDirectory("Sources"), CoreMetrics.CLASSES_COUNT, 2.0);
    verify(context).addMeasure(Php.newDirectory("Sources/Common"), CoreMetrics.CLASSES_COUNT, 1.0);
  }

  @Test
  public void shouldGenerateFilesCountMeasure() {
    attributeByMetrics.put(CoreMetrics.FILES_COUNT, "files");
    init();
    verify(context).addMeasure(CoreMetrics.FILES_COUNT, 4.0);
    verify(context).addMeasure(Php.newDirectory("Sources"), CoreMetrics.FILES_COUNT, 2.0);
    verify(context).addMeasure(Php.newDirectory("Sources/Common"), CoreMetrics.FILES_COUNT, 1.0);
  }

}
