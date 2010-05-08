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

package org.sonar.plugins.php.phpdepend.configuration;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.configuration.PhpPluginAbstractConfiguration;

/**
 * The php-depend plugin configuration class.
 */
public class PhpDependConfiguration extends PhpPluginAbstractConfiguration {

  /** Pdepend COMMAND_LINE. */
  public static final String COMMAND_LINE = "pdepend";

  /** The default report file name. */
  public static final String DEFAULT_REPORT_FILE_NAME = "pdepend.xml";

  /** The default report path beginning after {PROJETC_BUILD_PATH}. */
  public static final String DEFAULT_REPORT_FILE_PATH = "/logs";

  /** The command line default path. */
  protected static final String COMMAND_LINE_DEFAUT_PATH = "";

  /** Pdepend path property key. */
  protected static final String KEY_PATH = "sonar.phpdepend.path";

  /** The pdepend PHPUNIT option. */
  protected static final String PHPUNIT_OPT = "phpunit-xml";

  /** The report file name property key. */
  public static final String REPORT_FILE_NAME_PROPERTY_KEY = "sonar.phpDepend.reportFileName";

  /** The analyze only property key. */
  public static final String ANALYZE_ONLY_PROPERTY_KEY = "sonar.phpDepend.analyzeOnly";

  /** The analyze only default value. */
  public static final String DEFAULT_ANALYZE_ONLY = "false";

  /** The should run property key. */
  public static final String SHOULD_RUN_PROPERTY_KEY = "sonar.phpDepend.shouldRun";

  /** The should run default value. */
  public static final String DEFAULT_SHOULD_RUN = "true";

  /** The relative report path property key. */
  public static final String REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = "sonar.phpDepend.reportFileRelativePath";

  /** The command line suffix option. */
  public static final String SUFFIXES_OPT = "suffix";

  /** The command line ignore directory property key. */
  public static final String IGNORE_KEY = "sonar.phpDepend.ignore";

  /** The default ignore directory option. */
  public static final String DEFAULT_IGNORE = " ";

  /** The ignore directory option. */
  public static final String IGNORE_OPTION = "--ignore=";

  /** The command line include directory option. */
  public static final String EXCLUDE_PACKAGE_KEY = "sonar.phpDepend.exclude";

  /** The default exclude directory option. */
  public static final String DEFAULT_EXCLUDE_PACKAGES = " ";

  /** The exclude directory option. */
  public static final String EXCLUDE_OPTION = "--exclude=";

  /** The command line without annotations option. */
  public static final String WITHOUT_ANNOTATION_KEY = "sonar.phpDepend.withoutAnnotations";

  /** The default without annotations option. */
  public static final String DEFAULT_WITHOUT_ANNOTATION = "false";

  /** The without annotations option. */
  public static final String WITHOUT_ANNOTATION_OPTION = "--without-annotations=";

  /** The command line include directory option. */
  public static final String BAD_DOCUMENTATION_KEY = "sonar.phpDepend.badDocumentation";

  /** The default exclude directory option. */
  public static final String DEFAULT_BAD_DOCUMENTATION = "false";

  /** The exclude directory option. */
  public static final String BAD_DOCUMENTATION_OPTION = "--bad-documentation=";

  /** The Constant ARGUMENT_LINE_KEY. */
  public static final String ARGUMENT_LINE_KEY = "sonar.phpDepend.argumentLine";

  /** The Constant DEFAULT_ARGUMENT_LINE. */
  public static final String DEFAULT_ARGUMENT_LINE = "";

  // Only for unit tests
  /**
   * Instantiates a new php depend configuration.
   */
  protected PhpDependConfiguration() {
  }

  /**
   * Instantiates a new php depend configuration depending on given project.
   * 
   * @param project
   *          the project to be analyzed
   */
  public PhpDependConfiguration(Project project) {
    super();
    init(project);
  }

  /**
   * Gets the command line path.
   * 
   * @return the command line path
   */
  protected String getCommandLinePath() {
    return getProject().getConfiguration().getString(KEY_PATH, COMMAND_LINE_DEFAUT_PATH);
  }

  /**
   * Gets the report filecommand option.
   * 
   * @return the report filecommand option
   */
  public String getReportFileCommandOption() {
    return "--" + PHPUNIT_OPT + "=" + getReportFile().getAbsolutePath();
  }

  /**
   * Gets the suffixes command option.
   * 
   * @return the suffixes command option
   */
  public String getSuffixesCommandOption() {
    return "--" + SUFFIXES_OPT + "=" + StringUtils.join(Php.INSTANCE.getFileSuffixes(), ",");
  }

  @Override
  protected String getArgumentLineKey() {
    return ARGUMENT_LINE_KEY;
  }

  @Override
  protected String getDefaultArgumentLine() {
    return DEFAULT_ARGUMENT_LINE;
  }

  @Override
  protected String getDefaultReportFileName() {
    return DEFAULT_REPORT_FILE_NAME;
  }

  @Override
  protected String getDefaultReportFilePath() {
    return DEFAULT_REPORT_FILE_PATH;
  }

  @Override
  protected String getReportFileNameKey() {
    return REPORT_FILE_NAME_PROPERTY_KEY;
  }

  @Override
  protected String getReportFileRelativePathKey() {
    return REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;
  }

  @Override
  protected String getShouldAnalyzeOnlyKey() {
    return ANALYZE_ONLY_PROPERTY_KEY;
  }

  @Override
  protected String getShouldRunKey() {
    return SHOULD_RUN_PROPERTY_KEY;
  }

  @Override
  protected boolean shouldAnalyzeOnlyDefault() {
    return Boolean.parseBoolean(DEFAULT_ANALYZE_ONLY);
  }

  @Override
  protected boolean shouldRunDefault() {
    return Boolean.parseBoolean(DEFAULT_SHOULD_RUN);
  }

  @Override
  protected String getCommandLine() {
    return COMMAND_LINE;
  }

  public String getExcludePackeges() {
    String[] values = getProject().getConfiguration().getStringArray(EXCLUDE_PACKAGE_KEY);
    if (values != null && values.length > 0) {
      return StringUtils.join(values, ',');
    }
    return null;
  }

  public String getIgnoreDirs() {
    String[] values = getProject().getConfiguration().getStringArray(IGNORE_KEY);
    if (values != null && values.length > 0) {
      return StringUtils.join(values, ',');
    }
    return null;
  }

  public boolean isBadDocumentation() {
    return getProject().getConfiguration().getBoolean(BAD_DOCUMENTATION_KEY, Boolean.valueOf(DEFAULT_BAD_DOCUMENTATION));
  }

  public boolean isWithoutAnnotation() {
    return getProject().getConfiguration().getBoolean(WITHOUT_ANNOTATION_KEY, Boolean.valueOf(DEFAULT_WITHOUT_ANNOTATION));
  }

}
