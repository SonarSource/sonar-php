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

package org.sonar.plugins.php.pmd;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.plugins.php.pmd.configuration.PhpPmdConfiguration;
import org.sonar.plugins.php.pmd.sensor.PhpPmdSensor;

/**
 * The Class PhpPmdPlugin class declares all extensions to be run for a project to be analyzed by the PHPMD tool.
 */
@Properties( {
    @Property(key = PhpPmdConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, 
        defaultValue = PhpPmdConfiguration.DEFAULT_REPORT_FILE_PATH, name = "PhpDepend log directory", 
        description = "The relative path to the PHPMD log directory.", project = true),
    @Property(key = PhpPmdConfiguration.REPORT_FILE_NAME_PROPERTY_KEY, defaultValue = PhpPmdConfiguration.DEFAULT_REPORT_FILE_NAME, 
        name = "PhpDepend log file name", description = "The PHPMD log file name.", project = true),
    @Property(key = PhpPmdConfiguration.RULESETS_ARGUMENT_KEY, defaultValue = PhpPmdConfiguration.DEFAULT_RULESET_ARGUMENT,
        name = "The phpmd ruleset argument line", description = "PHPMD will use given ruleset", project = true),
    @Property(key = PhpPmdConfiguration.LEVEL_ARGUMENT_KEY, defaultValue = PhpPmdConfiguration.DEFAULT_LEVEL_ARGUMENT, 
        name = "The phpmd level argument line", description = PhpPmdConfiguration.DEFAULT_LEVEL_DESCRIPTION, project = true),
    @Property(key = PhpPmdConfiguration.IGNORE_ARGUMENT_KEY, defaultValue = PhpPmdConfiguration.DEFAULT_IGNORE_ARGUMENT, 
        name = "The phpmd ignore argument line", description = "PHPMD will ignore the given folders (comma separated folder names)",
        project = true),
    @Property(key = PhpPmdConfiguration.ARGUMENT_LINE_KEY, defaultValue = PhpPmdConfiguration.DEFAULT_ARGUMENT_LINE, 
        name = "The phpmd other arguments line", description = "Given arguments will be used as other arguments for PHPMD", 
        project = true),
    @Property(key = PhpPmdConfiguration.ANALYZE_ONLY_KEY, defaultValue = PhpPmdConfiguration.DEFAULT_ANALYZE_ONLY, 
        name = "Should the plugin only parse analysis report.", 
        description = "If set to true the plugin will the plugin will only parse the result file. If set to false launch tool and parse result.", project = true),
    @Property(key = PhpPmdConfiguration.SHOULD_RUN_KEY, defaultValue = PhpPmdConfiguration.DEFAULT_SHOULD_RUN, 
        name = "Should the plugin run on this project.", description = "If set to false, the plugin will not execute itself for this project.", project = true) })
public class PhpPmdPlugin implements Plugin {

  /** The plugin KEY. */
  public static final String KEY = "PHP PMD";

  /**
   * Gets the description.
   * 
   * @return the description *
   * @see org.sonar.api.Plugin#getDescription()
   */
  public String getDescription() {
    return "A plugin to cover the PMD PHP";
  }

  /**
   * Gets the extensions.
   * 
   * @return the extensions
   * @see org.sonar.api.Plugin#getExtensions()
   */
  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
    extensions.add(PhpPmdSensor.class);
    extensions.add(PhpPmdRulesRepository.class);
    return extensions;
  }

  /**
   * Gets the key.
   * 
   * @return the key
   * @see org.sonar.api.Plugin#getKey()
   */
  public String getKey() {
    return KEY;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   * @see org.sonar.api.Plugin#getName()
   */
  public String getName() {
    return "PHPMD";
  }

  /**
   * To string.
   * 
   * @return the string
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getKey();
  }
}
