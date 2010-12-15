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

package org.sonar.plugins.php.phpunit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_DEFAULT_MAIN_TEST_FILE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_DEFAULT_REPORT_FILE_NAME;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_DEFAULT_REPORT_FILE_PATH;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_PROPERTY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_NAME_PROPERTY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;

/**
 * The Class PhpDependConfigurationTest.
 */
public class PhpUnitConfigurationTest {

  /**
   * Should get valid suffixe option.
   */
  @Test(expected = PhpUnitConfigurationException.class)
  public void shouldThrowExceptionIfReportFileDoesNotExist() {
    Project project = mock(Project.class);
    Configuration c = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\Sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\target"));
    when(fs.getBasedir()).thenReturn(new File("C:\\projets\\PHP\\Monkey\\"));

    when(c.getString(PHPUNIT_REPORT_FILE_NAME_PROPERTY_KEY, PHPUNIT_DEFAULT_REPORT_FILE_NAME)).thenReturn("phpunit.xml");
    when(c.getString(PHPUNIT_MAIN_TEST_FILE_PROPERTY_KEY, PHPUNIT_DEFAULT_MAIN_TEST_FILE)).thenReturn("/out/of/dir/mainTestClass.php");
    when(c.getString(PHPUNIT_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PHPUNIT_DEFAULT_REPORT_FILE_PATH)).thenReturn("d:\\logs\\");
    when(project.getConfiguration()).thenReturn(c);
    PhpUnitConfiguration config = new PhpUnitConfiguration(project);
    config.getMainTestClass();
  }

  /**
   * Should get valid suffixe option.
   * 
   * @throws IOException
   */
  @Test(expected = PhpUnitConfigurationException.class)
  @Ignore
  public void shouldThrowExceptionIfReportIsNotInTestOrSourceDirs() throws IOException {
    Project project = mock(Project.class);
    Configuration c = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);

    File temp = File.createTempFile("fake", "file");

    File baseDir = temp.getParentFile();

    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\main")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("C:\\projets\\PHP\\Monkey\\sources\\test")));
    when(fs.getBuildDir()).thenReturn(new File(baseDir, "target"));
    when(fs.getBasedir()).thenReturn(baseDir);

    when(c.getString(PHPUNIT_REPORT_FILE_NAME_PROPERTY_KEY, PHPUNIT_DEFAULT_REPORT_FILE_NAME)).thenReturn("phpunit.xml");

    when(c.getString(PHPUNIT_MAIN_TEST_FILE_PROPERTY_KEY, PHPUNIT_DEFAULT_MAIN_TEST_FILE)).thenReturn(temp.getName());
    when(c.getString(PHPUNIT_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PHPUNIT_DEFAULT_REPORT_FILE_PATH)).thenReturn("d:\\logs\\");
    when(project.getConfiguration()).thenReturn(c);
    PhpUnitConfiguration config = new PhpUnitConfiguration(project);
    try {
      config.getMainTestClass();
    } catch (PhpUnitConfigurationException e) {
      assertEquals(true, e.getMessage().contains("not present neither in test directories nor in source"));
      throw e;
    }
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
    when(configuration.getString(PHPUNIT_REPORT_FILE_NAME_PROPERTY_KEY, PHPUNIT_DEFAULT_REPORT_FILE_NAME)).thenReturn(
        PHPUNIT_DEFAULT_REPORT_FILE_NAME);
    when(configuration.getString(PHPUNIT_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PHPUNIT_DEFAULT_REPORT_FILE_PATH)).thenReturn(
        PHPUNIT_DEFAULT_REPORT_FILE_PATH);
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
    when(configuration.getString(PHPUNIT_REPORT_FILE_NAME_PROPERTY_KEY, PHPUNIT_DEFAULT_REPORT_FILE_NAME)).thenReturn(
        PHPUNIT_DEFAULT_REPORT_FILE_NAME);
    when(
        configuration.getString(PhpUnitConfiguration.PHPUNIT_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpUnitConfiguration.PHPUNIT_DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
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
    when(configuration.getString(PHPUNIT_REPORT_FILE_NAME_PROPERTY_KEY, PHPUNIT_DEFAULT_REPORT_FILE_NAME)).thenReturn("punit.summary.xml");
    when(configuration.getString(PHPUNIT_REPORT_FILE_NAME_PROPERTY_KEY, PHPUNIT_DEFAULT_REPORT_FILE_NAME)).thenReturn("punit.summary.xml");
    when(
        configuration.getString(PhpUnitConfiguration.PHPUNIT_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
            PhpUnitConfiguration.PHPUNIT_DEFAULT_REPORT_FILE_PATH)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpUnitConfiguration config = new PhpUnitConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\punit.summary.xml");
  }
}
