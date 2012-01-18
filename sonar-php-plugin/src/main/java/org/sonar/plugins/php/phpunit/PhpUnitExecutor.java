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

import static org.sonar.plugins.php.api.Php.PHP;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_BOOTSTRAP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_BOOTSTRAP_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_CONFIGURATION_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_CONFIGURATION_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_FILTER_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_FILTER_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_GROUP_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_GROUP_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_IGNORE_CONFIGURATION_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_IGNORE_CONFIGURATION_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_LOADER_KEY;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_LOADER_OPTION;
import static org.sonar.plugins.php.phpunit.PhpUnitConfiguration.PHPUNIT_MAIN_TEST_FILE_KEY;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.AbstractPhpExecutor;

import com.google.common.collect.Lists;

/**
 * The Class PhpUnitExecutor.
 */
public class PhpUnitExecutor extends AbstractPhpExecutor {

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpUnitExecutor.class);

  private static final String PHPUNIT_COVERAGE_CLOVER_OPTION = "--coverage-clover=";

  private static final String PHPUNIT_LOG_JUNIT_OPTION = "--log-junit=";

  private static final String PHPUNIT_PREFIX = "phpunit";

  private static final String XML_SUFFIX = ".xml";

  /**
   * See https://github.com/sebastianbergmann/phpunit/blob/3.6/PHPUnit/TextUI/TestRunner.php <br/>
   * '1' means there are "test" failures (=> but the process has completed) <br/>
   * '2' means there are "test" errors (=> but the process has completed)
   */
  private static final Collection<Integer> ACCEPTED_EXIT_CODES = Lists.newArrayList(0, 1, 2);

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
    // PHPUnit has 1 specific acceptable exit code ('1'), so we must pass this on the constructor
    super(config, ACCEPTED_EXIT_CODES);
    this.configuration = config;
    this.project = project;
    PHP.setConfiguration(configuration.getProject().getConfiguration());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected List<String> getCommandLine() {
    List<String> result = new ArrayList<String>();
    result.add(configuration.getOsDependentToolScriptName());
    Configuration c = configuration.getProject().getConfiguration();
    addBasicOptions(result);

    boolean useConfigFile = configuration.isStringPropertySet(PHPUNIT_CONFIGURATION_KEY);
    if (useConfigFile) {
      result.add(PHPUNIT_CONFIGURATION_OPTION + configuration.getConfiguration());
    }
    addExtendedOptions(result);

    if (!useConfigFile) {

      boolean ignoreConfigFile = c.containsKey(PHPUNIT_IGNORE_CONFIGURATION_KEY) && c.getBoolean(PHPUNIT_IGNORE_CONFIGURATION_KEY);
      if (ignoreConfigFile) {
        result.add(PHPUNIT_IGNORE_CONFIGURATION_OPTION);
      }

      if (configuration.isStringPropertySet(PHPUNIT_MAIN_TEST_FILE_KEY)) {
        result.add(configuration.getMainTestClass());
      } else if (c.getBoolean(PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY) || ignoreConfigFile) {
        result.add(getTestDirectoryOrFiles());
      }
    }

    return result;
  }

  /**
   * @param result
   * @param c
   */
  private void addExtendedOptions(List<String> result) {
    if (configuration.isStringPropertySet(PHPUNIT_LOADER_KEY)) {
      result.add(PHPUNIT_LOADER_OPTION + configuration.getLoader());
    }
    if (configuration.isStringPropertySet(PHPUNIT_GROUP_KEY)) {
      result.add(PHPUNIT_GROUP_OPTION + configuration.getGroup());
    }
    if (configuration.isStringPropertySet(PHPUNIT_ARGUMENT_LINE_KEY)) {
      result.addAll(Lists.newArrayList(StringUtils.split(configuration.getArgumentLine(), ' ')));
    }
    result.add(PHPUNIT_LOG_JUNIT_OPTION + configuration.getReportFile());
    if (!configuration.shouldSkipCoverage()) {
      result.add(PHPUNIT_COVERAGE_CLOVER_OPTION + configuration.getCoverageReportFile());
    }
  }

  /**
   * @param result
   */
  private void addBasicOptions(List<String> result) {
    if (configuration.isStringPropertySet(PHPUNIT_FILTER_KEY)) {
      result.add(PHPUNIT_FILTER_OPTION + configuration.getFilter());
    }
    if (configuration.isStringPropertySet(PHPUNIT_BOOTSTRAP_KEY)) {
      result.add(PHPUNIT_BOOTSTRAP_OPTION + configuration.getBootstrap());
    }
  }

  /**
   * @param result
   * @return phpunit configuration option followed by the generated phpunit.xml launcher file. Or the test directory if only one.
   */
  private String getTestDirectoryOrFiles() {
    List<File> testDirs = project.getFileSystem().getTestDirs();
    String directoryOrFiles = null;
    if (testDirs.size() == 1) {
      directoryOrFiles = testDirs.get(0).toString();
    } else {
      // in case of multiple source directories, phpunit.xml file is generated and passed to phpunit.
      LOG.warn("Phpunit does not support multiple source directories for the moment.");
      LOG.warn("Group your tests folder under the same directory or use phpunit.xml file");
      List<File> testFiles = InputFileUtils.toFiles(project.getFileSystem().testFiles("php"));
      LOG.info("Generating phpunit.xml file containing all your test files...");
      File phpunitXml = createPhpunitConfigurationFile(testFiles);
      LOG.warn(phpunitXml + " file generated.");
      directoryOrFiles = PHPUNIT_CONFIGURATION_OPTION + phpunitXml.toString();
    }
    return directoryOrFiles;
  }

  /**
   * @param testFiles
   * @return
   */
  private File createPhpunitConfigurationFile(List<File> testFiles) {
    File workingDir = configuration.createWorkingDirectory();
    File ruleset = null;
    try {
      ruleset = File.createTempFile(PHPUNIT_PREFIX, XML_SUFFIX, workingDir);
      StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      xml.append("<phpunit><testsuites><testsuite name=\"Generated\">\n");
      for (File f : testFiles) {
        xml.append("<file>").append(f.getAbsolutePath()).append("</file>\n");
      }
      xml.append("</testsuite></testsuites></phpunit>");
      FileUtils.writeStringToFile(ruleset, xml.toString());
    } catch (IOException e) {
      String msg = "Error while creating  temporary phpunit.xml from files: " + testFiles + " to file : " + ruleset + " in dir "
        + workingDir;
      LOG.error(msg);
    }
    return ruleset.length() > 0 ? ruleset : null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getExecutedTool() {
    return "PhpUnit";
  }

  /**
   * {@inheritDoc}
   */
  public PhpUnitConfiguration getConfiguration() {
    return configuration;
  }

}
