/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
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

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.plugins.php.MockUtils;
import org.sonar.plugins.php.PhpPlugin;
import org.sonar.test.TestUtils;

import java.io.File;
import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PhpUnitSensorTest {

  @Rule
  public ExpectedException expected = ExpectedException.none();

  private final ModuleFileSystem projectFileSystem = mockModuleFileSystem();

  @Mock
  private PhpUnitResultParser parser;

  @Mock
  private PhpUnitCoverageResultParser coverageParser;

  @Mock
  private SensorContext context;

  private Project project;
  private Settings settings;
  private PhpUnitSensor sensor;
  private static final File TEST_REPORT_FILE = TestUtils.getResource(MockUtils.PHPUNIT_REPORT);
  private static final File COVERAGE_REPORT_FILE = TestUtils.getResource(MockUtils.PHPUNIT_COVERAGE_REPORT);

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(this);

    settings = newSettings();
    project = mock(Project.class);
    sensor = new PhpUnitSensor(projectFileSystem, settings, parser, coverageParser);
  }

  @Test
  public void testToString() {
    assertThat(sensor.toString()).isEqualTo("PHPUnit Sensor");
  }

  @Test
  public void shouldExecuteOnProject() {
    when(projectFileSystem.files(any(FileQuery.class))).thenReturn(ImmutableList.<File>of());
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    when(projectFileSystem.files(any(FileQuery.class))).thenReturn(ImmutableList.<File>of(mock(File.class)));
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void shouldParserReport() {
    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    sensor = new PhpUnitSensor(fs, settings, parser, coverageParser);

    sensor.analyse(project, context);

    verify(parser, times(1)).parse(TEST_REPORT_FILE);
    verify(coverageParser, times(1)).parse(COVERAGE_REPORT_FILE);
  }

  @Test
  public void noReport() {
    sensor = new PhpUnitSensor(mock(ModuleFileSystem.class), new Settings(), parser, coverageParser);
    sensor.analyse(project, context);

    verify(parser, never()).parse(any(File.class));
  }

  @Test
  public void badReport() {
    String fakePath = "/fake/path.xml";

    Settings localSettings = new Settings();
    Properties props = new Properties();
    props.put(PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY, fakePath);
    localSettings.addProperties(props);

    sensor = new PhpUnitSensor(mock(ModuleFileSystem.class), localSettings, parser, coverageParser);
    sensor.analyse(project, context);

    verify(parser, never()).parse(any(File.class));
  }

  @Test
  public void should_parse_relative_path_report() {
    Settings settings = new Settings();
    Properties props = new Properties();
    props.put(PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY, "phpunit.xml");
    settings.addProperties(props);
    sensor = new PhpUnitSensor(projectFileSystem, settings, parser, coverageParser);

    sensor.analyse(project, context);

    verify(parser, times(1)).parse(TEST_REPORT_FILE);
  }

  private static Settings newSettings() {
    Settings settings = new Settings();
    Properties props = new Properties();

    props.put(PhpPlugin.PHPUNIT_TESTS_REPORT_PATH_KEY, PhpUnitSensorTest.TEST_REPORT_FILE.getAbsolutePath());
    props.put(PhpPlugin.PHPUNIT_COVERAGE_REPORT_PATH_KEY, PhpUnitSensorTest.COVERAGE_REPORT_FILE.getAbsolutePath());

    settings.addProperties(props);

    return settings;
  }

  private ModuleFileSystem mockModuleFileSystem() {
    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    when(fs.baseDir()).thenReturn(TestUtils.getResource(MockUtils.PHPUNIT_REPORT_DIR));
    return fs;
  }
}
