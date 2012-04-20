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

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.Rule;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.MockUtils;
import org.sonar.plugins.php.core.PhpPluginExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PhpDependSensorTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void shouldNotLaunchOnNonPhpProject() {
    Project project = MockUtils.createMockProject(new BaseConfiguration());
    when(project.getLanguage()).thenReturn(Java.INSTANCE);

    PhpDependExecutor executor = mock(PhpDependExecutor.class);
    PhpDependSensor sensor = createSensor(project, executor);

    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldLaunch() {
    Project project = MockUtils.createMockProject(new BaseConfiguration());
    PhpDependExecutor executor = mock(PhpDependExecutor.class);
    PhpDependSensor sensor = createSensor(project, executor);

    assertEquals(true, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldNotLaunchIfSkip() {
    Configuration conf = new BaseConfiguration();
    conf.setProperty("sonar.phpDepend.skip", true);
    Project project = MockUtils.createMockProject(conf);
    PhpDependExecutor executor = mock(PhpDependExecutor.class);
    PhpDependSensor sensor = createSensor(project, executor);

    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void testAnalyse() {
    Project project = MockUtils.createMockProject(new BaseConfiguration());
    PhpDependExecutor executor = mock(PhpDependExecutor.class);
    PhpDependSensor sensor = createSensor(project, executor);
    SensorContext context = mock(SensorContext.class);
    sensor.analyse(project, context);

    verify(executor, times(1)).execute();
  }

  @Test
  public void testAnalyseWithoutExecutingTool() {
    Configuration conf = new BaseConfiguration();
    conf.setProperty("sonar.phpDepend.analyzeOnly", true);
    Project project = MockUtils.createMockProject(conf);
    PhpDependExecutor executor = mock(PhpDependExecutor.class);
    PhpDependSensor sensor = createSensor(project, executor);
    SensorContext context = mock(SensorContext.class);
    sensor.analyse(project, context);

    verify(executor, never()).execute();
  }

  @Test
  public void testAnalyzeExitsGracefullyOnError() {
    Project project = MockUtils.createMockProject(new BaseConfiguration());
    PhpDependExecutor executor = mock(PhpDependExecutor.class);
    doThrow(new PhpPluginExecutionException()).when(executor).execute();
    PhpDependSensor sensor = createSensor(project, executor);
    SensorContext context = mock(SensorContext.class);
    sensor.analyse(project, context);
  }

  @Test
  public void testAnalyseSummaryXmlFile() {
    Project project = MockUtils.createMockProject(new BaseConfiguration());
    SensorContext context = mock(SensorContext.class);
    PhpDependExecutor executor = mock(PhpDependExecutor.class);
    PhpDependSummaryReportParser parser = new PhpDependSummaryReportParser(project, context);
    PhpDependSensor sensor = createSensor(project, executor, parser);
    exception.expect(SonarException.class);
    exception.expectMessage("Php Depend summary-xml report parser is not ready yet. Please switch to phpunit-xml for now.");
    sensor.analyse(project, context);

    verify(executor, times(1)).execute();
  }

  protected PhpDependSensor createSensor(Project project, PhpDependExecutor executor) {
    PhpDependPhpUnitReportParser parser = mock(PhpDependPhpUnitReportParser.class);
    return createSensor(project, executor, parser);
  }

  protected PhpDependSensor createSensor(Project project, PhpDependExecutor executor, PhpDependResultsParser parser) {
    PhpDependConfiguration conf = new PhpDependConfiguration(project);
    PhpDependParserSelector parserSelector = mock(PhpDependParserSelector.class);
    when(parserSelector.select(conf)).thenReturn(parser);
    PhpDependSensor sensor = new PhpDependSensor(conf, executor, parserSelector);
    return sensor;
  }
}
