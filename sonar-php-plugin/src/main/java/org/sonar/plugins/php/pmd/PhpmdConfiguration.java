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
package org.sonar.plugins.php.pmd;

import java.io.File;
import java.util.List;

import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.AbstractPhpConfiguration;

/**
 * The PhpPmd configuration class. It handles report file path and name with default options
 */
public class PhpmdConfiguration extends AbstractPhpConfiguration {

  private static final String PHPMD_COMMAND_LINE = "phpmd";

  // -- PHPMD tool options ---
  public static final String PHPMD_REPORT_FORMAT = "xml";
  public static final String PHPMD_REPORT_FILE_OPTION = "--reportfile";
  public static final String PHPMD_LEVEL_OPTION = "--minimumpriority";
  public static final String PHPMD_EXTENSIONS_OPTION = "--suffixes";
  public static final String PHPMD_DEFAULT_RULESET_ARGUMENT = "codesize,unusedcode,naming";

  // --- Sonar config parameters ---
  public static final String PHPMD_SKIP_KEY = "sonar.phpPmd.skip";
  public static final String PHPMD_SHOULD_RUN_KEY = "sonar.phpPmd.shouldRun"; // OLD param that will be removed soon
  public static final String PHPMD_ANALYZE_ONLY_KEY = "sonar.phpPmd.analyzeOnly";
  public static final String PHPMD_REPORT_FILE_RELATIVE_PATH_KEY = "sonar.phpPmd.reportFileRelativePath";
  public static final String PHPMD_REPORT_FILE_RELATIVE_PATH_DEFVALUE = "/logs";
  public static final String PHPMD_REPORT_FILE_NAME_KEY = "sonar.phpPmd.reportFileName";
  public static final String PHPMD_REPORT_FILE_NAME_DEFVALUE = "pmd.xml";
  public static final String PHPMD_LEVEL_ARGUMENT_KEY = "sonar.phpPmd.minimumPriority";
  public static final String PHPMD_LEVEL_ARGUMENT_DEFVALUE = "2";
  public static final String PHPMD_ARGUMENT_LINE_KEY = "sonar.phpPmd.argumentLine";
  public static final String PHPMD_TIMEOUT_KEY = "sonar.phpPmd.timeout";

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
   * {@inheritDoc}
   */
  @Override
  protected String getArgumentLineKey() {
    return PHPMD_ARGUMENT_LINE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getCommandLine() {
    return PHPMD_COMMAND_LINE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDefaultArgumentLine() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDefaultReportFileName() {
    return PHPMD_REPORT_FILE_NAME_DEFVALUE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDefaultReportFilePath() {
    return PHPMD_REPORT_FILE_RELATIVE_PATH_DEFVALUE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getReportFileNameKey() {
    return PHPMD_REPORT_FILE_NAME_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getReportFileRelativePathKey() {
    return PHPMD_REPORT_FILE_RELATIVE_PATH_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getShouldAnalyzeOnlyKey() {
    return PHPMD_ANALYZE_ONLY_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getShouldRunKey() {
    return PHPMD_SHOULD_RUN_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getSkipKey() {
    return PHPMD_SKIP_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getTimeoutKey() {
    return PHPMD_TIMEOUT_KEY;
  }

  /**
   * Gets the level.
   * 
   * @return the level
   */
  public String getLevel() {
    return getProject().getConfiguration().getString(PHPMD_LEVEL_ARGUMENT_KEY, PHPMD_LEVEL_ARGUMENT_DEFVALUE);
  }

  /**
   * Gets the rulesets.
   * 
   * @return the rulesets
   */
  public String getRulesets() {
    return PHPMD_DEFAULT_RULESET_ARGUMENT;
  }
}
