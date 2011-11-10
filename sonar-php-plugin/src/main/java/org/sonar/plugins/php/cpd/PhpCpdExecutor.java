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
package org.sonar.plugins.php.cpd;

import static org.sonar.plugins.php.api.Php.PHP;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_EXCLUDE_OPTION;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_EXCLUDE_PACKAGE_KEY;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_LINES_MODIFIER;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_MINIMUM_NUMBER_OF_IDENTICAL_TOKENS_MODIFIER;
import static org.sonar.plugins.php.cpd.PhpCpdConfiguration.PHPCPD_REPORT_FILE_OPTION;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.php.core.AbstractPhpExecutor;

/**
 * The Class PhpDependExecutor.
 */
public class PhpCpdExecutor extends AbstractPhpExecutor {

  private static final String PHPCPD_DIRECTORY_SEPARATOR = " ";

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
    PHP.setConfiguration(configuration.getProject().getConfiguration());
  }

  /**
   * {@inheritDoc}
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
      String[] excludedPackages = configuration.getExcludePackages();

      if (excludedPackages != null) {
        for (String excludedPackage : excludedPackages) {
          for (File sourceDir : configuration.getProject().getFileSystem().getSourceDirs()) {
            result.add(PHPCPD_EXCLUDE_OPTION);
            result.add(new File(sourceDir, excludedPackage).getAbsolutePath());
          }
        }
      }

    }
    List<File> sourceDirectories = configuration.getSourceDirectories();
    if (sourceDirectories != null && !sourceDirectories.isEmpty()) {
      result.add(StringUtils.join(sourceDirectories, PHPCPD_DIRECTORY_SEPARATOR));
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getExecutedTool() {
    return configuration.getCommandLine();
  }

  /**
   * @return the configuration
   */
  public PhpCpdConfiguration getConfiguration() {
    return configuration;
  }

}
