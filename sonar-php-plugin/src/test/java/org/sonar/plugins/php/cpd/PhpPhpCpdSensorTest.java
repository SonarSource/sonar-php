/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi or Jerome Tama or Frederic Leroy
 * mailto: akram.benaissi@free.fr or jerome.tama@codehaus.org
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

package org.sonar.plugins.php.cpd;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.PhpPluginExecutionException;

public class PhpPhpCpdSensorTest {

  @Test
  public void generalSkip() {
    PropertiesConfiguration conf = new PropertiesConfiguration();
    conf.setProperty("sonar.php.cpd.skip", "true");

    PhpCpdConfiguration configuration = mock(PhpCpdConfiguration.class);
    when(configuration.isShouldRun()).thenReturn(false);

    Project project = createProject().setConfiguration(conf);

    PhpCpdSensor sensor = new PhpCpdSensor(configuration, null, null);
    assertFalse(sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void testAnalyze() {
    PropertiesConfiguration conf = new PropertiesConfiguration();
    Project project = createProject().setConfiguration(conf);
    PhpCpdExecutor executor = mock(PhpCpdExecutor.class);

    PhpCpdSensor sensor = getSensor(project, executor);
    assertTrue(sensor.shouldExecuteOnProject(project));
    SensorContext context = mock(SensorContext.class);

    sensor.analyse(project, context);

  }

  @Test(expected = PhpPluginExecutionException.class)
  public void testAnalyzeStopWhenExecuteFail() {
    PropertiesConfiguration conf = new PropertiesConfiguration();
    Project project = createProject().setConfiguration(conf);
    PhpCpdExecutor executor = mock(PhpCpdExecutor.class);

    PhpCpdSensor sensor = getSensor(project, executor);
    assertTrue(sensor.shouldExecuteOnProject(project));
    SensorContext context = mock(SensorContext.class);
    doThrow(new PhpPluginExecutionException()).when(executor).execute();
    sensor.analyse(project, context);

  }

  /**
   * @param project
   * @return
   */
  private PhpCpdSensor getSensor(Project project, PhpCpdExecutor executor) {
    PhpCpdConfiguration configuration = mock(PhpCpdConfiguration.class);
    when(configuration.isShouldRun()).thenReturn(true);
    when(configuration.shouldExecuteOnProject(project)).thenReturn(true);

    PhpCpdResultParser parser = mock(PhpCpdResultParser.class);

    PhpCpdSensor sensor = new PhpCpdSensor(configuration, executor, parser);
    return sensor;
  }

  private Project createProject() {
    return new Project("php_project").setLanguageKey(Php.KEY);
  }

}