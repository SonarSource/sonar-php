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

import com.thoughtworks.xstream.XStreamException;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.api.PhpConstants;

import static org.sonar.plugins.php.core.AbstractPhpConfiguration.DEFAULT_TIMEOUT;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ANALYZE_ONLY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ANALYZE_TEST_DIRECTORY_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_BOOTSTRAP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_CONFIGURATION_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_CONFIGURATION_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_COVERAGE_REPORT_FILE_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_COVERAGE_REPORT_FILE_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_COVERAGE_REPORT_PATH_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_COVERAGE_SKIP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_FILTER_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_GROUP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_IGNORE_CONFIGURATION_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_LOADER_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_NAME_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_NAME_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_REPORT_PATH_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_SKIP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_TIMEOUT_KEY;

/**
 * The Class PhpUnitSensor is used by the plugin to collect metrics concerning PHPUnit framework.
 */
@Properties({
  @Property(key = PHPUNIT_SKIP_KEY, defaultValue = "false", name = "Disable PHPUnit", project = true, global = true,
    description = "If set to true, PHPUnit will not run.",
    category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT,
    type = PropertyType.BOOLEAN),
  @Property(key = PHPUNIT_COVERAGE_SKIP_KEY, defaultValue = "false", name = "Disable PHPUnit coverage", project = true, global = true,
    description = "If true, code coverage measures will not be computed.", category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT,
    type = PropertyType.BOOLEAN),
  @Property(key = PHPUNIT_ANALYZE_ONLY_KEY, defaultValue = "false", name = "Only analyze existing PHPUnit report files", project = true,
    global = true, description = "If set to false, PHPUnit will be executed. If set to true, PHPUnit will not be executed and the report provided through the \""
      + PHPUNIT_REPORT_PATH_KEY + "\" property will be used.",
    category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT,
    type = PropertyType.BOOLEAN),
  @Property(key = PHPUNIT_REPORT_PATH_KEY,
    name = "Report file path", project = true, global = true, description = "Relative path to the report file to analyze. Example: path/to/phpunit.xml.",
    category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY, defaultValue = PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE,
    name = "Report relative file path (Deprecated)", project = true, global = true, description = "Replaced by the \"" + PHPUNIT_REPORT_PATH_KEY + "\" and \""
      + PHPUNIT_COVERAGE_REPORT_PATH_KEY
      + "\" properties.",
    category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_REPORT_FILE_NAME_KEY, defaultValue = PHPUNIT_REPORT_FILE_NAME_DEFVALUE, name = "Report file name (Deprecated)",
    project = true, global = true, description = "Replaced by the \"" + PHPUNIT_REPORT_PATH_KEY + "\" property.",
    category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_COVERAGE_REPORT_PATH_KEY,
    name = "Coverage report file path", project = true, global = true, description = "Path of the coverage report file to analyse.",
    category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_COVERAGE_REPORT_FILE_KEY, defaultValue = PHPUNIT_COVERAGE_REPORT_FILE_DEFVALUE,
    name = "Coverage report file name (Deprecated)", project = true, global = true, description = "Replaced by the \"" + PHPUNIT_COVERAGE_REPORT_PATH_KEY + "\" property.",
    category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_MAIN_TEST_FILE_KEY, defaultValue = "",
    name = "File containing the main method calling all the tests (Deprecated)", project = true, global = true,
    description = "Replaced by the \"" + PHPUNIT_CONFIGURATION_KEY + "\" property.", category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY, defaultValue = PHPUNIT_ANALYZE_TEST_DIRECTORY_DEFVALUE,
    name = "Should analyse the whole test directory (Deprecated)", project = true, global = true,
    description = "Replaced by the \"" + PHPUNIT_CONFIGURATION_KEY + "\" property.", category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT,
    type = PropertyType.BOOLEAN),
  @Property(key = PHPUNIT_FILTER_KEY, defaultValue = "", name = "Test filter", project = true, global = true,
    description = "Filter which tests to run.", category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_BOOTSTRAP_KEY, defaultValue = "", name = "Bootstrap file", project = true, global = true,
    description = "A \"bootstrap\" PHP file that is run before the tests.", category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_CONFIGURATION_KEY, defaultValue = PHPUNIT_CONFIGURATION_DEFVALUE, name = "Configuration file", project = true, global = true,
    description = "Read configuration from XML file.", category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT),
  @Property(
    key = PHPUNIT_IGNORE_CONFIGURATION_KEY,
    defaultValue = "false",
    name = "Ignore default configuration (Deprecated)",
    project = true,
    global = true,
    description = "Replaced by the \"" + PHPUNIT_CONFIGURATION_KEY + "\" property.",
    category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT,
    type = PropertyType.BOOLEAN),
  @Property(key = PHPUNIT_LOADER_KEY, defaultValue = "", name = "PHPUnit loader", project = true, global = true,
    description = "Specifies which TestSuiteLoader implementation to use.", category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_GROUP_KEY, defaultValue = "", name = "Groups to run", project = true, global = true,
    description = "Only runs tests from the specified group(s).", category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_ARGUMENT_LINE_KEY, defaultValue = "", name = "Additional arguments", project = true, global = true,
    description = "Additionnal parameters that can be passed to PHPUnit.", category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT),
  @Property(key = PHPUNIT_TIMEOUT_KEY, defaultValue = "" + DEFAULT_TIMEOUT, name = "Timeout", project = true, global = true,
    description = "Execution of PHPUnit will be stopped after this amount of time (in minutes).", category = PhpUnitSensor.CATEGORY_PHP_PHP_UNIT)
})
public class PhpUnitSensor implements Sensor {

  protected static final String CATEGORY_PHP_PHP_UNIT = "PHPUnit";

  private PhpUnitConfiguration configuration;
  private PhpUnitExecutor executor;
  private PhpUnitResultParser parser;
  private PhpUnitCoverageResultParser coverageParser;

  /**
   * @param executor
   * @param parser
   */
  public PhpUnitSensor(PhpUnitConfiguration conf, PhpUnitExecutor executor, PhpUnitResultParser parser,
      PhpUnitCoverageResultParser coverageParser) {
    super();
    this.configuration = conf;
    this.executor = executor;
    this.parser = parser;
    this.coverageParser = coverageParser;
  }

  /**
   * {@inheritDoc}
   */
  public void analyse(Project project, SensorContext context) {
    try {
      configuration.createWorkingDirectory();

      if (!configuration.isAnalyseOnly()) {
        executor.execute();
      }
      parser.parse(configuration.getReportFile());
      if (!configuration.shouldSkipCoverage()) {
        coverageParser.parse(configuration.getCoverageReportFile(), executor.isEmbeddedMode());
      }
    } catch (XStreamException e) {
      throw new SonarException("Report file is invalid, plugin will stop.", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean shouldExecuteOnProject(Project project) {
    return PhpConstants.LANGUAGE_KEY.equals(project.getLanguageKey())
      && configuration.isDynamicAnalysisEnabled()
      && !configuration.isSkip()
      && !project.getFileSystem().testFiles(PhpConstants.LANGUAGE_KEY).isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "PHPUnit Sensor";
  }
}
