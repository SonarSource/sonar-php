/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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

import com.google.common.collect.ImmutableSet;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S4784")
public class RegexUsageCheck extends FunctionUsageCheck {

  private static final String MESSAGE = "Make sure that using a regular expression is safe here.";

  private static final ImmutableSet<String> FUNCTION_NAMES = ImmutableSet.of(
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
    "mb_ereg_search_init",
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
    "preg_replace_callback_array",
    "preg_split",
    "split",
    "spliti");

  @Override
  protected ImmutableSet<String> functionNames() {
    return FUNCTION_NAMES;
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    if (!tree.arguments().isEmpty()) {
      context().newIssue(this, tree, MESSAGE);
    }
  }

}
