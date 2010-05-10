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

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.AbstractLanguage;

/**
 * This class defines the PHP language.
 */
public class Php extends AbstractLanguage {

  /** The php language name */
  public static final String PHP_LANGUAGE_NAME = "PHP";

  /** An php instance. */
  public static final Php INSTANCE = new Php();

  /** The php language key. */
  public static final String KEY = "php";

  /** All the valid php files suffixes. */
  private static final String[] SUFFIXES = { "php", "php3", "php4", "php5", "phtml", "inc" };

  /**
   * Allows to know if the given file name has a valid suffix.
   * 
   * @param fileName
   *          String representing the file name
   * @return boolean <code>true</code> if the file name's suffix is known, <code>false</code> any other way
   */
  public static boolean hasValidSuffixes(String fileName) {
    String pathLowerCase = StringUtils.lowerCase(fileName);
    for (String suffix : SUFFIXES) {
      if (pathLowerCase.endsWith("." + StringUtils.lowerCase(suffix))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Default constructor.
   */
  public Php() {
    super(KEY, PHP_LANGUAGE_NAME);
  }

  /**
   * Gets the file suffixes.
   * 
   * @return the file suffixes
   * @see org.sonar.api.resources.Language#getFileSuffixes()
   */
  public String[] getFileSuffixes() {
    return SUFFIXES;
  }

}
