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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.MockUtils;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PhpUnitSensorTest {

  @Mock
  private PhpUnitConfiguration phpConfig;

  @Mock
  private PhpUnitExecutor executor;

  @Mock
  private PhpUnitResultParser parser;

  @Mock
  private PhpUnitCoverageResultParser coverageParser;

  @Mock
  private Project project;

  @Mock
  private SensorContext context;

  private PhpUnitSensor sensor;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(project.getLanguageKey()).thenReturn("php");
    when(phpConfig.isDynamicAnalysisEnabled()).thenReturn(true);

    sensor = new PhpUnitSensor(phpConfig, executor, parser, coverageParser);
  }

  @Test
  public void testToString() {
    assertThat(sensor.toString()).isEqualTo("PHPUnit Sensor");
  }

  @Test
  public void shouldLaunch() {
    project = MockUtils.createMockProject();
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void shouldNotLaunchOnNonPhpProject() {
    when(project.getLanguageKey()).thenReturn("java");

    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
  }

  @Test
  public void shouldNotLaunchIfNotDynamicAnalysis() {
    when(phpConfig.isDynamicAnalysisEnabled()).thenReturn(false);

    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
  }

  @Test
  public void shouldNotLaunchIfSkipTestAndCoverage() {
    when(phpConfig.isSkip()).thenReturn(true);
    when(phpConfig.shouldSkipCoverage()).thenReturn(true);

    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
  }

  @Test
  public void shouldLaunchIfSkipCoverageButNotTests() {
    project = MockUtils.createMockProject();
    when(phpConfig.shouldSkipCoverage()).thenReturn(true);

    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void testAnalyse() {
    File report = new File("target/MockProject/target/report.xml");
    when(phpConfig.getReportFile()).thenReturn(report);
    File coverageReport = new File("target/MockProject/target/coverage-report.xml");
    when(phpConfig.getCoverageReportFile()).thenReturn(coverageReport);

    sensor.analyse(project, context);

    verify(executor, times(1)).execute();
    verify(parser, times(1)).parse(report);
    verify(coverageParser, times(1)).parse(coverageReport, false);
  }

  @Test
  public void testAnalyseWithoutCoverage() {
    when(phpConfig.shouldSkipCoverage()).thenReturn(true);
    File report = new File("target/MockProject/target/report.xml");
    when(phpConfig.getReportFile()).thenReturn(report);

    sensor.analyse(project, context);

    verify(executor, times(1)).execute();
    verify(parser, times(1)).parse(report);
    verify(coverageParser, never()).parse(report, false);
  }

  @Test
  public void testAnalyseWithoutExecutingTool() {
    when(phpConfig.isAnalyseOnly()).thenReturn(true);
    File report = new File("target/MockProject/target/report.xml");
    when(phpConfig.getReportFile()).thenReturn(report);
    File coverageReport = new File("target/MockProject/target/coverage-report.xml");
    when(phpConfig.getCoverageReportFile()).thenReturn(coverageReport);

    sensor.analyse(project, context);

    verify(executor, never()).execute();
    verify(parser, times(1)).parse(report);
    verify(coverageParser, times(1)).parse(coverageReport, false);
  }

}
