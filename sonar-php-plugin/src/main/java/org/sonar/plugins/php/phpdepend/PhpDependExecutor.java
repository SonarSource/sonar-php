/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2010 Akram Ben Aissi
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

package org.sonar.plugins.php.phpdepend;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.php.core.PhpPluginAbstractExecutor;

/**
 * The Class PhpDependExecutor.
 */
public class PhpDependExecutor extends PhpPluginAbstractExecutor {

  /**
   * 
   */
  private static final String PHPDEPEND_DIRECTORY_SEPARATOR = ",";
  /** The configuration. */
  private PhpDependConfiguration configuration;

  /**
   * Instantiates a new php depend executor.
   * 
   * @param configuration
   *          the configuration
   */
  public PhpDependExecutor(PhpDependConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected List<String> getCommandLine() {
    List<String> result = new ArrayList<String>();
    result.add(configuration.getOsDependentToolScriptName());
    result.add(configuration.getReportFileCommandOption());
    result.add(configuration.getSuffixesCommandOption());
    if (configuration.isStringPropertySet(PhpDependConfiguration.PDEPEND_EXCLUDE_PACKAGE_KEY)) {
      result.add(PhpDependConfiguration.PDEPEND_EXCLUDE_OPTION + configuration.getExcludePackeges());
    }
    if (configuration.isStringPropertySet(PhpDependConfiguration.PDEPEND_IGNORE_KEY)) {
      result.add(PhpDependConfiguration.PDEPEND_IGNORE_OPTION + configuration.getIgnoreDirs());
    }
    if (configuration.isBadDocumentation()) {
      result.add(PhpDependConfiguration.PDEPEND_BAD_DOCUMENTATION_OPTION);
    }
    if (configuration.isWithoutAnnotation()) {
      result.add(PhpDependConfiguration.PDEPEND_WITHOUT_ANNOTATION_OPTION);
    }
    if (configuration.isStringPropertySet(PhpDependConfiguration.PDEPEND_ARGUMENT_LINE_KEY)) {
      result.add(configuration.getArgumentLine());
    }
    // SONARPLUGINS-547 PhpDependExecutor: wrong dirs params
    result.add(StringUtils.join(configuration.getSourceDirectories(), PHPDEPEND_DIRECTORY_SEPARATOR));
    return result;
  }

  @Override
  protected String getExecutedTool() {
    return PhpDependConfiguration.PDEPEND_COMMAND_LINE;
  }

  /**
   * @return the configuration
   */
  public PhpDependConfiguration getConfiguration() {
    return configuration;
  }
  
  
  
}
