/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.plugins.php.PhpPlugin;

/**
 * This class defines the PHP language.
 */
public final class Php extends AbstractLanguage {

  public static final String NAME = "PHP";
  public static final String KEY = "php";

  public static final String DEFAULT_FILE_SUFFIXES = "php,php3,php4,php5,phtml,inc";

  private Configuration settings;

  /**
   * Construct the PHP language.
   */
  public Php(Configuration settings) {
    super(KEY, NAME);
    this.settings = settings;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = filterEmptyStrings(settings.getStringArray(PhpPlugin.FILE_SUFFIXES_KEY));
    if (suffixes.length == 0) {
      suffixes = Php.DEFAULT_FILE_SUFFIXES.split(",");
    }
    return suffixes;
  }

  private static String[] filterEmptyStrings(String[] stringArray) {
    List<String> nonEmptyStrings = new ArrayList<>();
    for (String string : stringArray) {
      if (!string.trim().isEmpty()) {
        nonEmptyStrings.add(string.trim());
      }
    }
    return nonEmptyStrings.toArray(new String[nonEmptyStrings.size()]);
  }

  /**
   * Allows to know if the given file name has a valid suffix.
   *
   * @param fileName String representing the file name
   * @return boolean <code>true</code> if the file name's suffix is known, <code>false</code> any other way
   */
  public boolean hasValidSuffixes(String fileName) {
    String pathLowerCase = fileName.toLowerCase(Locale.ROOT);
    for (String suffix : getFileSuffixes()) {
      if (pathLowerCase.endsWith("." + suffix.toLowerCase(Locale.ROOT))) {
        return true;
      }
    }
    return false;
  }

}
