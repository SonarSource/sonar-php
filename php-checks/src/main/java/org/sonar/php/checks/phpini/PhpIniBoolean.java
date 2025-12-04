/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.checks.phpini;

import java.util.Locale;
import java.util.Set;
import org.sonar.php.ini.tree.Directive;

public enum PhpIniBoolean {

  ON("1", "ON", "TRUE", "YES"),
  OFF("0", "OFF", "FALSE", "NO");

  private final Set<String> variants;

  PhpIniBoolean(String... variants) {
    this.variants = Set.of(variants);
  }

  public boolean matchesValue(Directive directive) {
    return matchesValue(directive.value().text());
  }

  public boolean matchesValue(String rawValue) {
    String value = rawValue;
    if (isQuotedValue(value, "\"") || isQuotedValue(value, "'")) {
      value = value.substring(1, value.length() - 1);
    }
    return variants.contains(value.toUpperCase(Locale.ENGLISH));
  }

  private static boolean isQuotedValue(String value, String quote) {
    return value.startsWith(quote) && value.endsWith(quote);
  }
}
