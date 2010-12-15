/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 EchoSource
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

package org.sonar.plugins.php.pmd;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.php.core.Php;
import org.sonar.plugins.php.core.PhpPluginAbstractExecutor;

/**
 * The Class PhpCheckstyleExecutor.
 */
public class PhpmdExecutor extends PhpPluginAbstractExecutor {

  private static final String PHPMD_PATH_SEPARATOR = ",";
  /** The config. */
  private PhpmdConfiguration config;

  /**
   * Instantiates a new php checkstyle executor.
   * 
   * @param configuration
   *          the configuration
   */
  public PhpmdExecutor(PhpmdConfiguration configuration) {
    this.config = configuration;
  }

  /**
   * Gets a complete command line with configured arguments
   * 
   * @return the command line
   * 
   * @see org.sonar.plugins.php.core.PhpPluginAbstractExecutor#getCommandLine()
   */
  @Override
  protected List<String> getCommandLine() {
    List<String> result = new ArrayList<String>();
    result.add(config.getOsDependentToolScriptName());

    // SONARPLUGINS-546 PhpmdExecutor: wrong dirs params
    result.add(StringUtils.join(config.getSourceDirectories(), PHPMD_PATH_SEPARATOR));

    result.add(PhpmdConfiguration.PHPMD_REPORT_FORMAT);
    result.add(config.getRulesets());
    result.add(PhpmdConfiguration.PHPMD_REPORT_FILE_OPTION);
    result.add(config.getReportFile().getAbsolutePath());
    // result.add(PhpmdConfiguration.PHPMD_LEVEL_OPTION);
    // result.add(config.getLevel());
    if (config.isStringPropertySet(PhpmdConfiguration.PHPMD_IGNORE_ARGUMENT_KEY)) {
      result.add(PhpmdConfiguration.PHPMD_IGNORE_OPTION);
      result.add(config.getIgnoreList());
    }
    result.add(PhpmdConfiguration.PHPMD_EXTENSIONS_OPTION);
    result.add(StringUtils.join(Php.INSTANCE.getFileSuffixes(), ","));
    if (config.isStringPropertySet(PhpmdConfiguration.PHPMD_ARGUMENT_LINE_KEY)) {
      result.add(config.getArgumentLine());
    }
    return result;
  }

  /**
   * Gets the executed tool.
   * 
   * @return the executed tool
   * 
   * @see org.sonar.plugins.php.core.PhpPluginAbstractExecutor#getExecutedTool()
   */
  @Override
  protected String getExecutedTool() {
    return "PHPMD";
  }
}
