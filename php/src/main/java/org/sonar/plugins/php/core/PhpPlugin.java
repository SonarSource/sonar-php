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

package org.sonar.plugins.php.core;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.plugins.php.core.decorators.PhpDirectoryDecorator;
import org.sonar.plugins.php.core.decorators.PhpFilesDecorator;
import org.sonar.plugins.php.core.sensors.PhpSourceImporter;
import org.sonar.plugins.php.cpd.PhpCpdMapping;

/**
 * This class is the sonar entry point of this plugin. It declares all the extension that can be launched with this plugin
 */
@Properties({
	  @Property(key = PhpPlugin.FILE_SUFFIXES_KEY, defaultValue = PhpPlugin.DEFAULT_SUFFIXES, name = "File suffixes",
	      description = "Comma-separated list of suffixes for files to analyze. To not filter, leave the list empty.",
	  global=true,
	  project = true)
	})
public class PhpPlugin implements Plugin {

  /** All the valid php files suffixes. */
  public static final String DEFAULT_SUFFIXES = "php,php3,php4,php5,phtml,inc";

  public static final String FILE_SUFFIXES_KEY = "sonar.php.file.suffixes";

  /** The php plugin key. */
  public static final String KEY = "PHP Language";

  /**
   * Gets the description.
   * 
   * @return the description
   * @see org.sonar.api.Plugin#getDescription()
   */
  public final String getDescription() {
    return "A plugin to cover the PHP language";
  }

  /**
   * Gets the extensions.
   * 
   * @return the extensions
   * @see org.sonar.api.Plugin#getExtensions()
   */
  public final List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
    // Adds the language
    extensions.add(Php.class);
    // Source importer
    extensions.add(PhpSourceImporter.class);
    extensions.add(PhpCpdMapping.class);
    // Php resource decorators
    extensions.add(PhpDirectoryDecorator.class);
    extensions.add(PhpFilesDecorator.class);
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
    return "PHP Language";
  }

  /**
   * To string.
   * 
   * @return the string
   * @see java.lang.Object#toString()
   */
  @Override
  public final String toString() {
    return getKey();
  }
}
