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

package org.sonar.plugins.php.phpdepend.executor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.php.core.executor.PhpPluginAbstractExecutor;
import org.sonar.plugins.php.phpdepend.configuration.PhpDependConfiguration;

/**
 * The Class PhpDependExecutor.
 */
public class PhpDependExecutor extends PhpPluginAbstractExecutor {

  /**
   * 
   */
  private static final String PHPDEPEND_DIRECTORY_SEPARATOR = ",";
  /** The configuration. */
  private PhpDependConfiguration config;

  /**
   * Instantiates a new php depend executor.
   * 
   * @param configuration
   *          the configuration
   */
  public PhpDependExecutor(PhpDependConfiguration configuration) {
    this.config = configuration;
  }

  @Override
  protected List<String> getCommandLine() {
    List<String> result = new ArrayList<String>();
    result.add(config.getOsDependentToolScriptName());
    result.add(config.getReportFileCommandOption());
    result.add(config.getSuffixesCommandOption());
    if (config.isStringPropertySet(PhpDependConfiguration.EXCLUDE_PACKAGE_KEY)) {
      result.add(PhpDependConfiguration.EXCLUDE_OPTION + config.getExcludePackeges());
    }
    if (config.isStringPropertySet(PhpDependConfiguration.IGNORE_KEY)) {
      result.add(PhpDependConfiguration.IGNORE_OPTION + config.getIgnoreDirs());
    }
    if (config.isBadDocumentation()) {
      result.add(PhpDependConfiguration.BAD_DOCUMENTATION_OPTION);
    }
    if (config.isWithoutAnnotation()) {
      result.add(PhpDependConfiguration.WITHOUT_ANNOTATION_OPTION);
    }
    if (config.isStringPropertySet(PhpDependConfiguration.ARGUMENT_LINE_KEY)) {
      result.add(config.getArgumentLine());
    }
    // SONARPLUGINS-547 PhpDependExecutor: wrong dirs params
    result.add(StringUtils.join(config.getSourceDirectories(), PHPDEPEND_DIRECTORY_SEPARATOR));
    return result;
  }

  @Override
  protected String getExecutedTool() {
    return PhpDependConfiguration.COMMAND_LINE;
  }
}
