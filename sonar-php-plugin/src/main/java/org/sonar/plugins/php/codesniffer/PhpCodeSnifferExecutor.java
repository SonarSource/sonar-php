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

package org.sonar.plugins.php.codesniffer;

import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_EXTENSIONS_MODIFIER;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_IGNORE_ARGUMENT_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_IGNORE_MODIFIER;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_REPORT_FILE_MODIFIER;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_REPORT_MODIFIER;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_STANDARD_ARGUMENT_KEY;
import static org.sonar.plugins.php.codesniffer.PhpCodeSnifferConfiguration.PHPCS_STANDARD_MODIFIER;
import static org.sonar.plugins.php.core.Php.PHP;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.plugins.php.core.PhpPluginAbstractExecutor;

/**
 * The Class PhpCheckstyleExecutor.
 */
public class PhpCodeSnifferExecutor extends PhpPluginAbstractExecutor {

  private static final String EXCLUSION_PATTERN_SEPARATOR = ",";

  /** The logger. */
  private static final Logger LOG = LoggerFactory.getLogger(PhpCodeSnifferExecutor.class);

  /** The PhpCodeSnifferConfiguration. */
  private PhpCodeSnifferConfiguration configuration;

  private PhpCodeSnifferProfileExporter exporter;
  private RulesProfile profile;

  /**
   * Instantiates a new php codesniffer executor.
   * 
   * @param configuration
   *          the configuration
   */
  public PhpCodeSnifferExecutor(PhpCodeSnifferConfiguration configuration, PhpCodeSnifferProfileExporter exporter, RulesProfile profile) {
    this.configuration = configuration;
    PHP.setConfiguration(configuration.getProject().getConfiguration());
    this.exporter = exporter;
    this.profile = profile;
  }

  /**
   * @see org.sonar.plugins.php.core.PhpPluginAbstractExecutor#getCommandLine()
   */
  @Override
  protected List<String> getCommandLine() {
    List<String> result = new ArrayList<String>();
    result.add(configuration.getOsDependentToolScriptName());
    result.add(PHPCS_REPORT_FILE_MODIFIER + configuration.getReportFile());
    result.add(PHPCS_REPORT_MODIFIER);

    // default level is no level, but can be overriden if set.
    if (configuration.isStringPropertySet(PhpCodeSnifferConfiguration.PHPCS_SEVERITY_KEY)) {
      String severityOption = configuration.getSeverityModifier();
      result.add(severityOption + configuration.getLevel());
    }
    // default standard is no standard (but maybe Pear is taken)
    // TODO use a generated ruleset from the current profile and pass it to standard.
    if (configuration.isStringPropertySet(PHPCS_STANDARD_ARGUMENT_KEY)) {
      result.add(PHPCS_STANDARD_MODIFIER + configuration.getStandard());
    } else {
      File ruleset = getRuleset(configuration, profile, exporter);
      if (ruleset != null) {
        result.add(PHPCS_STANDARD_MODIFIER + ruleset.getAbsolutePath());
      }
    }

    // result.add(PhpCodeSnifferConfiguration.PHPCS_STANDARD_MODIFIER + configuration.getRuleSet().getAbsolutePath());

    List<String> exclusionPatterns = configuration.getExclusionPatterns();
    boolean exclusionPatternsNotEmpty = exclusionPatterns != null && !exclusionPatterns.isEmpty();
    if (configuration.isStringPropertySet(PHPCS_IGNORE_ARGUMENT_KEY) && exclusionPatternsNotEmpty) {
      String ignorePatterns = StringUtils.join(exclusionPatterns, EXCLUSION_PATTERN_SEPARATOR);
      StringBuilder sb = new StringBuilder(PHPCS_IGNORE_MODIFIER).append(ignorePatterns);
      result.add(sb.toString());
    }

    if (configuration.isStringPropertySet(PHPCS_ARGUMENT_LINE_KEY)) {
      result.add(PHPCS_IGNORE_MODIFIER + configuration.getArgumentLine());
    }
    result.add(PHPCS_EXTENSIONS_MODIFIER + StringUtils.join(PHP.getFileSuffixes(), EXCLUSION_PATTERN_SEPARATOR));
    // Do not use the StringUtils.join() method here, because all the path will be treated as a single one
    for (File file : configuration.getSourceDirectories()) {
      result.add(file.getAbsolutePath());
    }
    LOG.debug("Command line " + result);
    return result;
  }

  /**
   * @see org.sonar.plugins.php.core.PhpPluginAbstractExecutor#getExecutedTool()
   */
  @Override
  protected String getExecutedTool() {
    return "PHPCodeSniffer";
  }

  /**
   * @return the configuration
   */
  public PhpCodeSnifferConfiguration getConfiguration() {
    return configuration;
  }

}
