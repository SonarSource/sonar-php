/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi
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

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_CONFIGURATION_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_IGNORE_CONFIGURATION_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_IGNORE_CONFIGURATION_PROPERTY_KEY;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;

public class PhpUnitExecutorTest {

  @Test
  public void shouldReturnCommandLineWithoutCoverageOptions() {
    PhpUnitConfiguration config = mock(PhpUnitConfiguration.class);
    Project project = mock(Project.class);
    MavenProject mProject = mock(MavenProject.class);
    when(project.getPom()).thenReturn(mProject);
    when(mProject.getBasedir()).thenReturn(new File("toto"));

    Configuration configuration = mock(Configuration.class);
    when(project.getConfiguration()).thenReturn(configuration);
    when(config.getProject()).thenReturn(project);

    ProjectFileSystem pfs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(pfs);
    File testDir = new File("c:/php/math-php-test/sources/test");
    when(project.getFileSystem().getTestDirs()).thenReturn(Arrays.asList(testDir));

    PhpUnitExecutor executor = new PhpUnitExecutor(config, project);
    when(config.shouldRunCoverage()).thenReturn(false);
    when(config.getCoverageReportFile()).thenReturn(new File("phpUnit.coverage.xml"));
    List<String> commandLine = executor.getCommandLine();
    assertTrue("Should not return any coverage options", !commandLine.contains("--coverage-clover=phpUnit.coverage.xml"));
  }

  @Test
  public void testIgnoreDefaultConfigurationFile() {
    PhpUnitConfiguration config = mock(PhpUnitConfiguration.class);
    Project project = mock(Project.class);
    MavenProject mProject = mock(MavenProject.class);
    when(project.getPom()).thenReturn(mProject);
    when(mProject.getBasedir()).thenReturn(new File("toto"));

    Configuration configuration = mock(Configuration.class);
    when(project.getConfiguration()).thenReturn(configuration);
    when(config.getProject()).thenReturn(project);

    when(configuration.containsKey(PHPUNIT_IGNORE_CONFIGURATION_PROPERTY_KEY)).thenReturn(true);
    when(configuration.getBoolean(PHPUNIT_IGNORE_CONFIGURATION_PROPERTY_KEY)).thenReturn(true);

    String phpunit = "phpunit";
    when(config.getOsDependentToolScriptName()).thenReturn(phpunit);
    ProjectFileSystem pfs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(pfs);
    File testDir = new File("c:/php/math-php-test/sources/test");
    when(project.getFileSystem().getTestDirs()).thenReturn(Arrays.asList(testDir));

    PhpUnitExecutor executor = new PhpUnitExecutor(config, project);
    when(config.shouldRunCoverage()).thenReturn(false);
    when(config.getCoverageReportFile()).thenReturn(new File("phpUnit.coverage.xml"));
    File reportFile = new File("phpunit.xml");
    when(config.getReportFile()).thenReturn(reportFile);

    List<String> commandLine = executor.getCommandLine();
    assertThat(commandLine).isNotEmpty();
    assertThat(commandLine).contains(PHPUNIT_IGNORE_CONFIGURATION_OPTION);

    List<String> expected = Arrays.asList(phpunit, "--no-configuration", "--log-junit=" + reportFile, testDir.toString());
    assertThat(commandLine).isEqualTo(expected);
  }

  @Test
  public void testNoIgnoreDefaultConfigurationFile() {
    PhpUnitConfiguration config = mock(PhpUnitConfiguration.class);
    Project project = mock(Project.class);
    MavenProject mProject = mock(MavenProject.class);
    when(project.getPom()).thenReturn(mProject);
    when(mProject.getBasedir()).thenReturn(new File("toto"));

    Configuration configuration = mock(Configuration.class);
    when(project.getConfiguration()).thenReturn(configuration);
    when(config.getProject()).thenReturn(project);

    when(configuration.containsKey(PHPUNIT_IGNORE_CONFIGURATION_PROPERTY_KEY)).thenReturn(false);
    when(configuration.getBoolean(PHPUNIT_IGNORE_CONFIGURATION_PROPERTY_KEY)).thenReturn(true);

    ProjectFileSystem pfs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(pfs);
    File testDir = new File("c:/php/math-php-test/sources/test");
    when(project.getFileSystem().getTestDirs()).thenReturn(Arrays.asList(testDir));

    PhpUnitExecutor executor = new PhpUnitExecutor(config, project);
    when(config.shouldRunCoverage()).thenReturn(false);
    when(config.getCoverageReportFile()).thenReturn(new File("phpUnit.coverage.xml"));
    List<String> commandLine = executor.getCommandLine();
    assertThat(commandLine).isNotEmpty();
    assertThat(commandLine).excludes(PHPUNIT_IGNORE_CONFIGURATION_OPTION);
  }

  @Test
  public void testAnalyseTestDirectory() {
    PhpUnitConfiguration config = mock(PhpUnitConfiguration.class);
    Project project = mock(Project.class);
    MavenProject mProject = mock(MavenProject.class);
    when(project.getPom()).thenReturn(mProject);
    when(mProject.getBasedir()).thenReturn(new File("toto"));

    ProjectFileSystem pfs = mock(ProjectFileSystem.class);
    when(project.getFileSystem()).thenReturn(pfs);

    Configuration configuration = mock(Configuration.class);
    when(project.getConfiguration()).thenReturn(configuration);
    when(config.getProject()).thenReturn(project);

    when(configuration.containsKey(PHPUNIT_IGNORE_CONFIGURATION_PROPERTY_KEY)).thenReturn(false);
    when(configuration.getBoolean(PHPUNIT_IGNORE_CONFIGURATION_PROPERTY_KEY)).thenReturn(true);

    File testDir = new File("c:/php/math-php-test/sources/test");
    when(project.getFileSystem().getTestDirs()).thenReturn(Arrays.asList(testDir));

    when(configuration.containsKey(PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY)).thenReturn(true);
    when(configuration.getBoolean(PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY)).thenReturn(true);

    PhpUnitExecutor executor = new PhpUnitExecutor(config, project);
    when(config.shouldRunCoverage()).thenReturn(false);
    when(config.getCoverageReportFile()).thenReturn(new File("phpUnit.coverage.xml"));
    List<String> commandLine = executor.getCommandLine();
    assertThat(commandLine).isNotEmpty();
    assertThat(commandLine).excludes(PHPUNIT_IGNORE_CONFIGURATION_OPTION);
    assertThat(commandLine).contains(testDir.toString());
  }

  @Test
  public void testDoNotAnalyseTestDirectory() {
    PhpUnitConfiguration config = mock(PhpUnitConfiguration.class);
    Project project = mock(Project.class);
    MavenProject mProject = mock(MavenProject.class);
    when(project.getPom()).thenReturn(mProject);
    when(mProject.getBasedir()).thenReturn(new File("toto"));
    ProjectFileSystem pfs = mock(ProjectFileSystem.class);

    when(project.getFileSystem()).thenReturn(pfs);
    Configuration configuration = mock(Configuration.class);
    when(project.getConfiguration()).thenReturn(configuration);
    when(config.getProject()).thenReturn(project);

    when(configuration.containsKey(PHPUNIT_IGNORE_CONFIGURATION_PROPERTY_KEY)).thenReturn(false);
    when(configuration.getBoolean(PHPUNIT_IGNORE_CONFIGURATION_PROPERTY_KEY)).thenReturn(true);

    when(configuration.containsKey(PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY)).thenReturn(true);
    when(configuration.getBoolean(PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY)).thenReturn(false);

    File testDir = new File("c:/php/math-php-test/sources/test");
    when(project.getFileSystem().getTestDirs()).thenReturn(Arrays.asList(testDir));

    PhpUnitExecutor executor = new PhpUnitExecutor(config, project);
    when(config.shouldRunCoverage()).thenReturn(false);
    when(config.getCoverageReportFile()).thenReturn(new File("phpUnit.coverage.xml"));
    List<String> commandLine = executor.getCommandLine();
    assertThat(commandLine).isNotEmpty();
    assertThat(commandLine).excludes(PHPUNIT_IGNORE_CONFIGURATION_OPTION);
    assertThat(commandLine).excludes(testDir.toString());
  }

  @Test
  public void testAnalyseMultipleTestDirectories() {
    PhpUnitConfiguration config = mock(PhpUnitConfiguration.class);
    Project project = mock(Project.class);
    MavenProject mProject = mock(MavenProject.class);
    when(project.getPom()).thenReturn(mProject);
    when(mProject.getBasedir()).thenReturn(new File("toto"));
    ProjectFileSystem pfs = mock(ProjectFileSystem.class);

    when(project.getFileSystem()).thenReturn(pfs);
    Configuration configuration = mock(Configuration.class);
    when(project.getConfiguration()).thenReturn(configuration);
    when(config.getProject()).thenReturn(project);

    when(configuration.containsKey(PHPUNIT_IGNORE_CONFIGURATION_PROPERTY_KEY)).thenReturn(false);
    when(configuration.getBoolean(PHPUNIT_IGNORE_CONFIGURATION_PROPERTY_KEY)).thenReturn(true);

    File testDir = new File("c:/php/math-php-test/sources/test");
    File testDir2 = new File("c:/php/math-php-test/sources/test2");
    when(project.getFileSystem().getTestDirs()).thenReturn(Arrays.asList(testDir, testDir2));

    PhpUnitExecutor executor = new PhpUnitExecutor(config, project);
    when(config.shouldRunCoverage()).thenReturn(false);
    when(config.getCoverageReportFile()).thenReturn(new File("phpUnit.coverage.xml"));
    List<String> commandLine = executor.getCommandLine();
    assertThat(commandLine).isNotEmpty();

    boolean found = false;
    for (String command : commandLine) {
      if (command != null && command.startsWith(PHPUNIT_CONFIGURATION_OPTION)) {
        found = true;
        break;
      }
    }
    assertThat(found).isTrue();
    assertThat(commandLine).excludes(PHPUNIT_IGNORE_CONFIGURATION_OPTION);
    assertThat(commandLine).excludes(testDir.toString());
  }

}