/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 MyCompany
 * mailto:contact AT sonarsource DOT com
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

package org.sonar.plugins.php.phpdepend.sensor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.phpdepend.configuration.PhpDependConfiguration;

public class PhpDependSensorTest {

  @Test
  public void shouldNotLaunchOnNonPhpProject() {
    Project project = mock(Project.class);
    when(project.getLanguage()).thenReturn(Java.INSTANCE);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getBuildDir()).thenReturn(new File(PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH));
    Configuration configuration = mock(Configuration.class);
    PhpDependSensor sensor = new PhpDependSensor();
    when(configuration.getString(PhpDependConfiguration.REPORT_FILE_NAME_PROPERTY_KEY, PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME))
        .thenReturn("pdepend.xml");
    when(
        configuration.getString(PhpDependConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpDependConfiguration config = mock(PhpDependConfiguration.class);
    when(config.isShouldRun()).thenReturn(true);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
    when(config.isShouldRun()).thenReturn(false);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldLaunchOnPhpProjectIfConfiguredSo() {
    Project project = mock(Project.class);
    when(project.getLanguage()).thenReturn(Php.INSTANCE);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getBuildDir()).thenReturn(new File(PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME));
    Configuration configuration = mock(Configuration.class);
    PhpDependSensor sensor = new PhpDependSensor();
    when(configuration.getString(PhpDependConfiguration.REPORT_FILE_NAME_PROPERTY_KEY, PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME))
        .thenReturn("pdepend.xml");
    when(
        configuration.getString(PhpDependConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(
        configuration.getBoolean(PhpDependConfiguration.SHOULD_RUN_PROPERTY_KEY, Boolean
            .parseBoolean(PhpDependConfiguration.DEFAULT_SHOULD_RUN))).thenReturn(Boolean.TRUE);
    when(project.getConfiguration()).thenReturn(configuration);
    assertEquals(true, sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void shouldNotLaunchOnPhpProjectIfConfiguredSo() {
    Project project = mock(Project.class);
    when(project.getLanguage()).thenReturn(Php.INSTANCE);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getBuildDir()).thenReturn(new File(PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH));
    Configuration configuration = mock(Configuration.class);
    PhpDependSensor sensor = new PhpDependSensor();
    when(configuration.getString(PhpDependConfiguration.REPORT_FILE_NAME_PROPERTY_KEY, PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME))
        .thenReturn("pdepend.xml");
    when(
        configuration.getString(PhpDependConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(configuration.getBoolean(PhpDependConfiguration.SHOULD_RUN_PROPERTY_KEY, Boolean.FALSE)).thenReturn(Boolean.FALSE);
    when(project.getConfiguration()).thenReturn(configuration);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }
}
