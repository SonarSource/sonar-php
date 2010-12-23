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

package org.sonar.plugins.php.cpd;

import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_COMMAND_LINE;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_DIRECTORY_SEPARATOR;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_EXCLUDE_OPTION;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_EXCLUDE_PACKAGE_KEY;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_LINES_MODIFIER;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS_MODIFIER;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_REPORT_FILE_OPTION;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.php.core.PhpPluginAbstractExecutor;

/**
 * The Class PhpDependExecutor.
 */
public class PhpCpdExecutor extends PhpPluginAbstractExecutor {

  /**
   * 
   */

  /** The configuration. */
  private PhpCpdConfiguration configuration;

  /**
   * Instantiates a new php depend executor.
   * 
   * @param configuration
   *          the configuration
   */
  public PhpCpdExecutor(PhpCpdConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Return the command line depending configuration and arguments.
   * 
   * @see org.sonar.plugins.php.core.PhpPluginAbstractExecutor#getCommandLine()
   */
  @Override
  protected List<String> getCommandLine() {
    List<String> result = new ArrayList<String>();
    result.add(configuration.getOsDependentToolScriptName());

    result.add(PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_LINES_MODIFIER);
    result.add(configuration.getMinimunNumberOfIdenticalLines());

    result.add(PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS_MODIFIER);
    result.add(configuration.getMinimunNumberOfIdenticalTokens());

    result.add(PHPCPD_REPORT_FILE_OPTION);
    result.add(configuration.getReportFile().getPath());

    String suffixes = configuration.getSuffixesCommandOption();
    if (StringUtils.isNotBlank(suffixes)) {
      result.add(PhpCpdConfiguration.PHPCPD_SUFFIXES);
      result.add(suffixes);
    }

    if (configuration.isStringPropertySet(PHPCPD_EXCLUDE_PACKAGE_KEY)) {
      result.add(PHPCPD_EXCLUDE_OPTION + configuration.getExcludePackages());
    }
    List<File> sourceDirectories = configuration.getSourceDirectories();
    if (sourceDirectories != null && !sourceDirectories.isEmpty()) {
      result.add(StringUtils.join(sourceDirectories, PHPCPD_DIRECTORY_SEPARATOR));
    }
    return result;
  }

  @Override
  protected String getExecutedTool() {
    return PHPCPD_COMMAND_LINE;
  }

}
