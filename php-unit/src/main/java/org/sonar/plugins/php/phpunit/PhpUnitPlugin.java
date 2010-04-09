/*
 * Sonar, open source software quality management tool. Copyright (C) 2009 SonarSource SA mailto:contact AT sonarsource DOT com
 * Sonar is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version. Sonar is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with Sonar; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02
 */

package org.sonar.plugins.php.phpunit;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.plugins.php.phpunit.configuration.PhpUnitConfiguration;
import org.sonar.plugins.php.phpunit.sensor.PhpUnitSensor;

/**
 * The PhpUnitPlugin handles files and metrics provided by punit tool.
 * 
 * @author jtama
 */
@Properties( {
		@Property(key = PhpUnitConfiguration.MAIN_TEST_CLASS_PROPERTY_KEY, defaultValue = PhpUnitConfiguration.DEFAULT_MAIN_TEST_CLASS, name = "Project main test class", description = "The project main test class including the relativ path ie : \"/source/tests/AllTests.php\"", project = true),
		@Property(key = PhpUnitConfiguration.REPORT_FILE_RELATIVE_PATH_PROPERTY_KEY, defaultValue = PhpUnitConfiguration.DEFAULT_REPORT_FILE_PATH, name = "PHPUnit log directory", description = "The relative path to the PHPUnit log directory beginning after {PROJECT_BUILD_PATH}.", project = true),
		@Property(key = PhpUnitConfiguration.REPORT_FILE_NAME_PROPERTY_KEY, defaultValue = PhpUnitConfiguration.DEFAULT_REPORT_FILE_NAME, name = "PhpUnit log file name", description = "The php unit log file name.", project = true),
		@Property(key = PhpUnitConfiguration.COVERAGE_REPORT_FILE_PROPERTY_KEY, defaultValue = PhpUnitConfiguration.DEFAULT_COVERAGE_REPORT_FILE, name = "PhpUnit coverage log file name", description = "The php unit coverage log file name.", project = true),
		@Property(key = PhpUnitConfiguration.FILTER_PROPERTY_KEY, defaultValue = PhpUnitConfiguration.DEFAULT_FILTER, name = "The phpunit filter arguments line", description = "Given arguments will be used as filters arguments for PHPUnit", project = true),
		@Property(key = PhpUnitConfiguration.BOOTSTRAP_PROPERTY_KEY, defaultValue = PhpUnitConfiguration.DEFAULT_BOOTSTRAP, name = "The phpunit bootstrap arguments line", description = "Given arguments will be used to set bootstrap for PHPUnit", project = true),
		@Property(key = PhpUnitConfiguration.CONFIGURATION_PROPERTY_KEY, defaultValue = PhpUnitConfiguration.DEFAULT_CONFIGURATION, name = "The phpunit configuration arguments line", description = "Given arguments will be used as configuration arguments for PHPUnit", project = true),
		@Property(key = PhpUnitConfiguration.LOADER_PROPERTY_KEY, defaultValue = PhpUnitConfiguration.DEFAULT_LOADER, name = "The phpunit loader arguments line", description = "Given arguments will be used as other loader for PHPUnit", project = true),
		@Property(key = PhpUnitConfiguration.ARGUMENT_LINE_KEY, defaultValue = PhpUnitConfiguration.DEFAULT_ARGUMENT_LINE, name = "The phpunit other arguments line", description = "Given arguments will be used as other arguments for PHPUnit", project = true),
		@Property(key = PhpUnitConfiguration.SHOULD_RUN_PROPERTY_KEY, defaultValue = PhpUnitConfiguration.DEFAULT_SHOULD_RUN, name = "Should run the plugin", description = "If set to true the plugin will launch tool and parse result. If set to false the plugin will only parse the result file.", project = true),
		@Property(key = PhpUnitConfiguration.ANALYZE_ONLY_PROPERTY_KEY, defaultValue = PhpUnitConfiguration.DEFAULT_ANALYZE_ONLY, name = "Should the plugin only get analyzis results", description = "If set to true the plugin will only parse the analyzis result file. If set to false the plugin will launch tool and parse result.", project = true),
		@Property(key = PhpUnitConfiguration.SHOULD_DEAL_WITH_COVERAGE_PROPERTY_KEY, defaultValue = PhpUnitConfiguration.DEFAULT_SHOULD_DEAL_WITH_COVERAGE, name = "Should the plugin deal with php unit coverage issues.", description = "If set to true the plugin will also take php coverage files into account", project = true)

})
public class PhpUnitPlugin implements Plugin {

	/** The plugin key. */
	public static final String KEY = "PHP UNIT";

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 * @see org.sonar.api.Plugin#getDescription()
	 */
	public final String getDescription() {
		return "A plugin to cover the PHP unit files";
	}

	/**
	 * Gets the extensions.
	 * 
	 * @return the extensions
	 * @see org.sonar.api.Plugin#getExtensions()
	 */
	public final List<Class<? extends Extension>> getExtensions() {
		List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
		extensions.add(PhpUnitSensor.class);
		return extensions;
	}

	/**
	 * Gets the key.
	 * 
	 * @return the key
	 * @see org.sonar.api.Plugin#getKey()
	 */
	public final String getKey() {
		return KEY;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 * @see org.sonar.api.Plugin#getName()
	 */
	public final String getName() {
		return "PHP UNIT";
	}

	/**
	 * To string.
	 * 
	 * @return the string
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		return getKey();
	}
}
