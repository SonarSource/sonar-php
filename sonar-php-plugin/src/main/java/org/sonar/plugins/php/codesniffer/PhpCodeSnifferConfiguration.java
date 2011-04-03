/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Sonar PHP Plugin
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

package org.sonar.plugins.php.codesniffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.AbstractPhpPluginConfiguration;

/**
 * The Class PhpCheckstyleConfiguration.
 */
public class PhpCodeSnifferConfiguration extends AbstractPhpPluginConfiguration {

  public static final String PHPCS_DEFAULT_REPORT_FILE_NAME = "codesniffer.xml";
  public static final String PHPCS_DEFAULT_REPORT_FILE_PATH = "/logs";
  public static final String PHPCS_REPORT_FILE_NAME_PROPERTY_KEY = "sonar.phpCodesniffer.reportFileName";
  public static final String PHPCS_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY = "sonar.phpCodesniffer.reportFileRelativePath";
  public static final String PHPCS_ANALYZE_ONLY_KEY = "sonar.phpCodesniffer.analyzeOnly";
  public static final String PHPCS_SHOULD_RUN_KEY = "sonar.phpCodesniffer.shouldRun";
  public static final String PHPCS_DEFAULT_ANALYZE_ONLY = "false";
  public static final String PHPCS_DEFAULT_SHOULD_RUN = "true";
  public static final String PHPCS_SEVERITY_KEY = "sonar.phpCodesniffer.levelArgument";

  public static final String PHPCS_SEVERITY_OR_LEVEL_MODIFIER = "--severity=";
  public static final String PHPCS_SEVERITY_OR_LEVEL_MODIFIER_KEY = "sonar.phpCodesniffer.severity.modifier";

  public static final String PHPCS_STANDARD_ARGUMENT_KEY = "sonar.phpCodesniffer.standardArgument";
  public static final String PHPCS_DEFAULT_STANDARD_ARGUMENT = "Pear";
  public static final String PHPCS_STANDARD_MODIFIER = "--standard=";
  public static final String PHPCS_STANDARD_MESSAGE = "Ruleset (or standard) to run PHP_CodeSniffer with";
  public static final String PHPCS_STANDARD_DESCRIPTION = "The ruleset file (or the standard name) used to run PHP_CodeSniffer against. "
      + "If no one is specified all standards will be launched";

  public static final String PHPCS_REPORT_FILE_MODIFIER = "--report-file=";
  public static final String PHPCS_ARGUMENT_LINE_KEY = "sonar.phpCodesniffer.argumentLine";
  public static final String PHPCS_DEFAULT_ARGUMENT_LINE = " ";
  public static final String PHPCS_IGNORE_ARGUMENT_KEY = "sonar.phpCodesniffer.ignoreArgument";
  public static final String PHPCS_REPORT_MODIFIER = "--report=checkstyle";
  public static final String PHPCS_EXTENSIONS_MODIFIER = "--extensions=";
  public static final String PHPCS_IGNORE_MODIFIER = "--ignore=";

  public static final String PHPCS_ANALYZE_ONLY_MESSAGE = "Only analyze existing PHP_CodeSniffer violation files";
  public static final String PHPCS_ANALYZE_ONLY_DESCRIPTION = "If set to true the plugin will the plugin will only parse the result file."
      + " If set to false launch tool and parse result.";
  /** The Constant PDEPEND_COMMAND_LINE. */

  private static final String PHPCS_COMMAND_LINE = "phpcs";

  private static final String PHP_CODESNIFFER_TMP_RULESET_FILENAME = "phpcs-ruleset.xml";

  /**   */
  private Project project;

  /** Used to export the project current active profile and generate a tmp ruleset to pass to PHP_CodeSniffer. */
  private PhpCodeSnifferProfileExporter exporter;

  /**   */
  private RulesProfile profile;

  /** The rule finder. */
  private RuleFinder ruleFinder;

  /**
   * Instantiates a new php checkstyle configuration.
   * 
   * @param project
   *          the pom
   */
  public PhpCodeSnifferConfiguration(Project project, PhpCodeSnifferProfileExporter exporter, RulesProfile profile, RuleFinder ruleFinder) {
    super(project);
    this.project = project;
    this.exporter = exporter;
    this.profile = profile;
    this.ruleFinder = ruleFinder;
  }

  /**
   * @return The temporary rulest file passed to phpcs to run only sniff for the ruleset.
   */
  public File getRuleSet() {
    Writer writer = null;
    File xmlFile = new File(project.getFileSystem().getSonarWorkingDirectory(), PHP_CODESNIFFER_TMP_RULESET_FILENAME);
    try {
      writer = new OutputStreamWriter(new FileOutputStream(xmlFile, false), CharEncoding.UTF_8);
      exporter.exportProfile(profile, writer);
      writer.flush();
      return xmlFile;
    } catch (IOException e) {
      throw new SonarException("Fail to export temporary ruleset file to " + xmlFile.getPath(), e);
    } finally {
      IOUtils.closeQuietly(writer);
    }
  }

  /**
   * Gets the default report file name.
   * 
   * @return the default report file name
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getDefaultReportFileName()
   */
  @Override
  protected String getDefaultReportFileName() {
    return PHPCS_DEFAULT_REPORT_FILE_NAME;
  }

  /**
   * Gets the default report file path.
   * 
   * @return the default report file path
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getPhpunitDefaultReportFilePath()
   */
  @Override
  protected String getDefaultReportFilePath() {
    return PHPCS_DEFAULT_REPORT_FILE_PATH;
  }

  /**
   * Gets the report file name key.
   * 
   * @return the report file name key
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getReportFileNameKey()
   */
  @Override
  protected String getReportFileNameKey() {
    return PHPCS_REPORT_FILE_NAME_PROPERTY_KEY;
  }

  /**
   * Gets the report file relative path key.
   * 
   * @return the report file relative path key
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getReportFileRelativePathKey()
   */
  @Override
  protected String getReportFileRelativePathKey() {
    return PHPCS_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY;
  }

  /**
   * Gets the should analyse only key.
   * 
   * @return the should analyse only key
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getShouldAnalyzeOnlyKey()
   */
  @Override
  protected String getShouldAnalyzeOnlyKey() {
    return PHPCS_ANALYZE_ONLY_KEY;
  }

  /**
   * Gets the should run key.
   * 
   * @return the should run key
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getShouldRunKey()
   */
  @Override
  protected String getShouldRunKey() {
    return PHPCS_SHOULD_RUN_KEY;
  }

  /**
   * Should analyze only default.
   * 
   * @return true, if should analyze only default
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#shouldAnalyseOnlyDefault()
   */
  @Override
  protected boolean shouldAnalyzeOnlyDefault() {
    return Boolean.parseBoolean(PHPCS_DEFAULT_ANALYZE_ONLY);
  }

  /**
   * Should run default.
   * 
   * @return true, if should run default
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#shouldRunDefault()
   */
  @Override
  protected boolean shouldRunDefault() {
    return Boolean.parseBoolean(PHPCS_DEFAULT_SHOULD_RUN);
  }

  /**
   * Gets the argument line key.
   * 
   * @return the argument line key
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration #getPhpunitArgumentLineKey()
   */
  @Override
  protected String getArgumentLineKey() {
    return PHPCS_ARGUMENT_LINE_KEY;
  }

  /**
   * Gets the default argument line value.
   * 
   * @return the default argument line
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration #getPhpunitDefaultArgumentLine()
   */
  @Override
  protected String getDefaultArgumentLine() {
    return PHPCS_DEFAULT_ARGUMENT_LINE;
  }

  /**
   * Gets the external tool command line
   * 
   * @return the external tool command line
   * @see org.sonar.plugins.php.core.AbstractPhpPluginConfiguration#getCommandLine()
   */
  @Override
  protected String getCommandLine() {
    return PHPCS_COMMAND_LINE;
  }

  /**
   * Gets the level argument value.
   * 
   * @return the level
   */
  public String getLevel() {
    return getProject().getConfiguration().getString(PHPCS_SEVERITY_KEY);
  }

  /**
   * Gets the standard argument value.
   * 
   * @return the standard
   */
  public String getStandard() {
    return getProject().getConfiguration().getString(PHPCS_STANDARD_ARGUMENT_KEY, PHPCS_DEFAULT_STANDARD_ARGUMENT);
  }

  /**
   * Gets the ignore list argument value.
   * 
   * @return the ignore list
   */
  public List<String> getExclusionPatterns() {
    return Arrays.asList(getProject().getExclusionPatterns());
  }

  /**
   * @return
   */
  public String getSeverityModifier() {
    return getProject().getConfiguration().getString(PHPCS_SEVERITY_OR_LEVEL_MODIFIER_KEY, PHPCS_SEVERITY_OR_LEVEL_MODIFIER);
  }

  /**
   * @return the ruleFinder
   */
  public RuleFinder getRuleFinder() {
    return ruleFinder;
  }

  /**
   * @param ruleFinder
   *          the ruleFinder to set
   */
  public void setRuleFinder(RuleFinder finder) {
    this.ruleFinder = finder;
  }

  /**
   * @return the profile
   */
  public RulesProfile getProfile() {
    return profile;
  }

}
