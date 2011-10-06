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
package org.sonar.plugins.php.codesniffer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.RuleFinder;

/**
 * The Class PhpDependConfigurationTest.
 */
public class PhpCodesnifferConfigurationTest {

  private static final String CODESNIFFER_SUMMARY_XML = "C:\\projets\\PHP\\Monkey\\target\\reports\\codesniffer-summary.xml";

  /**
   * Should get valid suffixe option.
   */
  @Test
  public void shouldReturnDefaultReportFileWithDefaultPath() {
    Project project = mock(Project.class);
    Configuration config = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
    String reportFileNamePropertyKey = PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_KEY;
    String defaultReportFileName = PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_DEFVALUE;
    when(config.getString(reportFileNamePropertyKey, defaultReportFileName)).thenReturn(defaultReportFileName);

    String defaultReportFilePath = PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_DEFVALUE;
    String reportFileRelativePathPropertyKey = PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_KEY;
    when(config.getString(reportFileRelativePathPropertyKey, defaultReportFilePath)).thenReturn(defaultReportFilePath);
    when(project.getConfiguration()).thenReturn(config);

    RulesProfile profile = mock(RulesProfile.class);
    PhpCodeSnifferProfileExporter exporter = mock(PhpCodeSnifferProfileExporter.class);
    RuleFinder finder = mock(RuleFinder.class);

    PhpCodeSnifferConfiguration phpConfig = new PhpCodeSnifferConfiguration(project, exporter, profile, finder);
    assertEquals(phpConfig.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\logs\\codesniffer.xml");
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
        configuration.getString(PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_KEY,
            PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_DEFVALUE)).thenReturn(
        PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_DEFVALUE);
    when(
        configuration.getString(PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_KEY,
            PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);

    RulesProfile profile = mock(RulesProfile.class);
    PhpCodeSnifferProfileExporter exporter = mock(PhpCodeSnifferProfileExporter.class);
    RuleFinder finder = mock(RuleFinder.class);

    PhpCodeSnifferConfiguration phpConfig = new PhpCodeSnifferConfiguration(project, exporter, profile, finder);
    assertEquals(phpConfig.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\codesniffer.xml");
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
        configuration.getString(PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_KEY,
            PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_DEFVALUE)).thenReturn("codesniffer-summary.xml");
    when(
        configuration.getString(PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_KEY,
            PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);

    RulesProfile profile = mock(RulesProfile.class);
    PhpCodeSnifferProfileExporter exporter = mock(PhpCodeSnifferProfileExporter.class);

    RuleFinder finder = mock(RuleFinder.class);

    PhpCodeSnifferConfiguration phpConfig = new PhpCodeSnifferConfiguration(project, exporter, profile, finder);
    assertEquals(phpConfig.getReportFile().getPath().replace('/', '\\'), CODESNIFFER_SUMMARY_XML);
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
        configuration.getString(PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_KEY,
            PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_DEFVALUE)).thenReturn("codesniffer-summary.xml");
    when(
        configuration.getString(PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_KEY,
            PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);

    RulesProfile profile = mock(RulesProfile.class);
    PhpCodeSnifferProfileExporter exporter = mock(PhpCodeSnifferProfileExporter.class);
    RuleFinder finder = mock(RuleFinder.class);

    PhpCodeSnifferConfiguration phpConfig = new PhpCodeSnifferConfiguration(project, exporter, profile, finder);
    assertEquals(phpConfig.getReportFile().getPath().replace('/', '\\'), CODESNIFFER_SUMMARY_XML);

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
        configuration.getString(PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_KEY,
            PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_NAME_DEFVALUE)).thenReturn("codesniffer-summary.xml");
    when(
        configuration.getString(PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_KEY,
            PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    RulesProfile profile = mock(RulesProfile.class);
    PhpCodeSnifferProfileExporter exporter = mock(PhpCodeSnifferProfileExporter.class);
    RuleFinder finder = mock(RuleFinder.class);

    PhpCodeSnifferConfiguration phpConfig = new PhpCodeSnifferConfiguration(project, exporter, profile, finder);
    assertEquals(phpConfig.getReportFile().getPath().replace('/', '\\'), CODESNIFFER_SUMMARY_XML);

    assertEquals("phpcs", phpConfig.getCommandLine());

  }
}
