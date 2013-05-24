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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.php.core.AbstractPhpConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the php unit configuration.
 */
public class PhpUnitConfiguration extends AbstractPhpConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(PhpUnitConfiguration.class);

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

  /**
   * @since 1.2
   */
  public static final String PHPUNIT_REPORT_PATH_KEY = "sonar.phpUnit.reportPath";
  /**
   * @since 1.2
   */
  public static final String PHPUNIT_COVERAGE_REPORT_PATH_KEY = "sonar.phpUnit.coverage.reportPath";
  /**
   * @deprecated since 1.2
   */
  public static final String PHPUNIT_REPORT_FILE_RELATIVE_PATH_KEY = "sonar.phpUnit.reportFileRelativePath";
  /**
   * @deprecated since 1.2
   */
  public static final String PHPUNIT_REPORT_FILE_RELATIVE_PATH_DEFVALUE = "/logs";
  /**
   * @deprecated since 1.2
   */
  public static final String PHPUNIT_REPORT_FILE_NAME_KEY = "sonar.phpUnit.reportFileName";
  /**
   * @deprecated since 1.2
   */
  public static final String PHPUNIT_REPORT_FILE_NAME_DEFVALUE = "phpunit.xml";
  /**
   * @deprecated since 1.2
   */
  public static final String PHPUNIT_COVERAGE_REPORT_FILE_KEY = "sonar.phpUnit.coverageReportFile";
  /**
   * @deprecated since 1.2
   */
  public static final String PHPUNIT_COVERAGE_REPORT_FILE_DEFVALUE = "phpunit.coverage.xml";
  /**
   * @deprecated since 1.2
   */
  public static final String PHPUNIT_MAIN_TEST_FILE_KEY = "sonar.phpUnit.mainTestClass";
  /**
   * @deprecated since 1.2
   */
  public static final String PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY = "sonar.phpUnit.analyze.test.directory";
  /**
   * @deprecated since 1.2
   */
  public static final String PHPUNIT_ANALYZE_TEST_DIRECTORY_DEFVALUE = "false";
  public static final String PHPUNIT_FILTER_KEY = "sonar.phpUnit.filter";
  public static final String PHPUNIT_BOOTSTRAP_KEY = "sonar.phpUnit.bootstrap";
  public static final String PHPUNIT_CONFIGURATION_KEY = "sonar.phpUnit.configuration";
  public static final String PHPUNIT_IGNORE_CONFIGURATION_KEY = "sonar.phpUnit.ignore.configuration";
  public static final String PHPUNIT_LOADER_KEY = "sonar.phpUnit.loader";
  public static final String PHPUNIT_GROUP_KEY = "sonar.phpUnit.group";
  public static final String PHPUNIT_ARGUMENT_LINE_KEY = "sonar.phpUnit.argumentLine";
  public static final String PHPUNIT_TIMEOUT_KEY = "sonar.phpUnit.timeout";

  private File coverageReportFile;

  /**
   * Instantiates a new php unit configuration.
   * 
   * @param project
   *          the a project
   */
  public PhpUnitConfiguration(Settings settings, ProjectFileSystem fileSystem) {
    super(settings, fileSystem);
  }

  /**
   * Should run coverage.
   * 
   * @return true, if successful
   */
  public boolean shouldSkipCoverage() {
    boolean skip = false;
    if (getSettings().hasKey(PHPUNIT_COVERAGE_SKIP_KEY)) {
      skip = getSettings().getBoolean(PHPUNIT_COVERAGE_SKIP_KEY);
    } else if (getSettings().hasKey(PHPUNIT_SHOULD_RUN_COVERAGE_KEY)) {
      skip = !getSettings().getBoolean(PHPUNIT_SHOULD_RUN_COVERAGE_KEY);
    }
    return skip;
  }

  /**
   * Gets the coverage report file name.
   * 
   * @return the report file name
   * @deprecated since 1.2
   */
  @Deprecated
  public String getCoverageReportFileName() {
    return getSettings().getString(PHPUNIT_COVERAGE_REPORT_FILE_KEY);
  }

  /**
   * Gets the coverage report path.
   * 
   * @return the report file path
   */
  public String getCoverageReportFilePath() {
    return getSettings().getString(PHPUNIT_COVERAGE_REPORT_PATH_KEY);
  }

  public File getDefaultCoverageReportFile() {
    return new File(getFileSystem().getSonarWorkingDirectory(), "phpunit.coverage.xml");
  }

  /**
   * Gets the coverage report file.
   * 
   * @return the coverage report file
   */
  public File getCoverageReportFile() {
    if (coverageReportFile == null) {
      String coverageReportPath = getCoverageReportFilePath();
      if (StringUtils.isBlank(coverageReportPath)) {
        // Test if deprecated properties are used
        if (getSettings().hasKey(PHPUNIT_COVERAGE_REPORT_FILE_KEY)) {
          LOG.warn("/!\\ " + PHPUNIT_COVERAGE_REPORT_FILE_KEY + " is deprecated. Please update project settings and use " + PHPUNIT_COVERAGE_REPORT_PATH_KEY);
          StringBuilder fileName = new StringBuilder(getReportFileRelativePath()).append(File.separator);
          fileName.append(getCoverageReportFileName());
          coverageReportFile = new File(getFileSystem().getBuildDir(), fileName.toString());
        }
        else {
          coverageReportFile = getDefaultCoverageReportFile();
        }
      }
      else {
        coverageReportFile = getFileSystem().resolvePath(coverageReportPath);
      }
      LOG.info("Report file for phpunit coverage: " + coverageReportFile);
    }
    return coverageReportFile;
  }

  /**
   * Gets the user defined filter.
   * 
   * @return the user defined filter.
   */
  public String getFilter() {
    return getSettings().getString(PHPUNIT_FILTER_KEY);
  }

  /**
   * Gets the user defined boot strap.
   * 
   * @return the user defined filter.
   */
  public String getBootstrap() {
    return getSettings().getString(PHPUNIT_BOOTSTRAP_KEY);
  }

  /**
   * Gets the user defined configuration file.
   * 
   * @return the user defined configuration file.
   */
  public String getConfiguration() {
    return getSettings().getString(PHPUNIT_CONFIGURATION_KEY);
  }

  /**
   * Gets the user defined loader.
   * 
   * @return the user defined loader.
   */
  public String getLoader() {
    return getSettings().getString(PHPUNIT_LOADER_KEY);
  }

  /**
   * Gets the user defined group.
   * 
   * @return the user defined group.
   */
  public String getGroup() {
    return getSettings().getString(PHPUNIT_GROUP_KEY);
  }

  /**
   * Checks if analyze test directories.
   * 
   * @return true, if analyze test directories
   */
  public boolean isAnalyseTestDirectory() {
    return getBooleanFromSettings(PHPUNIT_ANALYZE_TEST_DIRECTORY_KEY);
  }

  /**
   * Checks if default configuration should be ignored.
   * 
   * @return true, if default conf should be ignored
   */
  public boolean isIgnoreDefaultConfiguration() {
    return getBooleanFromSettings(PHPUNIT_IGNORE_CONFIGURATION_KEY);
  }

  /**
   * Gets the main test class.
   * 
   * @return the main test class
   */
  public String getMainTestClass() {
    return getSettings().getString(PHPUNIT_MAIN_TEST_FILE_KEY);
  }

  /**
   * Gets the path of the main test class.
   * 
   * @return the path of main test class
   */
  public String getMainTestClassFilePath() {
    String mainFileName = getSettings().getString(PHPUNIT_MAIN_TEST_FILE_KEY);
    List<File> directories = new ArrayList<File>(getTestDirectories());
    directories.addAll(getSourceDirectories());

    // find the first occurrence of the test file name class in the test directories.
    // if no file with that name is found in test directories, check in the sources
    for (File directory : directories) {
      if (directory.isDirectory()) {
        File file = new File(directory.getAbsolutePath(), mainFileName);
        if (file.exists()) {
          return file.getAbsolutePath();
        }
      }
    }
    // Otherwise return the file in the base directory
    File file = new File(getFileSystem().getBasedir(), mainFileName);
    if (!file.exists()) {
      StringBuilder message = new StringBuilder("The specified main class file cannot be found: ");
      message.append(mainFileName).append(". If you don't have a main test file, consider using a phpunit.xml file and do not ");
      message.append("use ").append(PHPUNIT_MAIN_TEST_FILE_KEY).append(" property.");
      throw new IllegalStateException(message.toString());
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

  @Override
  protected String getReportPathKey() {
    return PHPUNIT_REPORT_PATH_KEY;
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

}
