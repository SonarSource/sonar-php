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

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.php.MockUtils;
import org.sonar.plugins.php.api.Php;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_BOOTSTRAP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_CONFIGURATION_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_COVERAGE_SKIP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_FILTER_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_GROUP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_IGNORE_CONFIGURATION_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_LOADER_KEY;

public class PhpUnitExecutorTest {

  private Settings settings;
  private PhpUnitExecutor executor;
  private Project project;

  @Before
  public void init() throws Exception {
    settings = Settings.createForComponent(new PhpUnitSensor(null, null, null, null));
    project = MockUtils.createMockProject();
    PhpUnitConfiguration configuration = new PhpUnitConfiguration(settings, project.getFileSystem());
    executor = new PhpUnitExecutor(new Php(), configuration, project);
  }

  @Test
  public void testGetExecutedTool() throws Exception {
    assertThat(executor.getExecutedTool(), is("PHPUnit"));
  }

  @Test
  public void testSimplestCommandLine() {
    // Given
    settings.setProperty(PHPUNIT_COVERAGE_SKIP_KEY, "true");

    // Verify
    List<String> commandLine = executor.getCommandLine();
    assertThat(commandLine.size()).isEqualTo(2);

    assertThat(commandLine.get(0)).startsWith("phpunit");
    assertThat(commandLine.get(1)).isEqualTo("--log-junit=" + new File("target/MockProject/target/logs/phpunit.xml").getAbsolutePath());
  }

  @Test
  public void testSimpleCommandLineWithMoreArguments() {
    // Given
    settings.setProperty(PHPUNIT_FILTER_KEY, "filters");
    settings.setProperty(PHPUNIT_BOOTSTRAP_KEY, "src/bootstrap.php");
    settings.setProperty(PHPUNIT_CONFIGURATION_KEY, "conf/config.xml");
    settings.setProperty(PHPUNIT_LOADER_KEY, "loaders");
    settings.setProperty(PHPUNIT_GROUP_KEY, "groups");
    settings.setProperty(PHPUNIT_ARGUMENT_LINE_KEY, "--foo=bar --foo2=bar2");

    // Verify
    List<String> commandLine = executor.getCommandLine();
    assertThat(commandLine.size()).isEqualTo(10);

    assertThat(commandLine.get(0)).startsWith("phpunit");
    assertThat(commandLine.get(1)).isEqualTo("--filter=filters");
    assertThat(commandLine.get(2)).isEqualTo("--bootstrap=src/bootstrap.php");
    assertThat(commandLine.get(3)).isEqualTo("--configuration=conf/config.xml");
    assertThat(commandLine.get(4)).isEqualTo("--loader=loaders");
    assertThat(commandLine.get(5)).isEqualTo("--group=groups");
    assertThat(commandLine.get(6)).isEqualTo("--foo=bar");
    assertThat(commandLine.get(7)).isEqualTo("--foo2=bar2");
    assertThat(commandLine.get(8)).isEqualTo("--log-junit=" + new File("target/MockProject/target/logs/phpunit.xml").getAbsolutePath());
    assertThat(commandLine.get(9)).isEqualTo("--coverage-clover=" + new File("target/MockProject/target/logs/phpunit.coverage.xml").getAbsolutePath());
  }

  @Test
  public void testCommandWithoutConfigurationFileAndNoMainClass() throws Exception {
    // Given
    settings.setProperty(PHPUNIT_IGNORE_CONFIGURATION_KEY, "true");

    // Verify
    List<String> commandLine = executor.getCommandLine();
    assertThat(commandLine.size()).isEqualTo(5);

    assertThat(commandLine.get(3)).isEqualTo("--no-configuration");
    assertThat(commandLine.get(4)).isEqualTo(new File("target/MockProject/test").getAbsolutePath());
  }

  @Test
  public void testCommandWithAnalyseTestDirectoriesAndMutipleTestDirs() throws Exception {
    // Given
    settings.setProperty(PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY, "true");
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(fs.getTestDirs()).thenReturn(Arrays.asList(
        new File("target/MockProject/test1").getAbsoluteFile(),
        new File("target/MockProject/test2").getAbsoluteFile()));
    when(fs.getBuildDir()).thenReturn(new File("target/MockProject/target").getAbsoluteFile());
    when(project.getFileSystem()).thenReturn(fs);

    // Verify
    List<String> commandLine = executor.getCommandLine();
    assertThat(commandLine.size()).isEqualTo(4);
    assertThat(commandLine.get(3)).startsWith("--configuration=" + new File("target/MockProject/target/logs/phpunit").getAbsolutePath());

    // clean temp file created
    FileUtils.deleteDirectory(new File("target/MockProject/target/"));
  }

  @Test
  public void testCommandWithMainClass() throws Exception {
    // Given
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_KEY, "AllTests.php");

    FileUtils.forceMkdir(new File("target/MockProject"));
    File mainClass = new File("target/MockProject/AllTests.php");
    mainClass.createNewFile();

    // Verify
    List<String> commandLine = executor.getCommandLine();
    assertThat(commandLine.size()).isEqualTo(4);
    assertThat(commandLine.get(3)).startsWith(new File("target/MockProject/AllTests.php").getAbsolutePath());

    // and clean the created file
    mainClass.delete();
  }
}
