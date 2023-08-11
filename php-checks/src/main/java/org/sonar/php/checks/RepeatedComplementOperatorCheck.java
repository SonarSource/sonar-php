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

import java.util.HashMap;
import java.util.Map;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2761")
public class RepeatedComplementOperatorCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Use the \"%s\" operator just once or not at all.";
  private static final String ADDITIONAL_CAST_MESSAGE = " If a type cast is intended, use \"%s\" instead.";


  private static final Kind[] COMPLEMENT_UNARY = {
    Kind.BITWISE_COMPLEMENT,
    Kind.LOGICAL_COMPLEMENT,
    Kind.UNARY_MINUS};

  private static final Kind[] CAST_UNARY = {
    Kind.LOGICAL_COMPLEMENT,
    Kind.UNARY_MINUS};

  private static final Map<Kind, String> CAST_FUNCTION = castFunctions();

  public static Map<Kind, String> castFunctions() {
    Map<Kind, String> map = new HashMap();
    map.put(Kind.LOGICAL_COMPLEMENT, "(bool)");
    map.put(Kind.UNARY_MINUS, "(int)");
    return map;
  }


  @Override
  public void visitPrefixExpression(UnaryExpressionTree tree) {
    if (tree.is(COMPLEMENT_UNARY)) {
      checkRepeatedComplement(tree);
    }

    super.visitPrefixExpression(tree);
  }

  private void checkRepeatedComplement(UnaryExpressionTree tree) {
    if (CheckUtils.skipParenthesis(tree.expression()).is(tree.getKind())) {
      String message = String.format(MESSAGE, tree.operator().text());
      if (tree.is(CAST_UNARY)) {
        message += String.format(ADDITIONAL_CAST_MESSAGE, CAST_FUNCTION.get(tree.getKind()));
      }

      context().newIssue(this, tree, message);
    }
  }
}
