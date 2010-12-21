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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_DEFAULT_REPORT_FILE_NAME;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_DEFAULT_REPORT_FILE_PATH;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_DEFAULT_RULESET_ARGUMENT;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_IGNORE_ARGUMENT_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_NAME_PROPERTY_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_RULESETS_ARGUMENT_KEY;

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
public class PhpPmdConfigurationTest {

  @Test
  public void testGetRulesetsWithNotNull() {
    Project project = getMockProject();
    PhpmdConfiguration config = new PhpmdConfiguration(project);
    Configuration c = project.getConfiguration();
    String[] excludeDirs = new String[] { "a", "b" };
    when(c.getStringArray(PHPMD_RULESETS_ARGUMENT_KEY)).thenReturn(excludeDirs);
    assertEquals("a,b", config.getRulesets());
  }

  @Test
  public void testGetRulesetsWithEmpty() {
    Project project = getMockProject();
    PhpmdConfiguration config = new PhpmdConfiguration(project);
    Configuration c = project.getConfiguration();
    String[] excludeDirs = new String[] {};
    when(c.getStringArray(PHPMD_IGNORE_ARGUMENT_KEY)).thenReturn(excludeDirs);
    assertEquals(PHPMD_DEFAULT_RULESET_ARGUMENT, config.getRulesets());
  }

  @Test
  public void testGetRulesetsWithNull() {
    Project project = getMockProject();
    PhpmdConfiguration config = new PhpmdConfiguration(project);
    Configuration c = project.getConfiguration();
    when(c.getStringArray(PHPMD_IGNORE_ARGUMENT_KEY)).thenReturn(null);
    assertEquals(PHPMD_DEFAULT_RULESET_ARGUMENT, config.getRulesets());
  }

  @Test
  public void testGetIgnoreListWithNotNull() {
    Project project = getMockProject();
    PhpmdConfiguration config = new PhpmdConfiguration(project);
    Configuration c = project.getConfiguration();
    String[] excludeDirs = new String[] { "a", "b" };
    when(c.getStringArray(PHPMD_IGNORE_ARGUMENT_KEY)).thenReturn(excludeDirs);
    assertEquals("a,b", config.getIgnoreList());
  }

  @Test
  public void testGetIgnoreListWithEmpty() {
    Project project = getMockProject();
    PhpmdConfiguration config = new PhpmdConfiguration(project);
    Configuration c = project.getConfiguration();
    String[] excludeDirs = new String[] {};
    when(c.getStringArray(PHPMD_IGNORE_ARGUMENT_KEY)).thenReturn(excludeDirs);
    assertEquals(null, config.getIgnoreList());
  }

  @Test
  public void testGetIgnoreListWithNull() {
    Project project = getMockProject();
    PhpmdConfiguration config = new PhpmdConfiguration(project);
    Configuration c = project.getConfiguration();
    when(c.getStringArray(PHPMD_IGNORE_ARGUMENT_KEY)).thenReturn(null);
    assertEquals(null, config.getIgnoreList());
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
    when(configuration.getString(PHPMD_REPORT_FILE_NAME_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_NAME)).thenReturn(
        PHPMD_DEFAULT_REPORT_FILE_NAME);
    when(configuration.getString(PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_PATH)).thenReturn(
        PHPMD_DEFAULT_REPORT_FILE_PATH);
    when(project.getConfiguration()).thenReturn(configuration);
    PhpmdConfiguration config = new PhpmdConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\logs\\pmd.xml");
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
    when(configuration.getString(PHPMD_REPORT_FILE_NAME_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_NAME)).thenReturn(
        PHPMD_DEFAULT_REPORT_FILE_NAME);
    when(configuration.getString(PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpmdConfiguration config = new PhpmdConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\pmd.xml");
  }

  /**
   * Should return custom report file with custom path.
   */
  @Test
  public void shouldReturnCustomReportFileWithCustomPath() {
    Project project = getMockProject();
    PhpmdConfiguration config = new PhpmdConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\pmd-summary.xml");
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
    when(configuration.getString(PHPMD_REPORT_FILE_NAME_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_NAME)).thenReturn("pmd-summary.xml");
    when(configuration.getString(PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PHPMD_DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    return project;
  }
}
