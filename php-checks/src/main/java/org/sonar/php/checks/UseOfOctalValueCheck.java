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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import java.util.regex.Pattern;

@Rule(key = UseOfOctalValueCheck.KEY)
public class UseOfOctalValueCheck extends PHPVisitorCheck {

  public static final String KEY = "S1314";
  private static final String MESSAGE = "Use decimal rather than octal values.";

  // Pattern syntax from https://www.php.net/manual/en/language.types.integer.php#language.types.integer.syntax
  private static final Pattern OCTAL_NUMERIC_PATTERN = Pattern.compile("^0[0-7]+(_[0-7]+)?+$");

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Tree.Kind.NUMERIC_LITERAL)) {
      checkNumericValue(tree);
    }

    super.visitLiteral(tree);
  }

  private void checkNumericValue(LiteralTree tree) {
    String value = tree.value().replace("_", "");
    if (OCTAL_NUMERIC_PATTERN.matcher(value).find() && !isException(value)) {
      context().newIssue(this, tree, MESSAGE);
    }
  }

  /**
   * This rule should not apply to values smaller than 8 and octal values having 3 digits,
   * since 3 digits octal values are often used as file permission masks.
   * Also values like "03" should not raise an issue because they are used in dates.
   */
  private static boolean isException(String value) {
    return value.length() == 4 || value.length() == 2;
  }
}
