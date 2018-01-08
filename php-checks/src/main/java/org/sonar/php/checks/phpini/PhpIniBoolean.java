/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.checks.phpini;

import com.google.common.collect.ImmutableSet;
import java.util.Locale;
import java.util.Set;
import org.sonar.php.ini.tree.Directive;

public enum PhpIniBoolean {

  ON(ImmutableSet.of("1", "ON", "TRUE", "YES")),
  OFF(ImmutableSet.of("0", "OFF", "FALSE", "NO"));

  private final Set<String> variants;

  PhpIniBoolean(Set<String> variants) {
    this.variants = variants;
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
