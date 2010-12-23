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

package org.sonar.plugins.php.pmd;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.AbstractPhpPluginConfiguration;

/**
 * The PhpPmd configuration class. It handles report file path and name with default options
 */
public class PhpmdConfiguration extends AbstractPhpPluginConfiguration {

  private static final String PHPMD_COMMAND_LINE = "phpmd";
  public static final String PHPMD_DEFAULT_REPORT_FILE_NAME = "pmd.xml";
  public static final String PHPMD_DEFAULT_REPORT_FILE_PATH = "/logs";
  public static final String PHPMD_REPORT_FILE_NAME_PROPERTY_KEY = "sonar.phpPmd.reportFileName";
  public static final String PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = "sonar.phpPmd.reportFileRelativePath";

  public static final String PHPMD_SHOULD_RUN_KEY = "sonar.phpPmd.shouldRun";
  public static final String PHPMD_SHOULD_RUN_MESSAGE = "Execute PHPMD";
  public static final String PHPMD_DEFAULT_SHOULD_RUN = "true";
  public static final String PHPMD_SHOULD_RUN_DESCRIPTION = "Disabling PHPMD is not a good idea because almost all metrics rely on it.";

  public static final String PHPMD_ARGUMENT_LINE_KEY = "sonar.phpPmd.argumentLine";
  public static final String PHPMD_DEFAULT_ARGUMENT_LINE = " ";

  public static final String PHPMD_ANALYZE_ONLY_KEY = "sonar.phpPmd.analyzeOnly";
  public static final String PHPMD_DEFAULT_ANALYZE_ONLY = "false";
  public static final String PHPMD_ANALYZE_ONLY_MESSAGE = "Only analyze existing Phpmd report files";
  public static final String PHPMD_ANALYZE_ONLY_DESCRIPTION = "If set to true the plugin will the plugin will only parse the result file."
      + " If set to false launch tool and parse result.";

  public static final String PHPMD_REPORT_FILE_OPTION = "--reportfile";
  public static final String PHPMD_LEVEL_ARGUMENT_KEY = "sonar.phpPmd.minimumPriority";
  public static final String PHPMD_LEVEL_OPTION = "--minimumpriority";
  public static final String PHPMD_DEFAULT_LEVEL_ARGUMENT = "2";
  public static final String PHPMD_DEFAULT_LEVEL_DESCRIPTION = "The lowest level events won't be included in report file.Values "
      + "goes from 1(Strong) to 5(Weak) (only integers)";

  public static final String PHPMD_DEFAULT_IGNORE_ARGUMENT = " ";
  public static final String PHPMD_EXTENSIONS_OPTION = "--extensions";
  public static final String PHPMD_IGNORE_ARGUMENT_KEY = "sonar.phpPmd.ignore";
  public static final String PHPMD_IGNORE_OPTION = "--ignore";
  public static final String PHPMD_REPORT_FORMAT = "xml";
  public static final String PHPMD_DEFAULT_RULESET_ARGUMENT = "codesize,unusedcode,naming";
  public static final String PHPMD_RULESETS_ARGUMENT_KEY = "sonar.phpPmd.Rulsets";

  /**
   * Instantiates a new php pmd configuration.
   * 
   * @param project
   *          the project
   */
  public PhpmdConfiguration(Project project) {
    super(project);
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
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration #getPhpunitArgumentLineKey()
   */
  @Override
  protected String getArgumentLineKey() {
    return PHPMD_ARGUMENT_LINE_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getCommandLine()
   */
  @Override
  protected String getCommandLine() {
    return PHPMD_COMMAND_LINE;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration #getPhpunitDefaultArgumentLine()
   */
  @Override
  protected String getDefaultArgumentLine() {
    return null;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration #getDefaultReportFileName()
   */
  @Override
  protected String getDefaultReportFileName() {
    return PHPMD_DEFAULT_REPORT_FILE_NAME;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration #getPhpunitDefaultReportFilePath()
   */
  @Override
  protected String getDefaultReportFilePath() {
    return PHPMD_DEFAULT_REPORT_FILE_PATH;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration #getReportFileNameKey()
   */
  @Override
  protected String getReportFileNameKey() {
    return PHPMD_REPORT_FILE_NAME_PROPERTY_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration #getReportFileRelativePathKey()
   */
  @Override
  protected String getReportFileRelativePathKey() {
    return PHPMD_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration #getShouldAnalyzeOnlyKey()
   */
  @Override
  protected String getShouldAnalyzeOnlyKey() {
    return PHPMD_ANALYZE_ONLY_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration #getShouldRunKey()
   */
  @Override
  protected String getShouldRunKey() {
    return PHPMD_SHOULD_RUN_KEY;
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration #shouldAnalyzeOnlyDefault()
   */
  @Override
  protected boolean shouldAnalyzeOnlyDefault() {
    return Boolean.getBoolean(PHPMD_DEFAULT_ANALYZE_ONLY);
  }

  /**
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration #shouldRunDefault()
   */
  @Override
  protected boolean shouldRunDefault() {
    return Boolean.getBoolean(PHPMD_DEFAULT_SHOULD_RUN);
  }

  /**
   * Gets the level.
   * 
   * @return the level
   */
  public String getLevel() {
    return getProject().getConfiguration().getString(PHPMD_LEVEL_ARGUMENT_KEY, PHPMD_DEFAULT_LEVEL_ARGUMENT);
  }

  /**
   * Gets the ignore list.
   * 
   * @return the ignore list
   */
  public String getIgnoreList() {
    String[] values = getProject().getConfiguration().getStringArray(PHPMD_IGNORE_ARGUMENT_KEY);
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
    String[] values = getProject().getConfiguration().getStringArray(PHPMD_RULESETS_ARGUMENT_KEY);
    if (values != null && values.length > 0) {
      return StringUtils.join(values, ',');
    }
    return PHPMD_DEFAULT_RULESET_ARGUMENT;
  }
}
