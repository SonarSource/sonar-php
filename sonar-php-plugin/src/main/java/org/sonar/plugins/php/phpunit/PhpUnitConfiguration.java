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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.AbstractPhpPluginConfiguration;
import org.sonar.plugins.php.core.Php;

/**
 * This class handles the php unit configuration.
 * 
 * @version 0.1 @author JTama
 * @version 0.3 @author Akram Ben Aissi
 */
public class PhpUnitConfiguration extends AbstractPhpPluginConfiguration {

  public static final String PHPUNIT_DEFAULT_REPORT_FILE_NAME = "phpunit.xml";
  public static final String PHPUNIT_DEFAULT_REPORT_FILE_PATH = "/logs";
  public static final String PHPUNIT_REPORT_FILE_NAME_PROPERTY_KEY = "sonar.phpUnit.reportFileName";
  public static final String PHPUNIT_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = "sonar.phpUnit.reportFileRelativePath";
  public static final String PHPUNIT_ANALYZE_ONLY_PROPERTY_KEY = "sonar.phpUnit.analyzeOnly";
  public static final String PHPUNIT_DEFAULT_ANALYZE_ONLY = "false";
  public static final String PHPUNIT_SHOULD_RUN_PROPERTY_KEY = "sonar.phpUnit.shouldRun";
  public static final String PHPUNIT_DEFAULT_SHOULD_RUN = "true";
  private static final String PHPUNIT_COMMAND_LINE = "phpunit";

  public static final String PHPUNIT_MAIN_TEST_FILE_PROPERTY_KEY = "sonar.phpUnit.mainTestClass";
  public static final String PHPUNIT_DEFAULT_MAIN_TEST_FILE = "AllTests.php";
  public static final String PHPUNIT_DEFAULT_COVERAGE_REPORT_FILE = "phpunit.coverage.xml";
  public static final String PHPUNIT_COVERAGE_REPORT_FILE_PROPERTY_KEY = "sonar.phpUnit.coverageReportFile";
  public static final String PHPUNIT_SHOULD_RUN_COVERAGE_PROPERTY_KEY = "sonar.phpUnit.coverage.shouldRun";
  public static final String PHPUNIT_DEFAULT_SHOULD_RUN_COVERAGE = "true";

  public static final String PHPUNIT_FILTER_OPTION = "--filter=";
  public static final String PHPUNIT_FILTER_PROPERTY_KEY = "sonar.phpUnit.filter";
  public static final String PHPUNIT_DEFAULT_FILTER = " ";
  public static final String PHPUNIT_BOOTSTRAP_OPTION = "--bootstrap=";
  public static final String PHPUNIT_BOOTSTRAP_PROPERTY_KEY = "sonar.phpUnit.bootstrap";
  public static final String PHPUNIT_DEFAULT_BOOTSTRAP = " ";
  public static final String PHPUNIT_CONFIGURATION_OPTION = "--configuration=";
  public static final String PHPUNIT_CONFIGURATION_PROPERTY_KEY = "sonar.phpUnit.configuration";
  public static final String PHPUNIT_DEFAULT_CONFIGURATION = " ";
  public static final String PHPUNIT_LOADER_OPTION = "--loader=";
  public static final String PHPUNIT_LOADER_PROPERTY_KEY = "sonar.phpUnit.loader";
  public static final String PHPUNIT_DEFAULT_LOADER = " ";
  public static final String PHPUNIT_GROUP_OPTION = "--group=";
  public static final String PHPUNIT_GROUP_PROPERTY_KEY = "sonar.phpUnit.group";
  public static final String PHPUNIT_DEFAULT_GROUP = " ";
  public static final String PHPUNIT_ARGUMENT_LINE_KEY = "sonar.phpUnit.argumentLine";
  public static final String PHPUNIT_DEFAULT_ARGUMENT_LINE = "";

  public static final String PHPUNIT_MAIN_TEST_FILE_MESSAGE = "File containing the main method calling all the tests";
  public static final String PHPUNIT_MAIN_TEST_FILE_DESCRIPTION = "The project main test file including the relative path "
      + "ie : \"/source/tests/AllTests.php\". If not present, phpunit will look for phpunit.xml file in test directory.";
  public static final String PHPUNIT_ANALYZE_ONLY_DESCRIPTION = "If set to true the plugin will only parse the analyzis "
      + "result file. If set to false the plugin will launch tool and parse result. If the option sonar.dynamicAnalisys is set to true,"
      + " this plugin will also parse analyzis file only.";
  public static final String PHPUNIT_SHOULD_RUN_DESCRIPTION = "If set to true the plugin will launch tool and parse result."
      + " If set to false the plugin will only parse the result file.";
  public static final String PHPUNIT_SHOULD_RUN_COVERAGE_DESCRIPTION = "If set to true the plugin will compute coverage on php files";

  /**
   * Instantiates a new php unit configuration.
   * 
   * @param project
   *          the a project
   */
  public PhpUnitConfiguration(Project project) {
    super(project);
    if (getShouldAnalyzeOnlyKey() != null) {
      // Enable dynamic anaylisis if analyze only is set or dynamic analysis is set to false
      Configuration configuration = project.getConfiguration();
      analyzeOnly = configuration.getBoolean(getShouldAnalyzeOnlyKey(), shouldAnalyzeOnlyDefault())
          || !configuration.getBoolean(SONAR_DYNAMIC_ANALYSIS, DEFAULT_SONAR_DYNAMIC_ANALYSIS);
    }
  }

  /**
   * Gets the main test class.
   * 
   * @return the main test class
   */
  public String getMainTestClass() {
    String reportFileName = getProject().getConfiguration().getString(PHPUNIT_MAIN_TEST_FILE_PROPERTY_KEY, PHPUNIT_DEFAULT_MAIN_TEST_FILE);
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
    if ( !file.exists()) {
      StringBuilder message = new StringBuilder("The specificied main class file cannot be found: ");
      message.append(reportFileName).append(". If you don't have a main test file, consider using a phpunit.xml file and do not ");
      message.append("use ").append(PHPUNIT_MAIN_TEST_FILE_PROPERTY_KEY).append(" property.");
      throw new PhpUnitConfigurationException(message.toString());
    }
    return file.getAbsolutePath();
  }

  /**
   * Gets the argument line key.
   * 
   * @return the argument line key
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getArgumentLineKey()
   */
  @Override
  protected String getArgumentLineKey() {
    return PHPUNIT_ARGUMENT_LINE_KEY;
  }

  /**
   * Gets the command line.
   * 
   * @return the command line
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getCommandLine()
   */
  @Override
  protected String getCommandLine() {
    return PHPUNIT_COMMAND_LINE;
  }

  /**
   * Gets the default argument line.
   * 
   * @return the default argument line
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getDefaultArgumentLine()
   */
  @Override
  protected String getDefaultArgumentLine() {
    return PHPUNIT_DEFAULT_ARGUMENT_LINE;
  }

  /**
   * Gets the default report file name.
   * 
   * @return the default report file name
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getDefaultReportFileName()
   */
  @Override
  protected String getDefaultReportFileName() {
    return PHPUNIT_DEFAULT_REPORT_FILE_NAME;
  }

  /**
   * Gets the default report file path.
   * 
   * @return the default report file path
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getDefaultReportFilePath()
   */
  @Override
  protected String getDefaultReportFilePath() {
    return PHPUNIT_DEFAULT_REPORT_FILE_PATH;
  }

  /**
   * Gets the report file name key.
   * 
   * @return the report file name key
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getReportFileNameKey()
   */
  @Override
  protected String getReportFileNameKey() {
    return PHPUNIT_REPORT_FILE_NAME_PROPERTY_KEY;
  }

  /**
   * Gets the report file relative path key.
   * 
   * @return the report file relative path key
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getReportFileRelativePathKey()
   */
  @Override
  protected String getReportFileRelativePathKey() {
    return PHPUNIT_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;
  }

  /**
   * Gets the should analyze only key.
   * 
   * @return the should analyze only key
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getShouldAnalyzeOnlyKey()
   */
  @Override
  protected final String getShouldAnalyzeOnlyKey() {
    return PHPUNIT_ANALYZE_ONLY_PROPERTY_KEY;
  }

  /**
   * Gets the should run key.
   * 
   * @return the should run key
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getShouldRunKey()
   */
  @Override
  protected String getShouldRunKey() {
    return PHPUNIT_SHOULD_RUN_PROPERTY_KEY;
  }

  /**
   * Should analyze only default.
   * 
   * @return true, if should analyze only default
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#shouldAnalyzeOnlyDefault()
   */
  @Override
  protected final boolean shouldAnalyzeOnlyDefault() {
    return Boolean.parseBoolean(PHPUNIT_DEFAULT_ANALYZE_ONLY);
  }

  /**
   * Should run default.
   * 
   * @return true, if should run default
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#shouldRunDefault()
   */
  @Override
  protected boolean shouldRunDefault() {
    return Boolean.parseBoolean(PHPUNIT_DEFAULT_SHOULD_RUN);
  }

  /**
   * Should run coverage.
   * 
   * @return true, if successful
   */
  public boolean shouldRunCoverage() {
    return getProject().getConfiguration().getBoolean(PHPUNIT_SHOULD_RUN_COVERAGE_PROPERTY_KEY,
        Boolean.parseBoolean(PHPUNIT_DEFAULT_SHOULD_RUN_COVERAGE));
  }

  /**
   * Gets the coverage report file.
   * 
   * @return the coverage report file
   */
  public File getCoverageReportFile() {
    return new File(getProject().getFileSystem().getBuildDir(), new StringBuilder().append(getReportFileRelativePath())
        .append(File.separator)
        .append(getProject().getConfiguration().getString(PHPUNIT_COVERAGE_REPORT_FILE_PROPERTY_KEY, PHPUNIT_DEFAULT_COVERAGE_REPORT_FILE))
        .toString());
  }

  /**
   * Gets the user defined filter.
   * 
   * @return the user defined filter.
   */
  public String getFilter() {
    return getProject().getConfiguration().getString(PHPUNIT_FILTER_PROPERTY_KEY, PHPUNIT_DEFAULT_FILTER);
  }

  /**
   * Gets the user defined boot strap.
   * 
   * @return the user defined filter.
   */
  public String getBootstrap() {
    return getProject().getConfiguration().getString(PHPUNIT_BOOTSTRAP_PROPERTY_KEY, PHPUNIT_DEFAULT_BOOTSTRAP);
  }

  /**
   * Gets the user defined configuration file.
   * 
   * @return the user defined configuration file.
   */
  public String getConfiguration() {
    return getProject().getConfiguration().getString(PHPUNIT_CONFIGURATION_PROPERTY_KEY, PHPUNIT_DEFAULT_CONFIGURATION);
  }

  /**
   * Gets the user defined loader.
   * 
   * @return the user defined loader.
   */
  public String getLoader() {
    return getProject().getConfiguration().getString(PHPUNIT_LOADER_PROPERTY_KEY, PHPUNIT_DEFAULT_LOADER);
  }

  /**
   * Gets the user defined group.
   * 
   * @return the user defined group.
   */
  public String getGroup() {
    return getProject().getConfiguration().getString(PHPUNIT_GROUP_PROPERTY_KEY, PHPUNIT_DEFAULT_GROUP);
  }

  public final boolean shouldExecuteOnProject() {
    return isShouldRun() && Php.INSTANCE.equals(getProject().getLanguage());
  }

}