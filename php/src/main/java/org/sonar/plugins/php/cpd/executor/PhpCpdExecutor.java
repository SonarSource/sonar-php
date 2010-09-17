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

package org.sonar.plugins.php.cpd.executor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor;
import org.sonar.plugins.php.cpd.configuration.PhpCpdConfiguration;

/**
 * The Class PhpDependExecutor.
 */
public class PhpCpdExecutor extends PhpPluginAbstractExecutor {

  /**
   * 
   */

  /** The configuration. */
  private PhpCpdConfiguration config;

  /**
   * Instantiates a new php depend executor.
   * 
   * @param configuration
   *          the configuration
   */
  public PhpCpdExecutor(PhpCpdConfiguration configuration) {
    this.config = configuration;
  }

  /**
   * Return the command line depending configuration and arguments.
   * 
   * @see org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor#getCommandLine()
   */
  @Override
  protected List<String> getCommandLine() {
    List<String> result = new ArrayList<String>();
    result.add(config.getOsDependentToolScriptName());
    result.add(PhpCpdConfiguration.REPORT_FILE_OPTION);
    result.add(config.getReportFile().getAbsolutePath());

    String suffixes = config.getSuffixesCommandOption();
    if (StringUtils.isNotBlank(suffixes)) {
      result.add(PhpCpdConfiguration.SUFFIXES);
      result.add(suffixes);
    }

    if (config.isStringPropertySet(PhpCpdConfiguration.EXCLUDE_PACKAGE_KEY)) {
      result.add(PhpCpdConfiguration.EXCLUDE_OPTION + config.getExcludePackages());
    }
    result.add(StringUtils.join(config.getSourceDirectories(), PhpCpdConfiguration.DIRECTORY_SEPARATOR));
    return result;
  }

  @Override
  protected String getExecutedTool() {
    return PhpCpdConfiguration.COMMAND_LINE;
  }
}
