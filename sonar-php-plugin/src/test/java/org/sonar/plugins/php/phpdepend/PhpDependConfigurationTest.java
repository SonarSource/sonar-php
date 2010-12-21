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

package org.sonar.plugins.php.phpdepend;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_EXCLUDE_PACKAGE_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_IGNORE_KEY;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;

/**
 * The Class PhpDependConfigurationTest.
 */
public class PhpDependConfigurationTest {

  private static final String REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = PhpDependConfiguration.PDEPEND_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;
  private static final String DEFAULT_REPORT_FILE_PATH = PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH;
  private static final String DEFAULT_REPORT_FILE_NAME = PhpDependConfiguration.PDEPEND_DEFAULT_REPORT_FILE_NAME;
  private static final String REPORT_FILE_NAME_PROPERTY_KEY = PhpDependConfiguration.PDEPEND_REPORT_FILE_NAME_PROPERTY_KEY;

  @Test
  public void testGetIgnoreDirsWithNotNull() {
    Project project = getMockProject();
    PhpDependConfiguration config = getWindowsConfiguration(project);
    Configuration c = project.getConfiguration();
    String[] excludeDirs = new String[] { "a", "b" };
    when(c.getStringArray(PDEPEND_IGNORE_KEY)).thenReturn(excludeDirs);
    assertEquals("a,b", config.getIgnoreDirs());
  }

  @Test
  public void testGetExcludePackageWithNull() {
    Project project = getMockProject();
    PhpDependConfiguration config = getWindowsConfiguration(project);
    Configuration c = project.getConfiguration();
    when(c.getStringArray(PDEPEND_EXCLUDE_PACKAGE_KEY)).thenReturn(null);
    assertEquals(null, config.getExcludePackages());
  }

  @Test
  public void testGetExcludePackageWithNotNull() {
    Project project = getMockProject();
    PhpDependConfiguration config = getWindowsConfiguration(project);
    Configuration c = project.getConfiguration();
    String[] excludeDirs = new String[] { "a", "b" };
    when(c.getStringArray(PDEPEND_EXCLUDE_PACKAGE_KEY)).thenReturn(excludeDirs);
    assertEquals("a,b", config.getExcludePackages());
  }

  @Test
  public void testGetIgnoreDirsWithNull() {
    Project project = getMockProject();
    PhpDependConfiguration config = getWindowsConfiguration(project);
    Configuration c = project.getConfiguration();
    when(c.getStringArray(PDEPEND_IGNORE_KEY)).thenReturn(null);
    assertEquals(null, config.getIgnoreDirs());
  }

  /**
   * Should get command line for windows.
   */
  @Test
  public void shouldGetCommandLineForWindows() {
    Project project = getMockProject();
    PhpDependConfiguration config = getWindowsConfiguration(project);

    assertThat(config.getOsDependentToolScriptName(), is(PhpDependConfiguration.PDEPEND_COMMAND_LINE + ".bat"));
  }

  /**
   * Should get command line for not windows.
   */
  @Test
  public void shouldGetCommandLineForNotWindows() {
    Project project = getMockProject();
    // new Php();
    PhpDependConfiguration config = getNotWindowsConfiguration(project);
    assertThat(config.getOsDependentToolScriptName(), is(PhpDependConfiguration.PDEPEND_COMMAND_LINE));
  }

  /**
   * S Should get valid suffixe option.
   */
  @Test
  public void shouldGetValidSuffixeOption() {
    Project project = getMockProject();
    PhpDependConfiguration config = new PhpDependConfiguration(project);

    String suffixesOption = config.getSuffixesCommandOption();
    assertThat(suffixesOption, notNullValue());
    assertThat(suffixesOption, containsString(","));
  }

  /**
   * Should get valid suffixe option.
   */
  @Test
  public void shouldReturnDefaultReportFileWithDefaultPath() {
    Project project = getMockProject();
    PhpDependConfiguration config = new PhpDependConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\logs\\pdepend.xml");
  }

  private Project getMockProject() {
    Project project = mock(Project.class);
    Configuration configuration = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
    when(configuration.getString(REPORT_FILE_NAME_PROPERTY_KEY, DEFAULT_REPORT_FILE_NAME)).thenReturn(DEFAULT_REPORT_FILE_NAME);
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, DEFAULT_REPORT_FILE_PATH)).thenReturn(DEFAULT_REPORT_FILE_PATH);
    when(project.getConfiguration()).thenReturn(configuration);
    return project;
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
    when(configuration.getString(REPORT_FILE_NAME_PROPERTY_KEY, DEFAULT_REPORT_FILE_NAME)).thenReturn(DEFAULT_REPORT_FILE_NAME);
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
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
    when(configuration.getString(REPORT_FILE_NAME_PROPERTY_KEY, DEFAULT_REPORT_FILE_NAME)).thenReturn("pdepend.summary.xml");
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpDependConfiguration config = new PhpDependConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\pdepend.summary.xml");
  }

  /**
   * Gets the windows configuration.
   * 
   * @return the windows configuration
   */
  private PhpDependConfiguration getWindowsConfiguration(Project project) {
    return getConfiguration(project, true, "");
  }

  /**
   * Gets the not windows configuration.
   * 
   * @return the not windows configuration
   */
  private PhpDependConfiguration getNotWindowsConfiguration(Project project) {
    return getConfiguration(project, false, "");
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

  private PhpDependConfiguration getConfiguration(Project project, final boolean isOsWindows, final String path) {
    PhpDependConfiguration config = new PhpDependConfiguration(project) {

      protected String getCommandLinePath() {
        return path;
      }

      @Override
      public boolean isOsWindows() {
        return isOsWindows;
      }
    };
    return config;
  }

}
