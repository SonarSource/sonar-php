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

import static org.sonar.plugins.php.api.Php.PHP;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_BAD_DOCUMENTATION_OPTION;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_EXCLUDE_OPTION;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_EXCLUDE_PACKAGE_KEY;
import static org.sonar.plugins.php.phpdepend.PhpDependConfiguration.PDEPEND_WITHOUT_ANNOTATION_OPTION;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.php.core.AbstractPhpExecutor;

import com.google.common.collect.Lists;

/**
 * The Class PhpDependExecutor.
 */
public class PhpDependExecutor extends AbstractPhpExecutor {

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
    // PHPDepend has no specific valid exit code (only the standard '0'), so we can use the default constructor
    // see https://github.com/pdepend/pdepend/blob/master/src/main/php/PHP/Depend/TextUI/Runner.php
    super(configuration);
    this.configuration = configuration;
    PHP.setConfiguration(configuration.getProject().getConfiguration());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected List<String> getCommandLine() {
    List<String> result = new ArrayList<String>();
    result.add(configuration.getOsDependentToolScriptName());
    result.add(configuration.getReportFileCommandOption());
    result.add(configuration.getSuffixesCommandOption());
    if (configuration.isStringPropertySet(PDEPEND_EXCLUDE_PACKAGE_KEY)) {
      result.add(PDEPEND_EXCLUDE_OPTION + configuration.getExcludePackages());
    }

    if (configuration.isBadDocumentation()) {
      result.add(PDEPEND_BAD_DOCUMENTATION_OPTION);
    }
    if (configuration.isWithoutAnnotation()) {
      result.add(PDEPEND_WITHOUT_ANNOTATION_OPTION);
    }
    if (configuration.isStringPropertySet(PDEPEND_ARGUMENT_LINE_KEY)) {
      result.addAll(Lists.newArrayList(StringUtils.split(configuration.getArgumentLine(), ' ')));
    }
    // SONARPLUGINS-547 PhpDependExecutor: wrong dirs params
    result.add(StringUtils.join(configuration.getSourceDirectories(), PHPDEPEND_DIRECTORY_SEPARATOR));
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getExecutedTool() {
    return "PHP Depend";
  }

}
