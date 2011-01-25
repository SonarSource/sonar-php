/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_REPORT_FILE_NAME_PROPERTY_KEY;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.php.core.Php;

public class PhpDependSensorTest {

  private static final String DEFAULT_REPORT_FILE_NAME = PhpDependConfiguration.PDEPEND_DEFAULT_REPORT_FILE_NAME;
  private static final String DEFAULT_REPORT_FILE_PATH = PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH;
  private static final String SHOULD_RUN_PROPERTY_KEY = PhpDependConfiguration.PDEPEND_SHOULD_RUN_PROPERTY_KEY;
  private static final String DEFAULT_SHOULD_RUN = PhpDependConfiguration.PDEPEND_DEFAULT_SHOULD_RUN;
  private static final String REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = PhpDependConfiguration.PDEPEND_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;

  @Test
  public void shouldNotLaunchOnNonPhpProject() {
    Project project = mock(Project.class);
    when(project.getLanguage()).thenReturn(Java.INSTANCE);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getBuildDir()).thenReturn(new File(DEFAULT_REPORT_FILE_PATH));
    Configuration configuration = mock(Configuration.class);
    PhpDependResultsParser parser = mock(PhpDependResultsParser.class);
    PhpDependExecutor executor = mock(PhpDependExecutor.class);
    PhpDependSensor sensor = new PhpDependSensor(executor, parser);
    PhpDependConfiguration config = mock(PhpDependConfiguration.class);
    when(config.getProject()).thenReturn(project);
    when(executor.getConfiguration()).thenReturn(config);

    when(configuration.getString(PhpDependConfiguration.PDEPEND_REPORT_FILE_NAME_PROPERTY_KEY, DEFAULT_REPORT_FILE_NAME)).thenReturn(
        "pdepend.xml");
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);

    // when(config.isShouldRun()).thenReturn(true);
    // new Php();

    assertEquals(false, sensor.shouldExecuteOnProject(project));

    // when(config.isShouldRun()).thenReturn(false);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldLaunchOnPhpProjectIfConfiguredSo() {
    Project project = mock(Project.class);
    when(project.getLanguage()).thenReturn(Php.PHP);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getBuildDir()).thenReturn(new File(DEFAULT_REPORT_FILE_NAME));
    Configuration configuration = mock(Configuration.class);

    PhpDependResultsParser parser = mock(PhpDependResultsParser.class);
    PhpDependExecutor executor = mock(PhpDependExecutor.class);
    PhpDependSensor sensor = new PhpDependSensor(executor, parser);
    PhpDependConfiguration config = mock(PhpDependConfiguration.class);
    when(executor.getConfiguration()).thenReturn(config);
    when(config.getProject()).thenReturn(project);

    String reportFileNamePropertyKey = PDEPEND_REPORT_FILE_NAME_PROPERTY_KEY;
    when(configuration.getString(reportFileNamePropertyKey, DEFAULT_REPORT_FILE_NAME)).thenReturn("pdepend.xml");
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    // when(config.shouldExecuteOnProject()).thenReturn(true);
    when(configuration.getBoolean(SHOULD_RUN_PROPERTY_KEY, Boolean.parseBoolean(DEFAULT_SHOULD_RUN))).thenReturn(Boolean.TRUE);
    when(project.getConfiguration()).thenReturn(configuration);
    when(project.getLanguage()).thenReturn(Php.PHP);
    when(project.getPom()).thenReturn(new MavenProject());
    assertEquals(true, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldNotLaunchOnPhpProjectIfConfiguredSo() {
    Project project = mock(Project.class);
    when(project.getLanguage()).thenReturn(Php.PHP);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getBuildDir()).thenReturn(new File(DEFAULT_REPORT_FILE_PATH));
    Configuration configuration = mock(Configuration.class);

    PhpDependResultsParser parser = mock(PhpDependResultsParser.class);
    PhpDependExecutor executor = mock(PhpDependExecutor.class);
    PhpDependSensor sensor = new PhpDependSensor(executor, parser);
    PhpDependConfiguration config = mock(PhpDependConfiguration.class);
    when(executor.getConfiguration()).thenReturn(config);

    when(configuration.getString(PDEPEND_REPORT_FILE_NAME_PROPERTY_KEY, DEFAULT_REPORT_FILE_NAME)).thenReturn("pdepend.xml");
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(configuration.getBoolean(SHOULD_RUN_PROPERTY_KEY, Boolean.FALSE)).thenReturn(Boolean.FALSE);
    when(project.getConfiguration()).thenReturn(configuration);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }
}
