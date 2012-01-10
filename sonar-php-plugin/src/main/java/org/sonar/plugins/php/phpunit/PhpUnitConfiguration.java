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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.AbstractPhpConfiguration;

/**
 * This class handles the php unit configuration.
 * 
 * @version 0.1 @author JTama
 * @version 0.3 @author Akram Ben Aissi
 */
public class PhpUnitConfiguration extends AbstractPhpConfiguration {

  private static final String PHPUNIT_COMMAND_LINE = "phpunit";

  // -- PHPUnit tool options ---
  public static final String PHPUNIT_FILTER_OPTION = "--filter=";
  public static final String PHPUNIT_BOOTSTRAP_OPTION = "--bootstrap=";
  public static final String PHPUNIT_CONFIGURATION_OPTION = "--configuration=";
  public static final String PHPUNIT_IGNORE_CONFIGURATION_OPTION = "--no-configuration";
  public static final String PHPUNIT_LOADER_OPTION = "--loader=";
  public static final String PHPUNIT_GROUP_OPTION = "--group=";

  // --- Sonar config parameters ---
  public static final String PHPUNIT_SKIP_KEY = "sonar.phpUnit.skip";
  public static final String PHPUNIT_SHOULD_RUN_KEY = "sonar.phpUnit.shouldRun"; // OLD param that will be removed soon
  public static final String PHPUNIT_COVERAGE_SKIP_KEY = "sonar.phpUnit.coverage.skip";
  public static final String PHPUNIT_SHOULD_RUN_COVERAGE_KEY = "sonar.phpUnit.coverage.shouldRun"; // OLD param that will be removed soon
  public static final String PHPUNIT_ANALYZE_ONLY_KEY = "sonar.phpUnit.analyzeOnly";
  public static final String PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY = "sonar.phpUnit.reportFileRelativePath";
  public static final String PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE = "/logs";
  public static final String PHPUNIT_REPORT_FILE_NAME_KEY = "sonar.phpUnit.reportFileName";
  public static final String PHPUNIT_REPORT_FILE_NAME_DEFVALUE = "phpunit.xml";
  public static final String PHPUNIT_COVERAGE_REPORT_FILE_KEY = "sonar.phpUnit.coverageReportFile";
  public static final String PHPUNIT_COVERAGE_REPORT_FILE_DEFVALUE = "phpunit.coverage.xml";
  public static final String PHPUNIT_MAIN_TEST_FILE_KEY = "sonar.phpUnit.mainTestClass";
  public static final String PHPUNIT_MAIN_TEST_FILE_DEFVALUE = "AllTests.php";
  public static final String PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY = "sonar.phpUnit.analyze.test.directory";
  public static final String PHPUNIT_ANALYZE_TEST_DIRECTORY_DEFVALUE = "false";
  public static final String PHPUNIT_FILTER_KEY = "sonar.phpUnit.filter";
  public static final String PHPUNIT_BOOTSTRAP_KEY = "sonar.phpUnit.bootstrap";
  public static final String PHPUNIT_CONFIGURATION_KEY = "sonar.phpUnit.configuration";
  public static final String PHPUNIT_IGNORE_CONFIGURATION_KEY = "sonar.phpUnit.ignore.configuration";
  public static final String PHPUNIT_LOADER_KEY = "sonar.phpUnit.loader";
  public static final String PHPUNIT_GROUP_KEY = "sonar.phpUnit.group";
  public static final String PHPUNIT_ARGUMENT_LINE_KEY = "sonar.phpUnit.argumentLine";
  public static final String PHPUNIT_TIMEOUT_KEY = "sonar.phpUnit.timeout";

  private boolean skipCoverage;

  /**
   * Instantiates a new php unit configuration.
   * 
   * @param project
   *          the a project
   */
  public PhpUnitConfiguration(Project project) {
    super(project);
    // Enable dynamic analysis if analyze only is set or dynamic analysis is set to false
    Configuration configuration = project.getConfiguration();
    analyzeOnly = configuration.getBoolean(getShouldAnalyzeOnlyKey(), false);

    if (isStringPropertySet(PHPUNIT_COVERAGE_SKIP_KEY)) {
      skipCoverage = project.getConfiguration().getBoolean(PHPUNIT_COVERAGE_SKIP_KEY);
    } else if (isStringPropertySet(PHPUNIT_SHOULD_RUN_COVERAGE_KEY)) {
      skipCoverage = !project.getConfiguration().getBoolean(PHPUNIT_SHOULD_RUN_COVERAGE_KEY);
    }
  }

  /**
   * Gets the main test class.
   * 
   * @return the main test class
   */
  public String getMainTestClass() {
    String reportFileName = getProject().getConfiguration().getString(PHPUNIT_MAIN_TEST_FILE_KEY, PHPUNIT_MAIN_TEST_FILE_DEFVALUE);
    List<File> directories = new ArrayList<File>(getTestDirectories());
    directories.addAll(getSourceDirectories());

    // find the first occurrence of the test file name class in the test directories.
    // if no file with that name is found in test directories, check in the sources
    for (File directory : directories) {
      if (directory.isDirectory()) {
        File file = new File(directory.getAbsolutePath(), reportFileName);
        if (file.exists()) {
          return file.getAbsolutePath();
        }
      }
    }
    // Otherwise return the file in the base directory
    File file = new File(getProject().getFileSystem().getBasedir(), reportFileName);
    if (!file.exists()) {
      StringBuilder message = new StringBuilder("The specificied main class file cannot be found: ");
      message.append(reportFileName).append(". If you don't have a main test file, consider using a phpunit.xml file and do not ");
      message.append("use ").append(PHPUNIT_MAIN_TEST_FILE_KEY).append(" property.");
      throw new SonarException(message.toString());
    }
    return file.getAbsolutePath();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getArgumentLineKey() {
    return PHPUNIT_ARGUMENT_LINE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getCommandLine() {
    return PHPUNIT_COMMAND_LINE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDefaultArgumentLine() {
    return "";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDefaultReportFileName() {
    return PHPUNIT_REPORT_FILE_NAME_DEFVALUE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDefaultReportFilePath() {
    return PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getReportFileNameKey() {
    return PHPUNIT_REPORT_FILE_NAME_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getReportFileRelativePathKey() {
    return PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected final String getShouldAnalyzeOnlyKey() {
    return PHPUNIT_ANALYZE_ONLY_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getShouldRunKey() {
    return PHPUNIT_SHOULD_RUN_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getSkipKey() {
    return PHPUNIT_SKIP_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getTimeoutKey() {
    return PHPUNIT_TIMEOUT_KEY;
  }

  /**
   * Should run coverage.
   * 
   * @return true, if successful
   */
  public boolean shouldSkipCoverage() {
    return skipCoverage;
  }

  /**
   * Gets the coverage report file.
   * 
   * @return the coverage report file
   */
  public File getCoverageReportFile() {
    Configuration configuration = getProject().getConfiguration();
    return new File(getProject().getFileSystem().getBuildDir(), new StringBuilder().append(getReportFileRelativePath())
        .append(File.separator).append(configuration.getString(PHPUNIT_COVERAGE_REPORT_FILE_KEY, PHPUNIT_COVERAGE_REPORT_FILE_DEFVALUE))
        .toString());
  }

  /**
   * Gets the user defined filter.
   * 
   * @return the user defined filter.
   */
  public String getFilter() {
    return getProject().getConfiguration().getString(PHPUNIT_FILTER_KEY, " ");
  }

  /**
   * Gets the user defined boot strap.
   * 
   * @return the user defined filter.
   */
  public String getBootstrap() {
    return getProject().getConfiguration().getString(PHPUNIT_BOOTSTRAP_KEY, " ");
  }

  /**
   * Gets the user defined configuration file.
   * 
   * @return the user defined configuration file.
   */
  public String getConfiguration() {
    return getProject().getConfiguration().getString(PHPUNIT_CONFIGURATION_KEY, " ");
  }

  /**
   * Gets the user defined loader.
   * 
   * @return the user defined loader.
   */
  public String getLoader() {
    return getProject().getConfiguration().getString(PHPUNIT_LOADER_KEY, " ");
  }

  /**
   * Gets the user defined group.
   * 
   * @return the user defined group.
   */
  public String getGroup() {
    return getProject().getConfiguration().getString(PHPUNIT_GROUP_KEY, " ");
  }

}
