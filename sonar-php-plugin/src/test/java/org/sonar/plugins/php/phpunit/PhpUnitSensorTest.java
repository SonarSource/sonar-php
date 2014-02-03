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
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.MockUtils;
import org.sonar.plugins.php.PhpPlugin;
import org.sonar.test.TestUtils;

import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PhpUnitSensorTest {



  @Mock
  private PhpUnitResultParser parser;

  @Mock
  private PhpUnitCoverageResultParser coverageParser;

  @Mock
  private SensorContext context;

  private Project project;
  private Settings settings;
  private PhpUnitSensor sensor;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);

    settings = newSettings();
    project = MockUtils.newMockPHPProject();
    sensor = new PhpUnitSensor(settings, parser, coverageParser);
  }

  @Test
  public void testToString() {
    assertThat(sensor.toString()).isEqualTo("PHPUnit Sensor");
  }

  @Test
  public void shouldExecuteOnProject() {
    assertThat(sensor.shouldExecuteOnProject(MockUtils.newMockJavaProject())).isFalse();
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }


  @Test
  public void testAnalyse() {
    sensor.analyse(project, context);

    verify(parser, times(1)).parse(TestUtils.getResource(MockUtils.PHPUNIT_REPORT));
    verify(coverageParser, times(1)).parse(TestUtils.getResource(MockUtils.PHPUNIT_COVERAGE_REPORT));
  }

  private static Settings newSettings() {
    Settings settings = new Settings();
    Properties props = new Properties();

    props.put(PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY, TestUtils.getResource(MockUtils.PHPUNIT_REPORT).getPath());
    props.put(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY, TestUtils.getResource(MockUtils.PHPUNIT_COVERAGE_REPORT).getPath());

    settings.addProperties(props);

    return settings;
  }
}
