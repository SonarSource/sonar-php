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

import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_BOOTSTRAP_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_BOOTSTRAP_PROPERTY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_CONFIGURATION_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_CONFIGURATION_PROPERTY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_FILTER_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_FILTER_PROPERTY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_GROUP_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_GROUP_PROPERTY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_LOADER_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_LOADER_PROPERTY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_PROPERTY_KEY;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.PhpPluginAbstractExecutor;

/**
 * The Class PhpUnitExecutor.
 */
public class PhpUnitExecutor extends PhpPluginAbstractExecutor {

  private static final String PHPUNIT_COVERAGE_CLOVER_OPTION = "--coverage-clover=";

  private static final String PHPUNIT_LOG_JUNIT_OPTION = "--log-junit=";

  /** The configuration. */
  private final PhpUnitConfiguration configuration;

  /** The project. */
  private final Project project;

  /**
   * Instantiates a new php unit executor.
   * 
   * @param configuration
   *          the configuration
   * @param project
   *          the project
   */
  public PhpUnitExecutor(PhpUnitConfiguration config, Project project) {
    super();
    this.configuration = config;
    this.project = project;
  }

  /**
   * @see org.sonar.plugins.php.core.PhpPluginAbstractExecutor#getCommandLine()
   */
  @Override
  protected List<String> getCommandLine() {
    List<String> result = new ArrayList<String>();
    result.add(configuration.getOsDependentToolScriptName());
    if (configuration.isStringPropertySet(PHPUNIT_FILTER_PROPERTY_KEY)) {
      result.add(PHPUNIT_FILTER_OPTION + configuration.getFilter());
    } else if (configuration.isStringPropertySet(PHPUNIT_BOOTSTRAP_PROPERTY_KEY)) {
      result.add(PHPUNIT_BOOTSTRAP_OPTION + configuration.getBootstrap());
    } else if (configuration.isStringPropertySet(PHPUNIT_CONFIGURATION_PROPERTY_KEY)) {
      result.add(PHPUNIT_CONFIGURATION_OPTION + configuration.getConfiguration());
    } else if (configuration.isStringPropertySet(PHPUNIT_LOADER_PROPERTY_KEY)) {
      result.add(PHPUNIT_LOADER_OPTION + configuration.getLoader());
    } else if (configuration.isStringPropertySet(PHPUNIT_GROUP_PROPERTY_KEY)) {
      result.add(PHPUNIT_GROUP_OPTION + configuration.getGroup());
    } else if (configuration.isStringPropertySet(PHPUNIT_ARGUMENT_LINE_KEY)) {
      result.add(configuration.getArgumentLine());
    }
    result.add(PHPUNIT_LOG_JUNIT_OPTION + configuration.getReportFile());
    if (configuration.shouldRunCoverage()) {
      result.add(PHPUNIT_COVERAGE_CLOVER_OPTION + configuration.getCoverageReportFile());
    }
    if (configuration.isStringPropertySet(PHPUNIT_MAIN_TEST_FILE_PROPERTY_KEY)) {
      result.add(project.getName());
      result.add(configuration.getMainTestClass());
    }
    return result;
  }

  /**
   * @see org.sonar.plugins.php.core.PhpPluginAbstractExecutor#getExecutedTool()
   */
  @Override
  protected String getExecutedTool() {
    return "PhpUnit";
  }

  /**
   * @return the configuration
   */
  public PhpUnitConfiguration getConfiguration() {
    return configuration;
  }

}
