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
package org.sonar.plugins.php.phpdepend;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;

import java.io.File;
import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.*;

/**
 * The Class PhpDependConfigurationTest.
 */
public class PhpDependConfigurationTest {

  private static final String REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = PDEPEND_REPORT_FILE_RELATIVE_PATH_KEY;
  private static final String DEFAULT_REPORT_FILE_NAME = PDEPEND_REPORT_FILE_NAME_DEFVALUE;
  private static final String REPORT_FILE_NAME_PROPERTY_KEY = PDEPEND_REPORT_FILE_NAME_KEY;

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
    String[] excludeDirs = new String[] {"a", "b"};
    when(c.getStringArray(PDEPEND_EXCLUDE_PACKAGE_KEY)).thenReturn(excludeDirs);
    assertEquals("a,b", config.getExcludePackages());
  }

  @Test
  public void testGetExcludePackageWithEmpty() {
    Project project = getMockProject();
    PhpDependConfiguration config = getWindowsConfiguration(project);
    Configuration c = project.getConfiguration();
    String[] excludeDirs = new String[] {};
    when(c.getStringArray(PDEPEND_EXCLUDE_PACKAGE_KEY)).thenReturn(excludeDirs);
    assertEquals(null, config.getExcludePackages());
  }

  /**
   * Should get command line for windows.
   */
  @Test
  public void shouldGetCommandLineForWindows() {
    Project project = getMockProject();
    PhpDependConfiguration config = getWindowsConfiguration(project);

    assertThat(config.getOsDependentToolScriptName(), is(PDEPEND_COMMAND_LINE + ".bat"));
  }

  /**
   * Should get command line for not windows.
   */
  @Test
  public void shouldGetCommandLineForNotWindows() {
    Project project = getMockProject();
    // new Php();
    PhpDependConfiguration config = getNotWindowsConfiguration(project);
    assertThat(config.getOsDependentToolScriptName(), is(PDEPEND_COMMAND_LINE));
  }

  /**
   * S Should get valid suffixe option.
   */
  @Test
  public void shouldGetValidSuffixeOption() {
    Project project = getMockProject();
    PhpDependConfiguration config = new PhpDependConfiguration(project);

    String suffixesOption = config.getSuffixesCommandOption();
    assertThat(suffixesOption).isNotNull();
    assertThat(suffixesOption).contains(",");
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
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn(
        PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE);
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
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpDependConfiguration config = new PhpDependConfiguration(project);
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
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("reports");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpDependConfiguration config = new PhpDependConfiguration(project);
    assertEquals(config.getReportFile().getPath().replace('/', '\\'), "C:\\projets\\PHP\\Monkey\\target\\reports\\pdepend.summary.xml");
  }

  @Test
  public void shouldReturnDefaultReportFileOptionByDefault() {
    Project project = mock(Project.class);
    Configuration configuration = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/tests")));
    when(fs.getBuildDir()).thenReturn(new File("/Volumes/git/sonar/sonar-php-trunk-git/target"));
    when(configuration.getString(REPORT_FILE_NAME_PROPERTY_KEY, DEFAULT_REPORT_FILE_NAME)).thenReturn("pdepend.xml");
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("logs");
    when(configuration.getString(PDEPEND_REPORT_TYPE, PDEPEND_REPORT_TYPE_DEFVALUE)).thenReturn(PDEPEND_REPORT_TYPE_DEFVALUE);
    when(project.getConfiguration()).thenReturn(configuration);
    PhpDependConfiguration config = new PhpDependConfiguration(project);

    String cli = config.getReportFileCommandOption();
    assertEquals(cli, "--phpunit-xml=/Volumes/git/sonar/sonar-php-trunk-git/target/logs/pdepend.xml");
  }

  @Test
  public void shouldReturnPhpUnitReportFileOptionWhenRequested() {
    Project project = mock(Project.class);
    Configuration configuration = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/tests")));
    when(fs.getBuildDir()).thenReturn(new File("/Volumes/git/sonar/sonar-php-trunk-git/target"));
    when(configuration.getString(REPORT_FILE_NAME_PROPERTY_KEY, DEFAULT_REPORT_FILE_NAME)).thenReturn("pdepend.xml");
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("logs");
    when(configuration.getString(PDEPEND_REPORT_TYPE, PDEPEND_REPORT_TYPE_DEFVALUE)).thenReturn(PDEPEND_REPORT_TYPE_PHPUNIT);
    when(project.getConfiguration()).thenReturn(configuration);
    PhpDependConfiguration config = new PhpDependConfiguration(project);

    String cli = config.getReportFileCommandOption();
    assertEquals(cli, "--phpunit-xml=/Volumes/git/sonar/sonar-php-trunk-git/target/logs/pdepend.xml");
  }

  @Test
  public void shouldReturnSummaryReportFileOptionWhenRequested() {
    Project project = mock(Project.class);
    Configuration configuration = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/tests")));
    when(fs.getBuildDir()).thenReturn(new File("/Volumes/git/sonar/sonar-php-trunk-git/target"));
    when(configuration.getString(REPORT_FILE_NAME_PROPERTY_KEY, DEFAULT_REPORT_FILE_NAME)).thenReturn("summary.xml");
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("logs");
    when(configuration.getString(PDEPEND_REPORT_TYPE, PDEPEND_REPORT_TYPE_DEFVALUE)).thenReturn(PDEPEND_REPORT_TYPE_SUMMARY);
    when(project.getConfiguration()).thenReturn(configuration);
    PhpDependConfiguration config = new PhpDependConfiguration(project);

    String cli = config.getReportFileCommandOption();
    assertEquals(cli, "--summary-xml=/Volumes/git/sonar/sonar-php-trunk-git/target/logs/summary.xml");
  }

  @Test(expected = SonarException.class)
  public void shouldThrowExceptionWhenNotSupportedReportFileOptionUsed() {
    Project project = mock(Project.class);
    Configuration configuration = mock(Configuration.class);
    MavenProject mavenProject = mock(MavenProject.class);
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(project.getPom()).thenReturn(mavenProject);
    when(project.getFileSystem()).thenReturn(fs);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/src")));
    when(fs.getTestDirs()).thenReturn(Arrays.asList(new File("/Volumes/git/sonar/sonar-php-trunk-git/math-php-test/source/tests")));
    when(fs.getBuildDir()).thenReturn(new File("/Volumes/git/sonar/sonar-php-trunk-git/target"));
    when(configuration.getString(REPORT_FILE_NAME_PROPERTY_KEY, DEFAULT_REPORT_FILE_NAME)).thenReturn("summary.xml");
    when(configuration.getString(REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE)).thenReturn("logs");
    when(configuration.getString(PDEPEND_REPORT_TYPE, PDEPEND_REPORT_TYPE_DEFVALUE)).thenReturn("not-supported");
    when(project.getConfiguration()).thenReturn(configuration);
    PhpDependConfiguration config = new PhpDependConfiguration(project);

    String cli = config.getReportFileCommandOption();
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

      @SuppressWarnings("unused")
      public String getCommandLinePath() {
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
