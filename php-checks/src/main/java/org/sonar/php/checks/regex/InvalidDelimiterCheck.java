/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.checks.regex;

import java.util.regex.Matcher;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;

/**
 * This class does nothing. It exists only to be present in the SonarQube profile and GUI.
 * Issues for this class are created upfront, during the parsing of regex patterns
 */
@Rule(key = "S6393")
public class InvalidDelimiterCheck extends AbstractRegexCheck {

  private static final String MISSING_OPENING_DELIMITER_MESSAGE = "Add delimiters to this regular expression.";
  private static final String MISSING_CLOSING_DELIMITER_MESSAGE = "Add the missing \"%s\" delimiter to this regular expression.";

  @Override
  protected boolean hasValidDelimiters(LiteralTree tree) {
    String pattern = trimPattern(tree);
    if (pattern.length() >= 2) {
      Matcher m = DELIMITER_PATTERN.matcher(pattern);
      if (!m.find()) {
        newIssue(tree, MISSING_OPENING_DELIMITER_MESSAGE);
      } else if (!containsEndDelimiter(pattern.substring(1), m.group().charAt(0))) {
        newIssue(tree, String.format(MISSING_CLOSING_DELIMITER_MESSAGE, m.group().charAt(0)));
      }
    }
    // Always return false to interrupt stream in AbstractRegexCheck::checkFunctionCall
    return false;
  }

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    // do nothing, will not be called due to stream interruption
  }

}
