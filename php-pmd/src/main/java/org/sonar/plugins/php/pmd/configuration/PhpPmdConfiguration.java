/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 MyCompany
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

package org.sonar.plugins.php.pmd.configuration;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration;

/**
 * The PhpPmd configuration class. It handles report file path and name with default options
 */
public class PhpPmdConfiguration extends PhpPluginAbstractConfiguration {

  /** The Constant COMMAND_LINE. */
  private static final String COMMAND_LINE = "phpmd";

  /** The Constant DEFAULT_REPORT_FILE_NAME. */
  public static final String DEFAULT_REPORT_FILE_NAME = "pmd.xml";

  /** The Constant DEFAULT_REPORT_FILE_PATH. */
  public static final String DEFAULT_REPORT_FILE_PATH = "/logs";

  /** The Constant REPORT_FILE_NAME_PROPERTY_KEY. */
  public static final String REPORT_FILE_NAME_PROPERTY_KEY = "sonar.phpPmd.reportFileName";

  /** The Constant REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY. */
  public static final String REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = "sonar.phpPmd.reportFileRelativePath";

  /** The Constant SHOULD_ANALYSE_ONLY_KEY. */
  public static final String ANALYZE_ONLY_KEY = "sonar.phpPmd.analyseOnly";

  /** The Constant SHOULD_RUN_KEY. */
  public static final String SHOULD_RUN_KEY = "sonar.phpPmd.shouldRun";

  /** The Constant ARGUMENT_LINE_KEY. */
  public static final String ARGUMENT_LINE_KEY = "sonar.phpPmd.argumentLine";

  /** The Constant DEFAULT_ARGUMENT_LINE. */
  public static final String DEFAULT_ARGUMENT_LINE = " ";

  /** The Constant DEFAULT_SHOULD_ANALYSE_ONLY. */
  public static final String DEFAULT_ANALYZE_ONLY = "false";

  /** The Constant DEFAULT_SHOULD_RUN. */
  public static final String DEFAULT_SHOULD_RUN = "true";

  /** The Constant REPORT_FILE_OPTION. */
  public static final String REPORT_FILE_OPTION = "--reportfile";

  /** The Constant LEVEL_ARGUMENT_KEY. */
  public static final String LEVEL_ARGUMENT_KEY = "sonar.phpPmd.minimumPriority";

  /** The Constant LEVEL_OPTION. */
  public static final String LEVEL_OPTION = "--minimumpriority";

  /** The Constant DEFAULT_LEVEL_ARGUMENT. */
  public static final String DEFAULT_LEVEL_ARGUMENT = "2";

  /** The Constant DEFAULT_IGNORE_ARGUMENT. */
  public static final String DEFAULT_IGNORE_ARGUMENT = " ";

  /** The Constant EXTENSIONS_OPTION. */
  public static final String EXTENSIONS_OPTION = "--extensions";

  /** The Constant IGNORE_ARGUMENT_KEY. */
  public static final String IGNORE_ARGUMENT_KEY = "sonar.phpPmd.ignore";

  /** The Constant IGNORE_OPTION. */
  public static final String IGNORE_OPTION = "--ignore";

  /** The Constant REPORT_FORMAT. */
  public static final String REPORT_FORMAT = "xml";

  /** The Constant DEFAULT_RULESET_ARGUMENT. */
  public static final String DEFAULT_RULESET_ARGUMENT = "codesize,unusedcode";

  /** The Constant RULESETS_ARGUMENT_KEY. */
  public static final String RULESETS_ARGUMENT_KEY = "sonar.phpPmd.Rulsets";

  /**
   * Instantiates a new php pmd configuration.
   */
  protected PhpPmdConfiguration() {
  }

  /**
   * Instantiates a new php pmd configuration.
   * 
   * @param project
   *          the project
   */
  public PhpPmdConfiguration(Project project) {
    init(project);
  }

  /**
   * Gets the project source folders.
   * 
   * @return List<File> the source folders
   */
  public List<File> getSourceDir() {
    return getProject().getFileSystem().getSourceDirs();
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration #getArgumentLineKey()
   */
  @Override
  protected String getArgumentLineKey() {
    return ARGUMENT_LINE_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration#getCommandLine()
   */
  @Override
  protected String getCommandLine() {
    return COMMAND_LINE;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration #getDefaultArgumentLine()
   */
  @Override
  protected String getDefaultArgumentLine() {
    return null;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration #getDefaultReportFileName()
   */
  @Override
  protected String getDefaultReportFileName() {
    return DEFAULT_REPORT_FILE_NAME;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration #getDefaultReportFilePath()
   */
  @Override
  protected String getDefaultReportFilePath() {
    return DEFAULT_REPORT_FILE_PATH;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration #getReportFileNameKey()
   */
  @Override
  protected String getReportFileNameKey() {
    return REPORT_FILE_NAME_PROPERTY_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration #getReportFileRelativePathKey()
   */
  @Override
  protected String getReportFileRelativePathKey() {
    return REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration #getShouldAnalyzeOnlyKey()
   */
  @Override
  protected String getShouldAnalyzeOnlyKey() {
    return ANALYZE_ONLY_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration #getShouldRunKey()
   */
  @Override
  protected String getShouldRunKey() {
    return SHOULD_RUN_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration #shouldAnalyzeOnlyDefault()
   */
  @Override
  protected boolean shouldAnalyzeOnlyDefault() {
    return Boolean.getBoolean(DEFAULT_ANALYZE_ONLY);
  }

  /**
   * @see org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration #shouldRunDefault()
   */
  @Override
  protected boolean shouldRunDefault() {
    return Boolean.getBoolean(DEFAULT_SHOULD_RUN);
  }

  /**
   * Gets the level.
   * 
   * @return the level
   */
  public String getLevel() {
    return getProject().getConfiguration().getString(LEVEL_ARGUMENT_KEY, DEFAULT_LEVEL_ARGUMENT);
  }

  /**
   * Gets the ignore list.
   * 
   * @return the ignore list
   */
  public String getIgnoreList() {
    String[] values = getProject().getConfiguration().getStringArray(IGNORE_ARGUMENT_KEY);
    if (values != null && values.length > 0) {
      return StringUtils.join(values, ',');
    }
    return null;
  }

  /**
   * Gets the rulesets.
   * 
   * @return the rulesets
   */
  public String getRulesets() {
    String[] values = getProject().getConfiguration().getStringArray(RULESETS_ARGUMENT_KEY);
    if (values != null && values.length > 0) {
      return StringUtils.join(values, ',');
    }
    return DEFAULT_RULESET_ARGUMENT;
  }
}
