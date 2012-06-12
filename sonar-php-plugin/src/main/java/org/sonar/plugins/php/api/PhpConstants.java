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

/**
 * Constants for the PHP plugin.
 */
public final class PhpConstants {

  public static final String LANGUAGE_NAME = "PHP";
  public static final String LANGUAGE_KEY = "php";

  /** All the valid php files suffixes. */
  public static final String FILE_SUFFIXES_KEY = "sonar.php.file.suffixes";
  public static final String FILE_SUFFIXES_DEFVALUE = "php,php3,php4,php5,phtml,inc";

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

}
