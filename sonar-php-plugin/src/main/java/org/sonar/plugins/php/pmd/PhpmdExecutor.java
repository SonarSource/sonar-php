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
package org.sonar.plugins.php.pmd;

import static org.sonar.plugins.php.api.Php.PHP;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_ARGUMENT_LINE_KEY;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_EXTENSIONS_OPTION;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FILE_OPTION;
import static org.sonar.plugins.php.pmd.PhpmdConfiguration.PHPMD_REPORT_FORMAT;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.plugins.php.core.AbstractPhpExecutor;

import com.google.common.collect.Lists;

/**
 * The Class PhpCheckstyleExecutor.
 */
public class PhpmdExecutor extends AbstractPhpExecutor {

  private static final String PHPMD_PATH_SEPARATOR = ",";
  private PhpmdConfiguration configuration;
  private PhpmdProfileExporter exporter;
  private RulesProfile profile;

  /**
   * https://github.com/manuelpichler/phpmd/blob/master/src/main/php/PHP/PMD/TextUI/Command.php <br/>
   * '2' means there are violations (=> but the process has completed)
   */
  private static final Collection<Integer> ACCEPTED_EXIT_CODES = Lists.newArrayList(0, 2);

  /**
   * Instantiates a new php checkstyle executor.
   * 
   * @param configuration
   *          the configuration
   */
  public PhpmdExecutor(PhpmdConfiguration configuration, PhpmdProfileExporter exporter, RulesProfile profile) {
    // PHPMD has 1 specific acceptable exit code ('2'), so we must pass this on the constructor
    super(configuration, ACCEPTED_EXIT_CODES);
    this.configuration = configuration;
    PHP.setConfiguration(configuration.getProject().getConfiguration());
    this.exporter = exporter;
    this.profile = profile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected List<String> getCommandLine() {
    List<String> result = new ArrayList<String>();
    result.add(configuration.getOsDependentToolScriptName());

    // SONARPLUGINS-546 PhpmdExecutor: wrong dirs params
    result.add(StringUtils.join(configuration.getSourceDirectories(), PHPMD_PATH_SEPARATOR));

    result.add(PHPMD_REPORT_FORMAT);
    File ruleset = getRuleset(configuration, profile, exporter);
    if (ruleset != null) {
      result.add(ruleset.getAbsolutePath());
    } else {
      result.add(configuration.getRulesets());
    }
    result.add(PHPMD_REPORT_FILE_OPTION);
    result.add(configuration.getReportFile().getAbsolutePath());

    result.add(PHPMD_EXTENSIONS_OPTION);
    result.add(StringUtils.join(PHP.getFileSuffixes(), ","));
    if (configuration.isStringPropertySet(PHPMD_ARGUMENT_LINE_KEY)) {
      result.addAll(Lists.newArrayList(StringUtils.split(configuration.getArgumentLine(), ' ')));
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getExecutedTool() {
    return "PHPMD";
  }
}
