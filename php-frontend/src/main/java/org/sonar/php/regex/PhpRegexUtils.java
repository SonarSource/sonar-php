/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.regex;

import java.util.HashMap;
import java.util.Map;

public class PhpRegexUtils {

  public static final Map<Character, Character> BRACKET_DELIMITERS = bracketDelimiters();

  private static Map<Character, Character> bracketDelimiters() {
    Map<Character, Character> delimiters = new HashMap<>();
    delimiters.put('[', ']');
    delimiters.put('{','}');
    delimiters.put('<', '>');
    delimiters.put('(',')');
    return delimiters;
  }

  private PhpRegexUtils() {
  }

  public static Character getEndDelimiter(String pattern) {
    char startDelimiter = pattern.charAt(0);
    return BRACKET_DELIMITERS.getOrDefault(startDelimiter, startDelimiter);
  }
}
