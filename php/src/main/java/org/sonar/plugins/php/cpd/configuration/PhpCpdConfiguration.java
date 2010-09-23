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

/***

 */
package org.sonar.plugins.php.cpd.configuration;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration;

/***
 * @author akram
 */
public class PhpCpdConfiguration extends PhpPluginAbstractConfiguration {

  /** PhpCpd command line. */
  public static final String COMMAND_LINE = "phpcpd";
  public static final String REPORT_FILE_OPTION = "--log-pmd";
  /** The report file name property key. */
  public static final String REPORT_FILE_NAME_PROPERTY_KEY = "sonar.phpcpd.reportFileName";
  /** The relative report path property key. */
  public static final String REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = "sonar.phpcpd.reportFileRelativePath";

  /** The default report file name. */
  public static final String DEFAULT_REPORT_FILE_NAME = "php-cpd.xml";
  public static final String SUFFIXES = "--suffixes";
  public static final String EXCLUDE_PACKAGE_KEY = "sonar.phpcpd.excludes";
  public static final String EXCLUDE_OPTION = "--exclude";
  public static final String DIRECTORY_SEPARATOR = " ";
  private static final String SUFFIXE_SEPARATOR = ",";
  /** The default report path beginning after {PROJETC_BUILD_PATH}. */
  public static final String DEFAULT_REPORT_FILE_PATH = "/logs";
  /** The should run property key. */
  public static final String SHOULD_RUN_PROPERTY_KEY = "sonar.phpcpd.shouldRun";

  /**
   * Instantiates a new php cpd configuration.
   * 
   * @param project
   *          the project
   */
  public PhpCpdConfiguration(Project project) {
    super();
    super.init(project);
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getCommandLine()
   */
  @Override
  protected String getCommandLine() {
    return COMMAND_LINE;
  }

  /**
   * Gets the suffixes command option.
   * 
   * @return the suffixes command option
   */
  public String getSuffixesCommandOption() {
    return StringUtils.join(Php.INSTANCE.getFileSuffixes(), SUFFIXE_SEPARATOR);
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getArgumentLineKey()
   */
  @Override
  protected String getArgumentLineKey() {
    return "";
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getDefaultArgumentLine()
   */
  @Override
  protected String getDefaultArgumentLine() {
    return "";
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getDefaultReportFileName()
   */
  @Override
  protected String getDefaultReportFileName() {
    return DEFAULT_REPORT_FILE_NAME;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getShouldRunKey()
   */
  @Override
  protected String getShouldRunKey() {
    return SHOULD_RUN_PROPERTY_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#shouldAnalyzeOnlyDefault()
   */
  @Override
  protected boolean shouldAnalyzeOnlyDefault() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#shouldRunDefault()
   */
  @Override
  protected boolean shouldRunDefault() {
    return true;
  }

  @Override
  protected String getDefaultReportFilePath() {
    return DEFAULT_REPORT_FILE_PATH;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getReportFileRelativePathKey()
   */
  @Override
  protected String getReportFileRelativePathKey() {
    return REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getReportFileNameKey()
   */
  @Override
  protected String getReportFileNameKey() {
    return REPORT_FILE_NAME_PROPERTY_KEY;
  }

  @Override
  protected String getShouldAnalyzeOnlyKey() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @return
   */
  public String getExcludePackages() {
    String[] values = getProject().getConfiguration().getStringArray(EXCLUDE_PACKAGE_KEY);
    if (values != null && values.length > 0) {
      return StringUtils.join(values, DIRECTORY_SEPARATOR);
    }
    return null;
  }
}
