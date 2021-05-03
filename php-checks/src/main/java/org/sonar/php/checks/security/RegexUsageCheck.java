/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.checks.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;

@Rule(key = "S4784")
public class RegexUsageCheck extends FunctionUsageCheck {

  private static final String MESSAGE = "Make sure that using a regular expression is safe here.";

  private static final Set<Character> SPECIAL_CHARS = new HashSet<>(Arrays.asList('+', '*', '{'));
  private static final int MIN_PATTERN_LENGTH = 3 + 2 + 2; // 2 for string quotes and 2 for regex pattern delimeters

  // this function accepts pattern as second argument, all others as first
  private static final String MB_EREG_SEARCH_INIT = "mb_ereg_search_init";

  private static final Set<String> FUNCTION_NAMES = SetUtils.immutableSetOf(
    "ereg",
    "ereg_replace",
    "eregi",
    "eregi_replace",
    "fnmatch",
    "mb_ereg",
    "mb_ereg_match",
    "mb_ereg_replace",
    "mb_ereg_replace_callback",
    "mb_ereg_search",
    MB_EREG_SEARCH_INIT,
    "mb_ereg_search_pos",
    "mb_ereg_search_regs",
    "mb_eregi",
    "mb_eregi_replace",
    "preg_filter",
    "preg_grep",
    "preg_match",
    "preg_match_all",
    "preg_replace",
    "preg_replace_callback",
    "preg_split",
    "split",
    "spliti");

  @Override
  protected Set<String> functionNames() {
    return FUNCTION_NAMES;
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    int index = getPatternArgumentIndex(tree);
    Optional<CallArgumentTree> argument = CheckUtils.argument(tree, "pattern", index);
    if (!argument.isPresent()) {
      return;
    }

    ExpressionTree argumentValue = argument.get().value();
    if (argumentValue.is(Kind.REGULAR_STRING_LITERAL)) {
      String value = ((LiteralTree) argumentValue).value();
      if (value.length() >= MIN_PATTERN_LENGTH && hasEnoughNumberOfSpecialChars(value)) {
        context().newIssue(this, tree, MESSAGE);
      }
    }
  }

  private static int getPatternArgumentIndex(FunctionCallTree tree) {
    if (tree.callee().toString().equalsIgnoreCase(MB_EREG_SEARCH_INIT)) {
      return 1;
    }
    return 0;
  }

  private static boolean hasEnoughNumberOfSpecialChars(String value) {
    int numberOfSpecialChars = 0;
    for (char c : value.toCharArray()) {
      if (SPECIAL_CHARS.contains(c)) {
        numberOfSpecialChars++;
      }
      if (numberOfSpecialChars == 2) {
        return true;
      }
    }
    return false;
  }

}
