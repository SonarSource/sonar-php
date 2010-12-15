/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 EchoSource
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
package org.sonar.plugins.php.cpd;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.AbstractPhpPluginConfiguration;
import org.sonar.plugins.php.core.Php;

/***
 * @author akram
 */
public class PhpCpdConfiguration extends AbstractPhpPluginConfiguration {

  /** PhpCpd command line. */
  public static final String PHPCPD_COMMAND_LINE = "phpcpd";
  public static final String PHPCPD_REPORT_FILE_OPTION = "--log-pmd";
  /** The report file name property key. */
  public static final String PHPCPD_REPORT_FILE_NAME_PROPERTY_KEY = "sonar.phpcpd.reportFileName";
  /** The relative report path property key. */
  public static final String PHPCPD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = "sonar.phpcpd.reportFileRelativePath";

  /** The default report file name. */
  public static final String PHPCPD_DEFAULT_REPORT_FILE_NAME = "php-cpd.xml";
  public static final String PHPCPD_SUFFIXES = "--suffixes";
  public static final String PHPCPD_EXCLUDE_PACKAGE_KEY = "sonar.phpcpd.excludes";
  public static final String PHPCPD_EXCLUDE_OPTION = "--exclude";

  public static final String PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_LINES_KEY = "sonar.phpcpd.min.lines";
  public static final String PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_LINES_MODIFIER = "--min-lines";
  public static final String PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_LINES = "3";

  public static final String PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS_KEY = "sonar.phpcpd.min.tokens";
  public static final String PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS_MODIFIER = "--min-tokens";
  public static final String PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS = "5";

  public static final String PHPCPD_DIRECTORY_SEPARATOR = " ";
  public static final String PHPCPD_MODIFIER_VALUE_SEPARATOR = " ";
  private static final String PHPCPD_SUFFIXE_SEPARATOR = ",";
  /** The default report path beginning after {PROJETC_BUILD_PATH}. */
  public static final String PHPCPD_DEFAULT_REPORT_FILE_PATH = "/logs";
  /** The should run property key. */
  public static final String PHPCPD_SHOULD_RUN_PROPERTY_KEY = "sonar.phpcpd.shouldRun";
  public static final String PHPCPD_DEFAULT_SHOULD_RUN = "true";

  public static final String PHPCPD_ANALYZE_ONLY_KEY = "sonar.phpcpd.analyseOnly";

  /**
   * Instantiates a new php cpd configuration.
   * 
   * @param project
   *          the project
   */
  public PhpCpdConfiguration(Project project) {
    super(project);
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getCommandLine()
   */
  @Override
  protected String getCommandLine() {
    return PHPCPD_COMMAND_LINE;
  }

  /**
   * Gets the suffixes command option.
   * 
   * @return the suffixes command option
   */
  public String getSuffixesCommandOption() {
    return StringUtils.join(Php.INSTANCE.getFileSuffixes(), PHPCPD_SUFFIXE_SEPARATOR);
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getPhpunitArgumentLineKey()
   */
  @Override
  protected String getArgumentLineKey() {
    return "";
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getPhpunitDefaultArgumentLine()
   */
  @Override
  protected String getDefaultArgumentLine() {
    return "";
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getDefaultReportFileName()
   */
  @Override
  protected String getDefaultReportFileName() {
    return PHPCPD_DEFAULT_REPORT_FILE_NAME;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getShouldRunKey()
   */
  @Override
  protected String getShouldRunKey() {
    return PHPCPD_SHOULD_RUN_PROPERTY_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#shouldAnalyzeOnlyDefault()
   */
  @Override
  protected boolean shouldAnalyzeOnlyDefault() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#shouldRunDefault()
   */
  @Override
  protected boolean shouldRunDefault() {
    return true;
  }

  @Override
  protected String getDefaultReportFilePath() {
    return PHPCPD_DEFAULT_REPORT_FILE_PATH;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getReportFileRelativePathKey()
   */
  @Override
  protected String getReportFileRelativePathKey() {
    return PHPCPD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getReportFileNameKey()
   */
  @Override
  protected String getReportFileNameKey() {
    return PHPCPD_REPORT_FILE_NAME_PROPERTY_KEY;
  }

  protected String getShouldAnalyzeOnlyKey() {
    return PHPCPD_ANALYZE_ONLY_KEY;
  }

  /**
   * @return
   */
  public String getMinimunNumberOfIdenticalLines() {
    Configuration configuration = getProject().getConfiguration();
    return configuration.getString(PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_LINES_KEY, PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_LINES);
  }

  /**
   * @return
   */
  public String getMinimunNumberOfIdenticalTokens() {
    Configuration configuration = getProject().getConfiguration();
    return configuration.getString(PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS_KEY, PHPCPD_DEFAULT_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS);
  }

  /**
   * @return
   */
  public String getExcludePackages() {
    String[] values = getProject().getConfiguration().getStringArray(PHPCPD_EXCLUDE_PACKAGE_KEY);
    if (values != null && values.length > 0) {
      return StringUtils.join(values, PHPCPD_DIRECTORY_SEPARATOR);
    }
    return null;
  }

  public boolean shouldExecuteOnProject(Project project) {
    return isShouldRun() && Php.INSTANCE.equals(project.getLanguage());
  }

}
