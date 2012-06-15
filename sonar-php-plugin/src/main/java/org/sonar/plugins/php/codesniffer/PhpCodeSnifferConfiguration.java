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
package org.sonar.plugins.php.codesniffer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.php.core.AbstractPhpConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * The Class PhpCheckstyleConfiguration.
 */
public class PhpCodeSnifferConfiguration extends AbstractPhpConfiguration {

  private static final String PHPCS_COMMAND_LINE = "phpcs";
  private static final String PHP_CODESNIFFER_TMP_RULESET_FILENAME = "phpcs-ruleset.xml";

  // -- CodeSniffer tool options ---
  public static final String PHPCS_REPORT_MODIFIER = "--report=checkstyle";
  public static final String PHPCS_REPORT_FILE_MODIFIER = "--report-file=";
  public static final String PHPCS_STANDARD_MODIFIER = "--standard=";
  public static final String PHPCS_SEVERITY_OR_LEVEL_MODIFIER = "--severity=";
  public static final String PHPCS_EXTENSIONS_MODIFIER = "--extensions=";

  // --- Sonar config parameters ---
  public static final String PHPCS_SKIP_KEY = "sonar.phpCodesniffer.skip";
  public static final String PHPCS_SHOULD_RUN_KEY = "sonar.phpCodesniffer.shouldRun"; // OLD param that will be removed soon
  public static final String PHPCS_ANALYZE_ONLY_KEY = "sonar.phpCodesniffer.analyzeOnly";
  public static final String PHPCS_REPORT_FILE_RELATIVE_PATH_KEY = "sonar.phpCodesniffer.reportFileRelativePath";
  public static final String PHPCS_REPORT_FILE_RELATIVE_PATH_DEFVALUE = "/logs";
  public static final String PHPCS_REPORT_FILE_NAME_KEY = "sonar.phpCodesniffer.reportFileName";
  public static final String PHPCS_REPORT_FILE_NAME_DEFVALUE = "codesniffer.xml";
  public static final String PHPCS_STANDARD_ARGUMENT_KEY = "sonar.phpCodesniffer.standardArgument";
  public static final String PHPCS_SEVERITY_OR_LEVEL_MODIFIER_KEY = "sonar.phpCodesniffer.severity.modifier";
  public static final String PHPCS_SEVERITY_KEY = "sonar.phpCodesniffer.levelArgument";
  public static final String PHPCS_ARGUMENT_LINE_KEY = "sonar.phpCodesniffer.argumentLine";
  public static final String PHPCS_TIMEOUT_KEY = "sonar.phpCodesniffer.timeout";

  private PhpCodeSnifferProfileExporter exporter;
  private RulesProfile profile;

  /**
   * Instantiates a new php checkstyle configuration.
   * 
   * @param project
   *          the pom
   */
  public PhpCodeSnifferConfiguration(Settings settings, Project project, PhpCodeSnifferProfileExporter exporter, RulesProfile profile) {
    super(settings, project);
    this.exporter = exporter;
    this.profile = profile;
  }

  /**
   * Gets the level argument value.
   * 
   * @return the level
   */
  public String getLevel() {
    return getSettings().getString(PHPCS_SEVERITY_KEY);
  }

  /**
   * Gets the standard argument value.
   * 
   * @return the standard
   */
  public String getStandard() {
    return getSettings().getString(PHPCS_STANDARD_ARGUMENT_KEY);
  }

  /**
   * @return
   */
  public String getSeverityModifier() {
    return getSettings().getString(PHPCS_SEVERITY_OR_LEVEL_MODIFIER_KEY);
  }

  /**
   * @return the profile
   */
  public RulesProfile getProfile() {
    return profile;
  }

  /**
   * @return The temporary rulest file passed to phpcs to run only sniff for the ruleset.
   */
  public File getRuleSet() {
    Writer writer = null;
    File xmlFile = new File(getProject().getFileSystem().getSonarWorkingDirectory(), PHP_CODESNIFFER_TMP_RULESET_FILENAME);
    try {
      writer = new OutputStreamWriter(new FileOutputStream(xmlFile, false), CharEncoding.UTF_8);
      exporter.exportProfile(profile, writer);
      writer.flush();
      return xmlFile;
    } catch (IOException e) {
      throw new SonarException("Fail to export temporary ruleset file to " + xmlFile.getAbsolutePath(), e);
    } finally {
      IOUtils.closeQuietly(writer);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getReportFileNameKey() {
    return PHPCS_REPORT_FILE_NAME_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getReportFileRelativePathKey() {
    return PHPCS_REPORT_FILE_RELATIVE_PATH_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getShouldAnalyzeOnlyKey() {
    return PHPCS_ANALYZE_ONLY_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getSkipKey() {
    return PHPCS_SKIP_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getShouldRunKey() {
    return PHPCS_SHOULD_RUN_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getArgumentLineKey() {
    return PHPCS_ARGUMENT_LINE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getCommandLine() {
    return PHPCS_COMMAND_LINE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getTimeoutKey() {
    return PHPCS_TIMEOUT_KEY;
  }

}
