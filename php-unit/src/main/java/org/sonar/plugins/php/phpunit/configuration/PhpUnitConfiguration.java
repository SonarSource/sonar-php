/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 SQLi
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.php.phpunit.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration;

/**
 * This class handles the php unit configuration.
 * 
 * @version 0.1
 * @author JTama
 */
public class PhpUnitConfiguration extends PhpPluginAbstractConfiguration {

  /** Default value used for the PhpUnit report file name. */
  public static final String DEFAULT_REPORT_FILE_NAME = "phpunit.xml";

  /** Default value used for the relative PhpUnit report file path from the folder "Project dir\target". */
  public static final String DEFAULT_REPORT_FILE_PATH = "/logs";

  /** Punit report file name's property name. */
  public static final String REPORT_FILE_NAME_PROPERTY_KEY = "sonar.phpUnit.reportFileName";

  /** Punit report file path's property name. */
  public static final String REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = "sonar.phpUnit.reportFileRelativePath";

  /** The analyze only property key. */
  public static final String ANALYZE_ONLY_PROPERTY_KEY = "sonar.phpUnit.analyzeOnly";

  /** The analyze only default value. */
  public static final String DEFAULT_ANALYZE_ONLY = "false";

  /** The should run property key. */
  public static final String SHOULD_RUN_PROPERTY_KEY = "sonar.phpUnit.shouldRun";

  /** The should run default value. */
  public static final String DEFAULT_SHOULD_RUN = "true";

  /** The Constant COMMAND_LINE. */
  private static final String COMMAND_LINE = "phpunit";

  /** The Constant MAIN_TEST_CLASS_PROPERTY_KEY. */
  public static final String MAIN_TEST_FILE_PROPERTY_KEY = "sonar.phpUnit.mainTestClass";

  /** The Constant DEFAULT_MAIN_TEST_CLASS. */
  public static final String DEFAULT_MAIN_TEST_FILE = "AllTests.php";

  /** The Constant DEFAULT_COVERAGE_REPORT_FILE. */
  public static final String DEFAULT_COVERAGE_REPORT_FILE = "phpunit.coverage.xml";

  /** The Constant COVERAGE_REPORT_FILE_PROPERTY_KEY. */
  public static final String COVERAGE_REPORT_FILE_PROPERTY_KEY = "sonar.phpUnit.coverageReportFile";

  /** The should run property key. */
  public static final String SHOULD_DEAL_WITH_COVERAGE_PROPERTY_KEY = "sonar.phpUnit.coverage.shouldRun";

  /** The should run default value. */
  public static final String DEFAULT_SHOULD_DEAL_WITH_COVERAGE = "true";

  /** The filter argument line option */
  public static final String FILTER_OPTION = "--filter=";

  /** The filter argument line property key. */
  public static final String FILTER_PROPERTY_KEY = "sonar.phpUnit.filter";

  /** The filter argument line default value. */
  public static final String DEFAULT_FILTER = " ";

  /** The bootstrap argument line option */
  public static final String BOOTSTRAP_OPTION = "--bootstrap=";

  /** The bootstrap argument line property key. */
  public static final String BOOTSTRAP_PROPERTY_KEY = "sonar.phpUnit.bootstrap";

  /** The bootstrap argument line default value. */
  public static final String DEFAULT_BOOTSTRAP = " ";

  /** The configuration argument line option */
  public static final String CONFIGURATION_OPTION = "--configuration=";

  /** The configuration argument line property key. */
  public static final String CONFIGURATION_PROPERTY_KEY = "sonar.phpUnit.configuration";

  /** The configuration argument line default value. */
  public static final String DEFAULT_CONFIGURATION = " ";

  /** The loader argument line option */
  public static final String LOADER_OPTION = "--loader=";

  /** The loader argument line property key. */
  public static final String LOADER_PROPERTY_KEY = "sonar.phpUnit.loader";

  /** The loader argument line default value. */
  public static final String DEFAULT_LOADER = " ";

  /** The group argument line option */
  public static final String GROUP_OPTION = "--group=";

  /** The group argument line property key. */
  public static final String GROUP_PROPERTY_KEY = "sonar.phpUnit.group";

  /** The group argument line default value. */
  public static final String DEFAULT_GROUP = " ";

  /** */
  public static final String ARGUMENT_LINE_KEY = "sonar.phpUnit.argumentLine";
  public static final String DEFAULT_ARGUMENT_LINE = "";

  public static final String PROJECT_CLASS_DESCRIPTION = "The project main test class including the relativ path ie : \"/source/tests/AllTests.php\"";
  public static final String DEFAULT_ANALYZE_ONLY_DESCRIPTION = "If set to true the plugin will only parse the analyzis result file. If set to false the plugin will launch tool and parse result.";
  public static final String DEFAULT_SHOULD_RUN_DESCRIPTION = "If set to true the plugin will launch tool and parse result. If set to false the plugin will only parse the result file.";
  public static final String DEFAULT_SHOULD_DEAL_DESCRIPTION = "If set to true the plugin will also take php coverage files into account";

  // Only for unit tests
  /**
   * Instantiates a new php unit configuration.
   */
  protected PhpUnitConfiguration() {
  }

  /**
   * Instantiates a new php unit configuration.
   * 
   * @param aProject
   *          the a project
   */
  public PhpUnitConfiguration(Project aProject) {
    super();
    init(aProject);
  }

  /**
   * Gets the main test class.
   * 
   * @return the main test class
   * @throws FileNotFoundException
   */
  public String getMainTestClass() {
    String reportFileName = getProject().getConfiguration().getString(MAIN_TEST_FILE_PROPERTY_KEY, DEFAULT_MAIN_TEST_FILE);
    List<File> directories = getTestDirectories();
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
      throw new PhpUnitConfigurationException("The specificied main class file cannot be found: " + reportFileName);
    }
    return file.getAbsolutePath();
  }

  /**
   * Gets the argument line key.
   * 
   * @return the argument line key
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getArgumentLineKey()
   */
  @Override
  protected String getArgumentLineKey() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Gets the command line.
   * 
   * @return the command line
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getCommandLine()
   */
  @Override
  protected String getCommandLine() {
    return COMMAND_LINE;
  }

  /**
   * Gets the default argument line.
   * 
   * @return the default argument line
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getDefaultArgumentLine()
   */
  @Override
  protected String getDefaultArgumentLine() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Gets the default report file name.
   * 
   * @return the default report file name
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getDefaultReportFileName()
   */
  @Override
  protected String getDefaultReportFileName() {
    return DEFAULT_REPORT_FILE_NAME;
  }

  /**
   * Gets the default report file path.
   * 
   * @return the default report file path
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getDefaultReportFilePath()
   */
  @Override
  protected String getDefaultReportFilePath() {
    return DEFAULT_REPORT_FILE_PATH;
  }

  /**
   * Gets the report file name key.
   * 
   * @return the report file name key
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getReportFileNameKey()
   */
  @Override
  protected String getReportFileNameKey() {
    return REPORT_FILE_NAME_PROPERTY_KEY;
  }

  /**
   * Gets the report file relative path key.
   * 
   * @return the report file relative path key
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getReportFileRelativePathKey()
   */
  @Override
  protected String getReportFileRelativePathKey() {
    return REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;
  }

  /**
   * Gets the should analyze only key.
   * 
   * @return the should analyze only key
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getShouldAnalyzeOnlyKey()
   */
  @Override
  protected String getShouldAnalyzeOnlyKey() {
    return ANALYZE_ONLY_PROPERTY_KEY;
  }

  /**
   * Gets the should run key.
   * 
   * @return the should run key
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getShouldRunKey()
   */
  @Override
  protected String getShouldRunKey() {
    return SHOULD_RUN_PROPERTY_KEY;
  }

  /**
   * Should analyze only default.
   * 
   * @return true, if should analyze only default
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#shouldAnalyzeOnlyDefault()
   */
  @Override
  protected boolean shouldAnalyzeOnlyDefault() {
    return Boolean.parseBoolean(DEFAULT_ANALYZE_ONLY);
  }

  /**
   * Should run default.
   * 
   * @return true, if should run default
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#shouldRunDefault()
   */
  @Override
  protected boolean shouldRunDefault() {
    return Boolean.parseBoolean(DEFAULT_SHOULD_RUN);
  }

  /**
   * Should run coverage.
   * 
   * @return true, if successful
   */
  public boolean shouldRunCoverage() {
    return getProject().getConfiguration().getBoolean(SHOULD_DEAL_WITH_COVERAGE_PROPERTY_KEY,
        Boolean.parseBoolean(DEFAULT_SHOULD_DEAL_WITH_COVERAGE));
  }

  /**
   * Gets the coverage report file.
   * 
   * @return the coverage report file
   */
  public File getCoverageReportFile() {
    return new File(getProject().getFileSystem().getBuildDir(), new StringBuilder().append(getReportFileRelativePath()).append(
        File.separator).append(getProject().getConfiguration().getString(COVERAGE_REPORT_FILE_PROPERTY_KEY, DEFAULT_COVERAGE_REPORT_FILE))
        .toString());
  }

  /**
   * Gets the user defined filter.
   * 
   * @return the user defined filter.
   */
  public String getFilter() {
    return getProject().getConfiguration().getString(FILTER_PROPERTY_KEY, DEFAULT_FILTER);
  }

  /**
   * Gets the user defined boot strap.
   * 
   * @return the user defined filter.
   */
  public String getBootstrap() {
    return getProject().getConfiguration().getString(BOOTSTRAP_PROPERTY_KEY, DEFAULT_BOOTSTRAP);
  }

  /**
   * Gets the user defined configuration file.
   * 
   * @return the user defined configuration file.
   */
  public String getConfiguration() {
    return getProject().getConfiguration().getString(CONFIGURATION_PROPERTY_KEY, DEFAULT_CONFIGURATION);
  }

  /**
   * Gets the user defined loader.
   * 
   * @return the user defined loader.
   */
  public String getLoader() {
    return getProject().getConfiguration().getString(LOADER_PROPERTY_KEY, DEFAULT_LOADER);
  }

  /**
   * Gets the user defined group.
   * 
   * @return the user defined group.
   */
  public String getGroup() {
    return getProject().getConfiguration().getString(GROUP_PROPERTY_KEY, DEFAULT_GROUP);
  }
}
