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

package org.sonar.plugins.php.codesniffer.configuration;

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

/**
 * The Class PhpDependConfigurationTest.
 */
public class PhpCodesnifferConfigurationTest {

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
    when(
        configuration.getString(PhpCodesnifferConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
            PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn(PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_NAME);
    when(
        configuration.getString(PhpCodesnifferConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn(PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_PATH);
    when(project.getConfiguration()).thenReturn(configuration);
    PhpCodesnifferConfiguration config = new PhpCodesnifferConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\logs\\codesniffer.xml");
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
    when(
        configuration.getString(PhpCodesnifferConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
            PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn(PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_NAME);
    when(
        configuration.getString(PhpCodesnifferConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpCodesnifferConfiguration config = new PhpCodesnifferConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\codesniffer.xml");
  }

  /**
   * Should return custom report file with custom path.
   */
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
    when(
        configuration.getString(PhpCodesnifferConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
            PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn("codesniffer-summary.xml");
    when(
        configuration.getString(PhpCodesnifferConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpCodesnifferConfiguration config = new PhpCodesnifferConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\codesniffer-summary.xml");
  }

  /**
   * Should return custom report file with custom path.
   */
  @Test
  public void shouldReturnIgnoreList() {
    Project project = mock(Project.class);
    Configuration configuration = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
    when(
        configuration.getString(PhpCodesnifferConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
            PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn("codesniffer-summary.xml");
    when(
        configuration.getString(PhpCodesnifferConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpCodesnifferConfiguration config = new PhpCodesnifferConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\codesniffer-summary.xml");

    when(project.getConfiguration().getStringArray(PhpCodesnifferConfiguration.IGNORE_ARGUMENT_KEY)).thenReturn(new String[] { "a", "b" });
    assertEquals("a,b", config.getIgnoreList());

    when(project.getConfiguration().getStringArray(PhpCodesnifferConfiguration.IGNORE_ARGUMENT_KEY)).thenReturn(new String[] { "a" });
    assertEquals("a", config.getIgnoreList());

    when(project.getConfiguration().getStringArray(PhpCodesnifferConfiguration.IGNORE_ARGUMENT_KEY)).thenReturn(new String[] { "" });
    assertEquals("", config.getIgnoreList());

    when(project.getConfiguration().getStringArray(PhpCodesnifferConfiguration.IGNORE_ARGUMENT_KEY)).thenReturn(new String[] {});
    assertEquals(null, config.getIgnoreList());
  }

  /**
   * Should return custom report file with custom path.
   */
  @Test
  public void shouldReturnLevelAndCommandLine() {
    Project project = mock(Project.class);
    Configuration configuration = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
    when(
        configuration.getString(PhpCodesnifferConfiguration.REPORT_FILE_NAME_PROPERTY_KEY,
            PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_NAME)).thenReturn("codesniffer-summary.xml");
    when(
        configuration.getString(PhpCodesnifferConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpCodesnifferConfiguration.DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpCodesnifferConfiguration config = new PhpCodesnifferConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\codesniffer-summary.xml");

    when(
        project.getConfiguration().getString(PhpCodesnifferConfiguration.LEVEL_ARGUMENT_KEY,
            PhpCodesnifferConfiguration.DEFAULT_LEVEL_ARGUMENT)).thenReturn("warning");
    assertEquals("warning", config.getLevel());
    when(
        project.getConfiguration().getString(PhpCodesnifferConfiguration.LEVEL_ARGUMENT_KEY,
            PhpCodesnifferConfiguration.DEFAULT_LEVEL_ARGUMENT)).thenReturn(null);
    assertEquals(null, config.getLevel());

    assertEquals("sqlics", config.getCommandLine());

  }
}
