/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Sonar PHP Plugin
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

package org.sonar.plugins.php.cpd;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_SHOULD_RUN_PROPERTY_KEY;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.core.PhpPluginExecutionException;

public class PhpPhpCpdSensorTest {

  @Test
  public void testShouldNotExecuteOnProjectWithNulPom() {
    Project project = createProject();
    project.setLanguage(Java.INSTANCE);
    project.setPom(null);

    PropertiesConfiguration conf = (PropertiesConfiguration) project.getConfiguration();
    conf.setProperty("sonar.cpd.skip", "false");

    testShouldRun(project, false);
  }

  @Test
  public void testShouldNotExecuteOnNonPhpProject() {
    Project project = createProject();
    project.setLanguage(Java.INSTANCE);
    PropertiesConfiguration conf = (PropertiesConfiguration) project.getConfiguration();
    conf.setProperty("sonar.cpd.skip", "false");

    testShouldRun(project, false);
  }

  @Test
  public void testShouldNotExecuteOnNonPhpProjectWithSonarCpdSkipIsTrue() {
    Project project = createProject();
    project.setLanguage(Java.INSTANCE);
    PropertiesConfiguration conf = (PropertiesConfiguration) project.getConfiguration();
    conf.setProperty("sonar.cpd.skip", "true");

    testShouldRun(project, false);
  }

  @Test
  public void testShouldExecuteOnProjectWhenCpdSkipPropertyFalse() {
    Project project = createProject();
    PropertiesConfiguration conf = (PropertiesConfiguration) project.getConfiguration();
    conf.setProperty("sonar.cpd.skip", "false");

    testShouldRun(project, true);
  }

  @Test
  public void testShouldNotExecuteOnProjectWhenCpdSkipPropertyTrue() {
    Project project = createProject();
    PropertiesConfiguration conf = (PropertiesConfiguration) project.getConfiguration();
    conf.setProperty("sonar.cpd.skip", "true");

    testShouldRun(project, false);
  }

  @Test
  public void testShouldExecuteOnProjectWhenCpdSkipPropertyNotSetButDeprecatedCpdSkipPropertyFalse() {
    Project project = createProject();
    PropertiesConfiguration conf = (PropertiesConfiguration) project.getConfiguration();
    conf.setProperty("sonar.php.cpd.skip", "false");

    testShouldRun(project, true);
  }

  @Test
  public void testShouldNotExecuteOnProjectWhenCpdSkipPropertyNotSetButDeprecatedCpdSkipPropertyTrue() {
    Project project = createProject();
    PropertiesConfiguration conf = (PropertiesConfiguration) project.getConfiguration();
    conf.setProperty("sonar.php.cpd.skip", "true");

    testShouldRun(project, false);
  }

  @Test
  public void testShouldExecuteOnProjectWhenBothCpdSkipPropertyNotSetButShouldRunPropertyTrue() {
    Project project = createProject();
    PropertiesConfiguration conf = (PropertiesConfiguration) project.getConfiguration();
    conf.setProperty(PHPCPD_SHOULD_RUN_PROPERTY_KEY, "true");

    testShouldRun(project, true);
  }

  @Test
  public void testShouldNotExecuteOnProjectWhenBothCpdSkipPropertyNotSetButShouldRunPropertyFalse() {
    Project project = createProject();
    PropertiesConfiguration conf = (PropertiesConfiguration) project.getConfiguration();
    conf.setProperty(PHPCPD_SHOULD_RUN_PROPERTY_KEY, "false");

    testShouldRun(project, false);
  }

  @Test
  public void testShouldExecuteOnProjectWhenCpdSkipPropertyAndShouldRunPropertyNotSet() {
    Project project = createProject();

    testShouldRun(project, true);
  }

  private void testShouldRun(Project project, boolean shouldRun) {

    PhpCpdExecutor executor = mock(PhpCpdExecutor.class);
    PhpCpdSensor sensor = getSensor(project, executor);
    assertEquals(shouldRun, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void generalSkip() {

    PhpCpdConfiguration configuration = mock(PhpCpdConfiguration.class);
    Project project = createProject();
    PropertiesConfiguration conf = (PropertiesConfiguration) project.getConfiguration();
    conf.setProperty("sonar.cpd.skip", "true");
    conf.setProperty(PHPCPD_SHOULD_RUN_PROPERTY_KEY, "false");

    PhpCpdSensor sensor = new PhpCpdSensor(configuration, null, null);
    assertFalse(sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void testAnalyze() {

    Project project = createProject();
    project.setPom(new MavenProject());
    PhpCpdExecutor executor = mock(PhpCpdExecutor.class);

    PhpCpdSensor sensor = getSensor(project, executor);
    assertTrue(sensor.shouldExecuteOnProject(project));
    SensorContext context = mock(SensorContext.class);

    sensor.analyse(project, context);

  }

  @Test
  public void AnalyzeExecutesTheToolWhenNotInAnalyzeOnlyMode() {

    Project project = createProject();

    PhpCpdConfiguration configuration = mock(PhpCpdConfiguration.class);
    when(configuration.isAnalyseOnly()).thenReturn(false);

    PhpCpdExecutor executor = mock(PhpCpdExecutor.class);

    PhpCpdSensor sensor = new PhpCpdSensor(configuration, executor, mock(PhpCpdResultParser.class));

    SensorContext context = mock(SensorContext.class);
    sensor.analyse(project, context);

    verify(executor).execute();
  }

  @Test
  public void AnalyzeDoesntExecuteTheToolWhenInAnalyzeOnlyMode() {

    Project project = createProject();

    PhpCpdConfiguration configuration = mock(PhpCpdConfiguration.class);
    when(configuration.isAnalyseOnly()).thenReturn(true);

    PhpCpdExecutor executor = mock(PhpCpdExecutor.class);

    PhpCpdSensor sensor = new PhpCpdSensor(configuration, executor, mock(PhpCpdResultParser.class));

    SensorContext context = mock(SensorContext.class);
    sensor.analyse(project, context);

    verify(executor, never()).execute();
  }

  @Test
  public void testAnalyzeExitsGracefullyOnError() {
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

    PhpCpdResultParser parser = mock(PhpCpdResultParser.class);

    PhpCpdSensor sensor = new PhpCpdSensor(configuration, executor, parser);

    return sensor;
  }

  private Project createProject() {
    PropertiesConfiguration conf = new PropertiesConfiguration();
    Project project = new Project("php_project");
    project.setConfiguration(conf);
    project.setPom(new MavenProject());
    project.setLanguage(Php.PHP);
    return project;
  }

}