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

package org.sonar.plugins.php.phpunit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.php.core.Php;

public class PhpUnitSensorTest {

  private static final String COVERAGE_REPORT_FILE_PROPERTY_KEY = PhpUnitConfiguration.PHPUNIT_COVERAGE_REPORT_FILE_PROPERTY_KEY;

  private static final String SHOULD_RUN_PROPERTY_KEY = PhpUnitConfiguration.PHPUNIT_SHOULD_RUN_PROPERTY_KEY;

  private static final String REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = PhpUnitConfiguration.PHPUNIT_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;

  private static final String DEFAULT_REPORT_FILE_NAME = PhpUnitConfiguration.PHPUNIT_DEFAULT_REPORT_FILE_NAME;

  private static final String REPORT_FILE_NAME_PROPERTY_KEY = PhpUnitConfiguration.PHPUNIT_REPORT_FILE_NAME_PROPERTY_KEY;

  private Project project;

  private Configuration config;

  /**
   * Sould not launch on non php project.
   */
  @Test
  public void shouldNotLaunchOnNonPhpProject() {
    Project project = mock(Project.class);
    when(project.getLanguage()).thenReturn(Java.INSTANCE);
    PhpUnitConfiguration configuration = mock(PhpUnitConfiguration.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fs);
    String defaultReportFilePath = PhpUnitConfiguration.PHPUNIT_DEFAULT_REPORT_FILE_PATH;
    when(fs.getBuildDir()).thenReturn(new File(defaultReportFilePath));

    when(configuration.getReportFileNameKey()).thenReturn("punit.summary.xml");
    when(configuration.getCoverageReportFile()).thenReturn(new File("punit.summary.xml"));
    Configuration config = mock(Configuration.class);
    when(config.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, defaultReportFilePath)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(config);

    PhpUnitExecutor executor = mock(PhpUnitExecutor.class);
    PhpUnitResultParser parser = mock(PhpUnitResultParser.class);
    PhpUnitCoverageResultParser parser2 = mock(PhpUnitCoverageResultParser.class);
    PhpUnitSensor sensor = new PhpUnitSensor(executor, parser, parser2);
    when(executor.getConfiguration()).thenReturn(configuration);

    assertEquals(false, sensor.shouldExecuteOnProject(project));
    when(project.getLanguage()).thenReturn(Php.INSTANCE);
    when(configuration.isShouldRun()).thenReturn(false);

    when(executor.getConfiguration()).thenReturn(configuration);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  /**
   * Sould not launch on non php project.
   */
  @Test
  public void shouldNotLaunchWhenConfiguredSoOnPhpProject() {
    init();
    when(project.getLanguage()).thenReturn(Php.INSTANCE);
    when(config.getBoolean(SHOULD_RUN_PROPERTY_KEY, Boolean.getBoolean(PhpUnitConfiguration.PHPUNIT_DEFAULT_SHOULD_RUN))).thenReturn(false);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getBuildDir()).thenReturn(new File(PhpUnitConfiguration.PHPUNIT_DEFAULT_REPORT_FILE_PATH));

    PhpUnitExecutor executor = mock(PhpUnitExecutor.class);
    PhpUnitResultParser parser = mock(PhpUnitResultParser.class);
    PhpUnitCoverageResultParser parser2 = mock(PhpUnitCoverageResultParser.class);
    PhpUnitSensor sensor = new PhpUnitSensor(executor, parser, parser2);
    PhpUnitConfiguration configuration = mock(PhpUnitConfiguration.class);
    when(executor.getConfiguration()).thenReturn(configuration);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  private void init() {
    project = mock(Project.class);
    config = mock(Configuration.class);

    when(config.getString(REPORT_FILE_NAME_PROPERTY_KEY, DEFAULT_REPORT_FILE_NAME)).thenReturn("punit.summary.xml");
    when(config.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PhpUnitConfiguration.PHPUNIT_DEFAULT_REPORT_FILE_PATH)).thenReturn("/log");
    String file = "phpunit.coverage.xml";
    when(config.getString(COVERAGE_REPORT_FILE_PROPERTY_KEY, PhpUnitConfiguration.PHPUNIT_DEFAULT_COVERAGE_REPORT_FILE)).thenReturn(file);
    when(config.getBoolean(SHOULD_RUN_PROPERTY_KEY, Boolean.getBoolean(PhpUnitConfiguration.PHPUNIT_DEFAULT_SHOULD_RUN))).thenReturn(true);
    when(project.getConfiguration()).thenReturn(config);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getBuildDir()).thenReturn(new File(PhpUnitConfiguration.PHPUNIT_DEFAULT_REPORT_FILE_PATH));
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\test")));
  }
}
