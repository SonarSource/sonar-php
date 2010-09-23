/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
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

package org.sonar.plugins.php.phpdepend.configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.php.core.Php;

/**
 * The Class PhpDependConfigurationTest.
 */
public class PhpDependConfigurationTest {

  /**
   * Should get command line for windows.
   */
  @Test
  public void shouldGetCommandLineForWindows() {
    PhpDependConfiguration config = getWindowsConfiguration();
    assertThat(config.getOsDependentToolScriptName(), is(PhpDependConfiguration.COMMAND_LINE + ".bat"));
  }

  /**
   * Should get command line for not windows.
   */
  @Test
  public void shouldGetCommandLineForNotWindows() {
    PhpDependConfiguration config = getNotWindowsConfiguration();
    assertThat(config.getOsDependentToolScriptName(), is(PhpDependConfiguration.COMMAND_LINE));
  }

  /**
   * Should get valid suffixe option.
   */
  @Test
  public void shouldGetValidSuffixeOption() {
    Configuration configuration = mock(Configuration.class);
    Php php = new Php(configuration);
    PhpDependConfiguration config = getWindowsConfiguration();
    String suffixesOption = config.getSuffixesCommandOption();
    assertThat(suffixesOption, notNullValue());
    assertThat(suffixesOption, containsString(","));
  }

  /**
   * Should get valid suffixe option.
   */
  @Test
  public void shouldReturnDefaultReportFileWithDefaultPath() {
    Project project = mock(Project.class);
    Configuration configuration = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
    when(configuration.getString(PhpDependConfiguration.REPORT_FILE_NAME_PROPERTY_KEY, PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME))
        .thenReturn(PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME);
    when(
        configuration.getString(PhpDependConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn(PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH);
    when(project.getConfiguration()).thenReturn(configuration);
    PhpDependConfiguration config = new PhpDependConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\logs\\pdepend.xml");
  }

  /**
   * Should get valid suffixe option.
   */
  @Test
  public void shouldReturnDefaultReportFileWithCustomPath() {
    Project project = mock(Project.class);
    Configuration configuration = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
    when(configuration.getString(PhpDependConfiguration.REPORT_FILE_NAME_PROPERTY_KEY, PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME))
        .thenReturn(PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME);
    when(
        configuration.getString(PhpDependConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpDependConfiguration config = new PhpDependConfiguration(project);
    System.out.println(config.getReportFile().getPath().replace('/', '\\'));
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\pdepend.xml");
  }

  @Test
  public void shouldReturnCustomReportFileWithCustomPath() {
    Project project = mock(Project.class);
    Configuration configuration = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
    when(configuration.getString(PhpDependConfiguration.REPORT_FILE_NAME_PROPERTY_KEY, PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME))
        .thenReturn("pdepend.summary.xml");
    when(
        configuration.getString(PhpDependConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpDependConfiguration config = new PhpDependConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\pdepend.summary.xml");
  }

  /**
   * Gets the windows configuration.
   * 
   * @return the windows configuration
   */
  private PhpDependConfiguration getWindowsConfiguration() {
    return getConfiguration(true, "");
  }

  /**
   * Gets the not windows configuration.
   * 
   * @return the not windows configuration
   */
  private PhpDependConfiguration getNotWindowsConfiguration() {
    return getConfiguration(false, "");
  }

  /**
   * Gets the configuration.
   * 
   * @param isOsWindows
   *          the is os windows
   * @param path
   *          the path
   * @return the configuration
   */

  private PhpDependConfiguration getConfiguration(final boolean isOsWindows, final String path) {
    PhpDependConfiguration config = new PhpDependConfiguration() {

      protected String getCommandLinePath() {
        return path;
      }

      @Override
      protected boolean isOsWindows() {
        return isOsWindows;
      }
    };
    return config;
  }

}
