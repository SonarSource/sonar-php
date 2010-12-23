/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Akram Ben Aissi or Jerome Tama or Frederic Leroy
 * mailto: akram.benaissi@free.fr or jerome.tama@codehaus.org
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

package org.sonar.plugins.php.phpdepend;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.core.AbstractPhpPluginConfiguration;
import org.sonar.plugins.php.core.Php;

/**
 * The php-depend plugin configuration class.
 */
public class PhpDependConfiguration extends AbstractPhpPluginConfiguration {

  public static final String PDEPEND_COMMAND_LINE = "pdepend";
  public static final String PDEPEND_DEFAULT_REPORT_FILE_NAME = "pdepend.xml";
  public static final String DEFAULT_REPORT_FILE_PATH = "/logs";
  protected static final String PDEPEND_COMMAND_LINE_DEFAUT_PATH = "";
  protected static final String PDEPEND_KEY_PATH = "sonar.phpdepend.path";
  protected static final String PDEPEND_OPT = "phpunit-xml";

  public static final String PDEPEND_REPORT_FILE_NAME_PROPERTY_KEY = "sonar.phpDepend.reportFileName";
  public static final String PDEPEND_ANALYZE_ONLY_PROPERTY_KEY = "sonar.phpDepend.analyzeOnly";
  public static final String PDEPEND_DEFAULT_ANALYZE_ONLY = "false";
  public static final String PDEPEND_SHOULD_RUN_PROPERTY_KEY = "sonar.phpDepend.shouldRun";
  public static final String PDEPEND_DEFAULT_SHOULD_RUN = "true";
  public static final String PDEPEND_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = "sonar.phpDepend.reportFileRelativePath";
  public static final String PDEPEND_SUFFIXES_OPT = "suffix";
  public static final String PDEPEND_IGNORE_KEY = "sonar.phpDepend.ignore";
  public static final String PDEPEND_DEFAULT_IGNORE = " ";
  public static final String PDEPEND_IGNORE_OPTION = "--ignore=";

  public static final String PDEPEND_EXCLUDE_PACKAGE_KEY = "sonar.phpDepend.exclude";
  public static final String PDEPEND_DEFAULT_EXCLUDE_PACKAGES = " ";
  public static final String PDEPEND_EXCLUDE_OPTION = "--exclude=";
  public static final String PDEPEND_WITHOUT_ANNOTATION_KEY = "sonar.phpDepend.withoutAnnotations";
  public static final String PDEPEND_DEFAULT_WITHOUT_ANNOTATION = "false";
  public static final String PDEPEND_WITHOUT_ANNOTATION_OPTION = "--without-annotations=";
  public static final String PDEPEND_BAD_DOCUMENTATION_KEY = "sonar.phpDepend.badDocumentation";
  public static final String PDEPEND_DEFAULT_BAD_DOCUMENTATION = "false";
  public static final String PDEPEND_BAD_DOCUMENTATION_OPTION = "--bad-documentation=";
  public static final String PDEPEND_ARGUMENT_LINE_KEY = "sonar.phpDepend.argumentLine";
  public static final String PDEPEND_DEFAULT_ARGUMENT_LINE = "";

  public static final String PDEPEND_SHOULD_RUN_DESCRIPTION = "If set to true the plugin will launch tool and parse result."
      + " If set to false the plugin will only parse the result file.";

  public static final String PDEPEND_ANALYZE_ONLY_MESSAGE = "Only analyze existing pdepend files";
  public static final String PDEPEND_ANALYZE_ONLY_DESCRIPTION = "If set to true the plugin will the plugin will only parse "
      + "the result file. If set to false launch tool and parse result.";

  /**
   * Instantiates a new php depend configuration depending on given project.
   * 
   * @param project
   *          the project to be analyzed
   */
  public PhpDependConfiguration(Project project) {
    super(project);
  }

  /**
   * Gets the report filecommand option.
   * 
   * @return the report filecommand option
   */
  public String getReportFileCommandOption() {
    return "--" + PDEPEND_OPT + "=" + getReportFile().getAbsolutePath();
  }

  /**
   * Gets the suffixes command option.
   * 
   * @return the suffixes command option
   */
  public String getSuffixesCommandOption() {
    return "--" + PDEPEND_SUFFIXES_OPT + "=" + StringUtils.join(Php.INSTANCE.getFileSuffixes(), ",");
  }

  @Override
  protected String getArgumentLineKey() {
    return PDEPEND_ARGUMENT_LINE_KEY;
  }

  @Override
  protected String getDefaultArgumentLine() {
    return PDEPEND_DEFAULT_ARGUMENT_LINE;
  }

  @Override
  protected String getDefaultReportFileName() {
    return PDEPEND_DEFAULT_REPORT_FILE_NAME;
  }

  @Override
  protected String getDefaultReportFilePath() {
    return DEFAULT_REPORT_FILE_PATH;
  }

  @Override
  protected String getReportFileNameKey() {
    return PDEPEND_REPORT_FILE_NAME_PROPERTY_KEY;
  }

  @Override
  protected String getReportFileRelativePathKey() {
    return PDEPEND_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;
  }

  @Override
  protected String getShouldAnalyzeOnlyKey() {
    return PDEPEND_ANALYZE_ONLY_PROPERTY_KEY;
  }

  @Override
  protected String getShouldRunKey() {
    return PDEPEND_SHOULD_RUN_PROPERTY_KEY;
  }

  @Override
  protected boolean shouldAnalyzeOnlyDefault() {
    return Boolean.parseBoolean(PDEPEND_DEFAULT_ANALYZE_ONLY);
  }

  @Override
  protected boolean shouldRunDefault() {
    return Boolean.parseBoolean(PDEPEND_DEFAULT_SHOULD_RUN);
  }

  @Override
  protected String getCommandLine() {
    return PDEPEND_COMMAND_LINE;
  }

  public String getExcludePackages() {
    String[] values = getProject().getConfiguration().getStringArray(PDEPEND_EXCLUDE_PACKAGE_KEY);
    if (values != null && values.length > 0) {
      return StringUtils.join(values, ',');
    }
    return null;
  }

  public String getIgnoreDirs() {
    String[] values = getProject().getConfiguration().getStringArray(PDEPEND_IGNORE_KEY);
    if (values != null && values.length > 0) {
      return StringUtils.join(values, ',');
    }
    return null;
  }

  public boolean isBadDocumentation() {
    return getProject().getConfiguration().getBoolean(PDEPEND_BAD_DOCUMENTATION_KEY, Boolean.valueOf(PDEPEND_DEFAULT_BAD_DOCUMENTATION));
  }

  public boolean isWithoutAnnotation() {
    return getProject().getConfiguration().getBoolean(PDEPEND_WITHOUT_ANNOTATION_KEY, Boolean.valueOf(PDEPEND_DEFAULT_WITHOUT_ANNOTATION));
  }

  public final boolean shouldExecuteOnProject() {
    return isShouldRun() && Php.INSTANCE.equals(getProject().getLanguage());
  }

}
