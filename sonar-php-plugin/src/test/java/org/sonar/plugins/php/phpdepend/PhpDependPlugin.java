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

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;

/**
 * The Class PhpDependPlugin.
 */
@Properties({
    @Property(key = PhpDependConfiguration.PDEPEND_REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY,
        defaultValue = PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH, name = "PhpDepend log directory",
        description = "The relative path after project build path to the PhpDepend log directory.", project = true),
    @Property(key = PhpDependConfiguration.PDEPEND_REPORT_FILE_NAME_PROPERTY_KEY, defaultValue = PhpDependConfiguration.PDEPEND_DEFAULT_REPORT_FILE_NAME,
        name = "PhpDepend log file name", description = "The php depend log file name.", project = true),
    @Property(key = PhpDependConfiguration.PDEPEND_IGNORE_KEY, defaultValue = PhpDependConfiguration.PDEPEND_DEFAULT_IGNORE,
        name = "Directories that will be ignored in the analysis process.",
        description = "A list of comma separated folder name that will be excluded from analysis", project = true),
    @Property(key = PhpDependConfiguration.PDEPEND_EXCLUDE_PACKAGE_KEY, defaultValue = PhpDependConfiguration.PDEPEND_DEFAULT_EXCLUDE_PACKAGES,
        name = "Packages that will be excluded from the analysis process.",
        description = "A list of comma separated packages that will be excluded from analysis", project = true),
    @Property(key = PhpDependConfiguration.PDEPEND_BAD_DOCUMENTATION_KEY, defaultValue = PhpDependConfiguration.PDEPEND_DEFAULT_BAD_DOCUMENTATION,
        name = "The project documentation is clean.", description = "If set to true, documentation analysis will be skipped",
        project = true),
    @Property(key = PhpDependConfiguration.PDEPEND_WITHOUT_ANNOTATION_KEY, defaultValue = PhpDependConfiguration.PDEPEND_DEFAULT_WITHOUT_ANNOTATION,
        name = "Packages that will be excluded from the analysis process.",
        description = "A list of comma separated packages that will be excluded from analysis", project = true),
    @Property(key = PhpDependConfiguration.PDEPEND_ARGUMENT_LINE_KEY, defaultValue = PhpDependConfiguration.PDEPEND_DEFAULT_ARGUMENT_LINE,
        name = "The php depend argument line", description = "PhpCodeSniffer will be launched with this arguments", project = true),
    @Property(key = PhpDependConfiguration.PDEPEND_ANALYZE_ONLY_PROPERTY_KEY, defaultValue = PhpDependConfiguration.PDEPEND_DEFAULT_ANALYZE_ONLY,
        name = "Should the plugin only parse analyzis report.", description = PhpDependConfiguration.PDEPEND_ANALYZE_ONLY_DESCRIPTION,
        project = true),
    @Property(key = PhpDependConfiguration.PDEPEND_SHOULD_RUN_PROPERTY_KEY, defaultValue = PhpDependConfiguration.PDEPEND_DEFAULT_SHOULD_RUN,
        name = "Should run the plugin", description = PhpDependConfiguration.PDEPEND_SHOULD_RUN_DESCRIPTION, project = true) })
public class PhpDependPlugin implements Plugin {

  /** The Plugin KEY. */
  private static final String KEY = "PHP DEPEND";

  /**
   * @see org.sonar.api.Plugin#getDescription()
   */
  public final String getDescription() {
    return "A plugin to cover the PHP DEPEND";
  }

  /**
   * @see org.sonar.api.Plugin#getExtensions()
   */
  public final List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
    extensions.add(PhpDependSensor.class);
    return extensions;
  }

  /**
   * @see org.sonar.api.Plugin#getKey()
   */
  public final String getKey() {
    return KEY;
  }

  /**
   * @see org.sonar.api.Plugin#getName()
   */
  public final String getName() {
    return KEY;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public final String toString() {
    return getKey();
  }
}
