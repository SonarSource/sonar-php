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
package org.sonar.plugins.php.phpunit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_BOOTSTRAP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_CONFIGURATION_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_COVERAGE_REPORT_FILE_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_COVERAGE_REPORT_FILE_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_FILTER_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_GROUP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_LOADER_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_NAME_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_NAME_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_SHOULD_RUN_COVERAGE_KEY;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;

/**
 * The Class PhpDependConfigurationTest.
 */
public class PhpUnitConfigurationTest {

  /**
   * Should get valid suffixe option.
   */
  @Test
  public void testConfigurationParameters() {
    Project project = mock(Project.class);
    Configuration c = getMockConfiguration(project);
    when(c.getBoolean(PHPUNIT_SHOULD_RUN_COVERAGE_KEY, true)).thenReturn(true);

    when(project.getConfiguration()).thenReturn(c);
    PhpUnitConfiguration config = new PhpUnitConfiguration(project);
    assertEquals(false, config.shouldSkipCoverage());

    when(c.getString(PHPUNIT_FILTER_KEY, " ")).thenReturn(" ");
    assertEquals(" ", config.getFilter());

    when(c.getString(PHPUNIT_BOOTSTRAP_KEY, " ")).thenReturn(" ");
    assertEquals(" ", config.getBootstrap());

    when(c.getString(PHPUNIT_CONFIGURATION_KEY, " ")).thenReturn(" ");
    assertEquals(" ", config.getConfiguration());

    when(c.getString(PHPUNIT_LOADER_KEY, " ")).thenReturn(" ");
    assertEquals(" ", config.getLoader());

    when(c.getString(PHPUNIT_GROUP_KEY, " ")).thenReturn(" ");
    assertEquals(" ", config.getGroup());

    when(c.getString(PHPUNIT_COVERAGE_REPORT_FILE_KEY, PHPUNIT_COVERAGE_REPORT_FILE_DEFVALUE)).thenReturn(
        PHPUNIT_COVERAGE_REPORT_FILE_DEFVALUE);
    File expectedReportFile = new File(project.getFileSystem().getBuildDir(), config.getReportFileRelativePath() + File.separator
      + PHPUNIT_COVERAGE_REPORT_FILE_DEFVALUE);
    assertEquals(expectedReportFile, config.getCoverageReportFile());

  }

  /**
   * Should get valid suffixe option.
   */
  @Test(expected = SonarException.class)
  public void shouldThrowExceptionIfReportFileDoesNotExist() {
    Project project = mock(Project.class);
    Configuration c = getMockConfiguration(project);
    when(project.getConfiguration()).thenReturn(c);
    PhpUnitConfiguration config = new PhpUnitConfiguration(project);
    config.getMainTestClass();
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
    when(configuration.getString(PHPUNIT_REPORT_FILE_NAME_KEY, PHPUNIT_REPORT_FILE_NAME_DEFVALUE)).thenReturn(
        PHPUNIT_REPORT_FILE_NAME_DEFVALUE);
    when(configuration.getString(PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY, PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn(
        PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE);
    when(project.getConfiguration()).thenReturn(configuration);
    PhpUnitConfiguration config = new PhpUnitConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\logs\\phpunit.xml");
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
    when(configuration.getString(PHPUNIT_REPORT_FILE_NAME_KEY, PHPUNIT_REPORT_FILE_NAME_DEFVALUE)).thenReturn(
        PHPUNIT_REPORT_FILE_NAME_DEFVALUE);
    when(configuration.getString(PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY, PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpUnitConfiguration config = new PhpUnitConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\phpunit.xml");
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
    when(configuration.getString(PHPUNIT_REPORT_FILE_NAME_KEY, PHPUNIT_REPORT_FILE_NAME_DEFVALUE)).thenReturn("punit.summary.xml");
    when(configuration.getString(PHPUNIT_REPORT_FILE_NAME_KEY, PHPUNIT_REPORT_FILE_NAME_DEFVALUE)).thenReturn("punit.summary.xml");
    when(configuration.getString(PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY, PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpUnitConfiguration config = new PhpUnitConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\punit.summary.xml");
  }

  private Configuration getMockConfiguration(Project project) {
    Configuration c = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
    when(fs.getBasedir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\"));

    when(c.getString(PHPUNIT_REPORT_FILE_NAME_KEY, PHPUNIT_REPORT_FILE_NAME_DEFVALUE)).thenReturn("phpunit.xml");
    when(c.getString(PHPUNIT_MAIN_TEST_FILE_KEY, PHPUNIT_MAIN_TEST_FILE_DEFVALUE)).thenReturn("/out/of/dir/mainTestClass.php");
    when(c.getString(PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY, PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("d:\\logs\\");
    return c;
  }
}
