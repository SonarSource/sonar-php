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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;
import org.sonar.plugins.php.MockUtils;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.sonar.plugins.php.core.AbstractPhpConfiguration.DEFAULT_TIMEOUT;

/**
 * The Class PhpDependConfigurationTest.
 */
public class PhpUnitConfigurationTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Settings settings;
  private PhpUnitConfiguration phpConfig;

  @Before
  public void init() throws Exception {
    settings = Settings.createForComponent(new PhpUnitSensor(null, null, null, null));
    phpConfig = new PhpUnitConfiguration(settings, MockUtils.createMockProject().getFileSystem());
  }

  @Test
  public void shouldReturnDefaultValues() {
    assertThat(phpConfig.getCommandLine()).isEqualTo("phpunit");
    assertThat(phpConfig.isSkip()).isFalse();
    assertThat(phpConfig.shouldSkipCoverage()).isFalse();
    assertThat(phpConfig.isAnalyseOnly()).isFalse();
    File report = new File("target/MockProject/target/logs/phpunit.xml");
    assertThat(phpConfig.getReportFile().getAbsolutePath()).isEqualTo(report.getAbsolutePath());
    File coverageReport = new File("target/MockProject/target/logs/phpunit.coverage.xml");
    assertThat(phpConfig.getCoverageReportFile().getAbsolutePath()).isEqualTo(coverageReport.getAbsolutePath());
    assertThat(phpConfig.getMainTestClass()).isNull();
    assertThat(phpConfig.isAnalyseTestDirectory()).isFalse();
    assertThat(phpConfig.getFilter()).isNull();
    assertThat(phpConfig.getBootstrap()).isNull();
    assertThat(phpConfig.getConfiguration()).isNull();
    assertThat(phpConfig.isIgnoreDefaultConfiguration()).isFalse();
    assertThat(phpConfig.getLoader()).isNull();
    assertThat(phpConfig.getGroup()).isNull();
    assertThat(phpConfig.getArgumentLine()).isNull();
    assertThat(phpConfig.getTimeout()).isEqualTo(DEFAULT_TIMEOUT);
  }

  @Test
  public void shouldReturnCustomProperties() {
    // Given
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_SKIP_KEY, "true");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_COVERAGE_SKIP_KEY, "true");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_ANALYZE_ONLY_KEY, "true");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_REPORT_FILE_NAME_KEY, "my-phpunit.xml");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_COVERAGE_REPORT_FILE_KEY, "my-coverage.xml");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY, "reports");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_KEY, "AllTests.php");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY, "true");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_FILTER_KEY, "foo");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_BOOTSTRAP_KEY, "bootstrap.php");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_CONFIGURATION_KEY, "config.xml");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_IGNORE_CONFIGURATION_KEY, "true");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_LOADER_KEY, "loader");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_GROUP_KEY, "groups");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_ARGUMENT_LINE_KEY, "--ignore=**/tests/**,**/jpgraph/**,**/Zend/**");
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_TIMEOUT_KEY, "120");

    // Verify
    assertThat(phpConfig.getCommandLine()).isEqualTo("phpunit");
    assertThat(phpConfig.isSkip()).isTrue();
    assertThat(phpConfig.shouldSkipCoverage()).isTrue();
    assertThat(phpConfig.isAnalyseOnly()).isTrue();
    File report = new File("target/MockProject/target/reports/my-phpunit.xml");
    assertThat(phpConfig.getReportFile().getAbsolutePath()).isEqualTo(report.getAbsolutePath());
    File coverageReport = new File("target/MockProject/target/reports/my-coverage.xml");
    assertThat(phpConfig.getCoverageReportFile().getAbsolutePath()).isEqualTo(coverageReport.getAbsolutePath());
    assertThat(phpConfig.getMainTestClass()).isEqualTo("AllTests.php");
    assertThat(phpConfig.isAnalyseTestDirectory()).isTrue();
    assertThat(phpConfig.getFilter()).isEqualTo("foo");
    assertThat(phpConfig.getBootstrap()).isEqualTo("bootstrap.php");
    assertThat(phpConfig.getConfiguration()).isEqualTo("config.xml");
    assertThat(phpConfig.isIgnoreDefaultConfiguration()).isTrue();
    assertThat(phpConfig.getLoader()).isEqualTo("loader");
    assertThat(phpConfig.getGroup()).isEqualTo("groups");
    assertThat(phpConfig.getArgumentLine()).isEqualTo("--ignore=**/tests/**,**/jpgraph/**,**/Zend/**");
    assertThat(phpConfig.getTimeout()).isEqualTo(120);
  }

  @Test
  public void shouldFindMainClassInSources() throws Exception {
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_KEY, "AllTests.php");

    FileUtils.forceMkdir(new File("target/MockProject/test"));
    File mainClass = new File("target/MockProject/test/AllTests.php");
    mainClass.createNewFile();

    assertThat(phpConfig.getMainTestClassFilePath()).isEqualTo(mainClass.getAbsolutePath());
    mainClass.delete();
  }

  @Test
  public void shouldFindMainClassInBaseDir() throws Exception {
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_KEY, "AllTests.php");

    FileUtils.forceMkdir(new File("target/MockProject"));
    File mainClass = new File("target/MockProject/AllTests.php");
    mainClass.createNewFile();

    assertThat(phpConfig.getMainTestClassFilePath()).isEqualTo(mainClass.getAbsolutePath());
    mainClass.delete();
  }

  @Test
  public void shouldFailIfMainClassNotFound() throws Exception {
    settings.setProperty(PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_KEY, "AllTests.php");

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("The specified main class file cannot be found: AllTests.php. " +
      "If you don't have a main test file, consider using a phpunit.xml file and do not use sonar.phpUnit.mainTestClass property.");

    phpConfig.getMainTestClassFilePath();
  }

}
