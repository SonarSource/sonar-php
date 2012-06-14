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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.PhpPluginExecutionException;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PhpDependSensorTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private PhpDependConfiguration phpConfig;

  @Mock
  private PhpDependExecutor executor;

  @Mock
  private PhpDependParserSelector parserSelector;

  @Mock
  private PhpDependResultsParser parser;

  @Mock
  private Project project;

  @Mock
  private SensorContext context;

  private PhpDependSensor sensor;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(project.getLanguageKey()).thenReturn("php");
    when(parserSelector.select()).thenReturn(parser);

    sensor = new PhpDependSensor(phpConfig, executor, parserSelector);
  }

  @Test
  public void testToString() {
    assertThat(sensor.toString()).isEqualTo("PHP Depend Sensor");
  }

  @Test
  public void shouldLaunch() {
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void shouldNotLaunchOnNonPhpProject() {
    when(project.getLanguageKey()).thenReturn("java");

    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
  }

  @Test
  public void shouldNotLaunchIfSkip() {
    when(phpConfig.isSkip()).thenReturn(true);

    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
  }

  @Test
  public void testAnalyse() {
    File report = new File("target/MockProject/target/report.xml");
    when(phpConfig.getReportFile()).thenReturn(report);

    sensor.analyse(project, context);

    verify(executor, times(1)).execute();
    verify(parser, times(1)).parse(report);
  }

  @Test
  public void testAnalyseWithoutExecutingTool() {
    File report = new File("target/MockProject/target/report.xml");
    when(phpConfig.getReportFile()).thenReturn(report);
    when(phpConfig.isAnalyseOnly()).thenReturn(true);

    sensor.analyse(project, context);

    verify(executor, never()).execute();
    verify(parser, times(1)).parse(report);
  }

  @Test
  public void testAnalyzeExitsGracefullyOnError() {
    doThrow(new PhpPluginExecutionException()).when(executor).execute();

    sensor.analyse(project, context);

    // No exception is thrown
  }
}
