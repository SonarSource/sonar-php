/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.regex;

import java.util.HashMap;
import java.util.Map;

public class PhpRegexUtils {

  public static final Map<Character, Character> BRACKET_DELIMITERS = bracketDelimiters();

  private static Map<Character, Character> bracketDelimiters() {
    Map<Character, Character> delimiters = new HashMap<>();
    delimiters.put('[', ']');
    delimiters.put('{', '}');
    delimiters.put('<', '>');
    delimiters.put('(', ')');
    return delimiters;
  }

  private PhpRegexUtils() {
  }

  public static Character getEndDelimiter(String pattern) {
    char startDelimiter = pattern.charAt(0);
    return BRACKET_DELIMITERS.getOrDefault(startDelimiter, startDelimiter);
  }
}
