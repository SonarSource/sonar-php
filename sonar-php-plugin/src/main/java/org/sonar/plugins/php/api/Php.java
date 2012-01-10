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
package org.sonar.plugins.php.api;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.plugins.php.PhpPlugin;

/**
 * This class defines the PHP language.
 */
public final class Php extends AbstractLanguage {

  private Configuration configuration;

  /** The php language name */
  public static final String PHP_LANGUAGE_NAME = "PHP";

  /**
   * An array containing all PHP keywords.
   */
  public static final String[] PHP_KEYWORDS_ARRAY = new String[] {"and", "or", "xor", "exception", "array", "as", "break", "case",
    "class", "const", "continue", "declare", "default", "die", "do", "echo", "else", "elseif", "empty", "enddeclare", "endfor",
    "endforeach", "endif", "endswitch", "endwhile", "eval", "exit", "extends", "for", "foreach", "function", "global", "if", "include",
    "include_once", "isset", "list", "new", "print", "require", "require_once", "return", "static", "switch", "unset", "use", "var",
    "while", "final", "php_user_filter", "interface", "implements", "instanceof", "public", "private", "protected", "abstract", "clone",
    "try", "catch", "throw", "cfunction", "old_function", "this", "final", "namespace", "goto"};

  /**
   * An array containing reserved variables.
   */
  public static final String[] PHP_RESERVED_VARIABLES_ARRAY = new String[] {"__FUNCTION__", "__CLASS__", "__METHOD__", "__NAMESPACE__",
    "__DIR__", "__FILE__", "__LINE__", "$this"};

  /** An php instance. */
  public static final Php PHP = new Php();

  /** The php language key. */
  public static final String KEY = "php";

  /**
   * Construct the PHP language instance based on its key.
   */

  public Php() {
    super(KEY, PHP_LANGUAGE_NAME);
  }

  /**
   * @param configuration
   *          the configuration to set
   */
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public String[] getFileSuffixes() {
    String[] suffixes = StringUtils.split(PhpPlugin.FILE_SUFFIXES_DEFVALUE, ",");
    if (configuration != null) {
      String[] configuredSuffixes = configuration.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY);
      if (configuredSuffixes != null && configuredSuffixes.length > 0) {
        suffixes = configuredSuffixes;
      }
    }
    return suffixes;
  }

  /**
   * Allows to know if the given file name has a valid suffix.
   * 
   * @param fileName
   *          String representing the file name
   * @return boolean <code>true</code> if the file name's suffix is known, <code>false</code> any other way
   */
  public static boolean hasValidSuffixes(String fileName) {
    String pathLowerCase = StringUtils.lowerCase(fileName);
    for (String suffix : PHP.getFileSuffixes()) {
      if (pathLowerCase.endsWith("." + StringUtils.lowerCase(suffix))) {
        return true;
      }
    }
    return false;
  }

}
