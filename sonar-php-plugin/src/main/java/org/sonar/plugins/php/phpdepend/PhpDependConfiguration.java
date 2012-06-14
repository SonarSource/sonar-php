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
package org.sonar.plugins.php.phpdepend;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.php.api.Php;
import org.sonar.plugins.php.core.AbstractPhpConfiguration;

/**
 * The php-depend plugin configuration class.
 */
public class PhpDependConfiguration extends AbstractPhpConfiguration {

  public static final String PDEPEND_COMMAND_LINE = "pdepend";

  // -- PHPDepend tool options ---
  public static final String PDEPEND_SUFFIXES_OPT = "--suffix";
  public static final String PDEPEND_WITHOUT_ANNOTATION_OPTION = "--without-annotations";
  public static final String PDEPEND_BAD_DOCUMENTATION_OPTION = "--bad-documentation";
  public static final String PDEPEND_EXCLUDE_OPTION = "--exclude=";

  // --- Sonar config parameters ---
  public static final String PDEPEND_SKIP_KEY = "sonar.phpDepend.skip";
  public static final String PDEPEND_SHOULD_RUN_PROPERTY_KEY = "sonar.phpDepend.shouldRun"; // OLD param that will be removed soon
  public static final String PDEPEND_ANALYZE_ONLY_KEY = "sonar.phpDepend.analyzeOnly";
  public static final String PDEPEND_REPORT_FILE_RELATIVE_PATH_KEY = "sonar.phpDepend.reportFileRelativePath";
  public static final String PDEPEND_REPORT_FILE_RELATIVE_PATH_DEFVALUE = "/logs";
  public static final String PDEPEND_REPORT_TYPE = "sonar.phpDepend.reportType";
  public static final String PDEPEND_REPORT_TYPE_DEFVALUE = "phpunit-xml";
  public static final String PDEPEND_REPORT_TYPE_PHPUNIT = "phpunit-xml";
  public static final String PDEPEND_REPORT_TYPE_SUMMARY = "summary-xml";
  public static final String PDEPEND_REPORT_FILE_NAME_KEY = "sonar.phpDepend.reportFileName";
  public static final String PDEPEND_REPORT_FILE_NAME_DEFVALUE = "pdepend.xml";

  public static final String PDEPEND_WITHOUT_ANNOTATION_KEY = "sonar.phpDepend.withoutAnnotations";
  public static final String PDEPEND_WITHOUT_ANNOTATION_DEFVALUE = "false";
  public static final String PDEPEND_BAD_DOCUMENTATION_KEY = "sonar.phpDepend.badDocumentation";
  public static final String PDEPEND_BAD_DOCUMENTATION_DEFVALUE = "false";
  public static final String PDEPEND_EXCLUDE_PACKAGE_KEY = "sonar.phpDepend.exclude";
  public static final String PDEPEND_ARGUMENT_LINE_KEY = "sonar.phpDepend.argumentLine";
  public static final String PDEPEND_TIMEOUT_KEY = "sonar.phpDepend.timeout";

  /**
   * Instantiates a new php depend configuration depending on given project.
   * 
   * @param project
   *          the project to be analyzed
   */
  public PhpDependConfiguration(Settings settings, Project project) {
    super(settings, project);
  }

  /**
   * Gets the report filecommand option.
   * 
   * @return the report filecommand option
   */
  public String getReportFileCommandOption() {
    String reportType = getReportType();
    if (reportType.equals(PDEPEND_REPORT_TYPE_PHPUNIT) || reportType.equals(PDEPEND_REPORT_TYPE_SUMMARY)) {
      return "--" + reportType + "=" + getReportFile().getAbsolutePath();
    } else {
      throw new IllegalArgumentException("Invalid PHP Depend report type: " + reportType + ". Supported types: phpunit-xml, summary-xml");
    }
  }

  public String getReportType() {
    return getSettings().getString(PDEPEND_REPORT_TYPE);
  }

  public String getExcludePackages() {
    return getSettings().getString(PDEPEND_EXCLUDE_PACKAGE_KEY);
  }

  public boolean isBadDocumentation() {
    return getSettings().getBoolean(PDEPEND_BAD_DOCUMENTATION_KEY);
  }

  public boolean isWithoutAnnotation() {
    return getSettings().getBoolean(PDEPEND_WITHOUT_ANNOTATION_KEY);
  }

  /**
   * Gets the suffixes command option.
   * 
   * @return the suffixes command option
   */
  public String getSuffixesCommandOption(Php php) {
    return PDEPEND_SUFFIXES_OPT + "=" + StringUtils.join(php.getFileSuffixes(), ",");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getArgumentLineKey() {
    return PDEPEND_ARGUMENT_LINE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getReportFileNameKey() {
    return PDEPEND_REPORT_FILE_NAME_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getReportFileRelativePathKey() {
    return PDEPEND_REPORT_FILE_RELATIVE_PATH_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getShouldAnalyzeOnlyKey() {
    return PDEPEND_ANALYZE_ONLY_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getShouldRunKey() {
    return PDEPEND_SHOULD_RUN_PROPERTY_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getSkipKey() {
    return PDEPEND_SKIP_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getCommandLine() {
    return PDEPEND_COMMAND_LINE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getTimeoutKey() {
    return PDEPEND_TIMEOUT_KEY;
  }

}
