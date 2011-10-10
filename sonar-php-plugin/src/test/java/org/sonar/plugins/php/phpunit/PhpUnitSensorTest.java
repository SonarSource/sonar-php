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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.MockUtils;

public class PhpUnitSensorTest {

  @Test
  public void shouldLaunch() {
    Project project = MockUtils.createMockProject(new BaseConfiguration());
    PhpUnitExecutor executor = mock(PhpUnitExecutor.class);
    PhpUnitSensor sensor = createSensor(project, executor);

    assertEquals(true, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldNotLaunchOnNonPhpProject() {
    Project project = MockUtils.createMockProject(new BaseConfiguration());
    when(project.getLanguage()).thenReturn(Java.INSTANCE);

    PhpUnitExecutor executor = mock(PhpUnitExecutor.class);
    PhpUnitSensor sensor = createSensor(project, executor);

    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldNotLaunchIfNotDynamicAnalysis() {
    Configuration conf = new BaseConfiguration();
    conf.setProperty("sonar.dynamicAnalysis", "false");
    Project project = MockUtils.createMockProject(conf);
    PhpUnitExecutor executor = mock(PhpUnitExecutor.class);
    PhpUnitSensor sensor = createSensor(project, executor);

    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldNotLaunchIfBothSkip() {
    Configuration conf = new BaseConfiguration();
    conf.setProperty("sonar.phpUnit.skip", true);
    conf.setProperty("sonar.phpUnit.coverage.skip", true);
    Project project = MockUtils.createMockProject(conf);
    PhpUnitExecutor executor = mock(PhpUnitExecutor.class);
    PhpUnitSensor sensor = createSensor(project, executor);

    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldLaunchIfOnlyOneSkip() {
    Configuration conf = new BaseConfiguration();
    conf.setProperty("sonar.phpUnit.skip", true);
    Project project = MockUtils.createMockProject(conf);
    PhpUnitExecutor executor = mock(PhpUnitExecutor.class);
    PhpUnitSensor sensor = createSensor(project, executor);

    assertEquals(true, sensor.shouldExecuteOnProject(project));

    // and the other way around
    conf = new BaseConfiguration();
    conf.setProperty("sonar.phpUnit.coverage.skip", true);
    project = MockUtils.createMockProject(conf);
    executor = mock(PhpUnitExecutor.class);
    sensor = createSensor(project, executor);

    assertEquals(true, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void testAnalyse() {
    Project project = MockUtils.createMockProject(new BaseConfiguration());
    PhpUnitExecutor executor = mock(PhpUnitExecutor.class);
    PhpUnitSensor sensor = createSensor(project, executor);
    SensorContext context = mock(SensorContext.class);
    sensor.analyse(project, context);

    verify(executor, times(1)).execute();
  }

  @Test
  public void testAnalyseWithoutExecutingTool() {
    Configuration conf = new BaseConfiguration();
    conf.setProperty("sonar.phpUnit.analyzeOnly", true);
    Project project = MockUtils.createMockProject(conf);
    PhpUnitExecutor executor = mock(PhpUnitExecutor.class);
    PhpUnitSensor sensor = createSensor(project, executor);
    SensorContext context = mock(SensorContext.class);
    sensor.analyse(project, context);

    verify(executor, never()).execute();
  }

  protected PhpUnitSensor createSensor(Project project, PhpUnitExecutor executor) {
    PhpUnitResultParser parser = mock(PhpUnitResultParser.class);
    PhpUnitCoverageResultParser coverageResultParser = mock(PhpUnitCoverageResultParser.class);
    PhpUnitSensor sensor = new PhpUnitSensor(new PhpUnitConfiguration(project), executor, parser, coverageResultParser);
    return sensor;
  }

}
