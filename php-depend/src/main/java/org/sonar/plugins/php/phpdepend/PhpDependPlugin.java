/*
 * Sonar, open source software quality management tool. Copyright (C) 2009 SonarSource SA mailto:contact AT sonarsource DOT com
 * Sonar is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version. Sonar is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with Sonar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02
 */

package org.sonar.plugins.php.phpdepend;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.plugins.php.phpdepend.configuration.PhpDependConfiguration;
import org.sonar.plugins.php.phpdepend.sensor.PhpDependSensor;

/**
 * The Class PhpDependPlugin.
 */
@Properties( {
		@Property(key = PhpDependConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, defaultValue = PhpDependConfiguration.DEFAULT_REPORT_FILE_PATH, name = "PhpDepend log directory", description = "The relative path after project build path to the PhpDepend log directory.", project = true),
		@Property(key = PhpDependConfiguration.REPORT_FILE_NAME_PROPERTY_KEY, defaultValue = PhpDependConfiguration.DEFAULT_REPORT_FILE_NAME, name = "PhpDepend log file name", description = "The php depend log file name.", project = true),
		@Property(key = PhpDependConfiguration.IGNORE_KEY, defaultValue = PhpDependConfiguration.DEFAULT_IGNORE, name = "Directories that will be ignored in the analysis process.", description = "A list of comma separated folder name that will be excluded from analysis", project = true),
		@Property(key = PhpDependConfiguration.EXCLUDE_PACKAGE_KEY, defaultValue = PhpDependConfiguration.DEFAULT_EXCLUDE_PACKAGES, name = "Packages that will be excluded from the analysis process.", description = "A list of comma separated packages that will be excluded from analysis", project = true),
		@Property(key = PhpDependConfiguration.BAD_DOCUMENTATION_KEY, defaultValue = PhpDependConfiguration.DEFAULT_BAD_DOCUMENTATION, name = "The project documentation is clean.", description = "If set to true, documentation analysis will be skipped", project = true),
		@Property(key = PhpDependConfiguration.WITHOUT_ANNOTATION_KEY, defaultValue = PhpDependConfiguration.DEFAULT_WITHOUT_ANNOTATION, name = "Packages that will be excluded from the analysis process.", description = "A list of comma separated packages that will be excluded from analysis", project = true),
		@Property(key = PhpDependConfiguration.ARGUMENT_LINE_KEY, defaultValue = PhpDependConfiguration.DEFAULT_ARGUMENT_LINE, name = "The php depend argument line", description = "PhpCodeSniffer will be launched with this arguments", project = true),
		@Property(key = PhpDependConfiguration.ANALYZE_ONLY_PROPERTY_KEY, defaultValue = PhpDependConfiguration.DEFAULT_ANALYZE_ONLY, name = "Should the plugin only parse analyzis report.", description = "If set to true the plugin will the plugin will only parse the result file. If set to false launch tool and parse result.", project = true),
		@Property(key = PhpDependConfiguration.SHOULD_RUN_PROPERTY_KEY, defaultValue = PhpDependConfiguration.DEFAULT_SHOULD_RUN, name = "Should run the plugin", description = "If set to true the plugin will launch tool and parse result. If set to false the plugin will only parse the result file.", project = true) })
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
