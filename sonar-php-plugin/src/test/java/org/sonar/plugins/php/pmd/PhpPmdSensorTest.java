/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 Akram Ben Aissi
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

package org.sonar.plugins.php.pmd;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.Php;

/**
 * The Class PhpPmdSensorTest.
 */
public class PhpPmdSensorTest {

  @Before
  public void setUp() {
    // new Php();
  }

  /**
   * Sould not launch on non php project.
   */
  @Test
  public void shouldNotLaunchOnNonPhpProject() {
    Project project = getMockProject(Java.INSTANCE);
    PhpmdSensor sensor = getSensor(project);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  /**
   * @param project
   * @return
   */
  private PhpmdSensor getSensor(Project project) {
    RulesProfile profile = mock(RulesProfile.class);
    RuleFinder finder = mock(RuleFinder.class);
    PhpmdSensor sensor = new PhpmdSensor(profile, finder, Php.INSTANCE);
    return sensor;
  }

  /**
   * Sould launch on php project.
   */
  @Test
  public void shouldLaunchOnPhpProject() {
    Project project = getMockProject(Php.INSTANCE);
    PhpmdSensor sensor = getSensor(project);
    assertEquals(false, sensor.shouldExecuteOnProject(project));
  }

  /**
   * Sould not launch parsing when no report can be found.
   */
  @Test(expected = SonarException.class)
  public void shouldNotLaunchParsingWhenNoReportCanBeFound() {
    Project project = mock(Project.class);
    Configuration configuration = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
    when(configuration.getString(PhpmdConfiguration.PHPMD_REPORT_FILE_NAME_PROPERTY_KEY, PhpmdConfiguration.PHPMD_DEFAULT_REPORT_FILE_NAME))
        .thenReturn("tot.xml");
    when(
        configuration.getString(PhpmdConfiguration.PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpmdConfiguration.PHPMD_DEFAULT_REPORT_FILE_PATH)).thenReturn(PhpmdConfiguration.PHPMD_DEFAULT_REPORT_FILE_PATH);
    when(configuration.getBoolean(PhpmdConfiguration.PHPMD_ANALYZE_ONLY_KEY, false)).thenReturn(true);
    when(project.getConfiguration()).thenReturn(configuration);
    when(project.getLanguage()).thenReturn(Php.INSTANCE);
    PhpmdSensor sensor = getSensor(project);
    SensorContext context = mock(SensorContext.class);
    sensor.analyse(project, context);
    verify(context, never()).saveViolation(any(Violation.class));
  }

  /**
   * @return a mock project used by all tests cases in this class.
   */
  private Project getMockProject(AbstractLanguage language) {
    Project project = mock(Project.class);
    when(project.getLanguage()).thenReturn(language);
    Configuration configuration = mock(Configuration.class);

    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
    when(configuration.getString(PhpmdConfiguration.PHPMD_REPORT_FILE_NAME_PROPERTY_KEY, PhpmdConfiguration.PHPMD_DEFAULT_REPORT_FILE_NAME))
        .thenReturn("tot.xml");
    when(
        configuration.getString(PhpmdConfiguration.PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpmdConfiguration.PHPMD_DEFAULT_REPORT_FILE_PATH)).thenReturn(PhpmdConfiguration.PHPMD_DEFAULT_REPORT_FILE_PATH);
    when(project.getConfiguration()).thenReturn(configuration);
    return project;
  }

}
