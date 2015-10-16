/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.checks;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Rule(
  key = ConcatenatedStringLiteralCheck.KEY,
  name = "String literals should not be concatenated",
  priority = Priority.MINOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("5min")
public class ConcatenatedStringLiteralCheck extends PHPVisitorCheck {

  public static final String KEY = "S2005";
  private static final String MESSAGE = "Combine these strings instead of concatenating them.";

  private static final Kind[] STRING_KINDS = {
    Kind.REGULAR_STRING_LITERAL,
    Kind.EXPANDABLE_STRING_LITERAL
  };


  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
    List<ExpressionTree> arguments = getFlatConcatenationArguments(tree);

    for (int i = 0; i < arguments.size() - 1; i++) {
      if (arguments.get(i).is(STRING_KINDS) && arguments.get(i + 1).is(STRING_KINDS)) {
        context().newIssue(KEY, MESSAGE).tree(arguments.get(i));
        return;
      }
    }

    if (arguments.isEmpty()) {
      super.visitBinaryExpression(tree);
    }
  }

  private static List<ExpressionTree> getFlatConcatenationArguments(BinaryExpressionTree tree) {
    List<ExpressionTree> arguments = new ArrayList<>();

    ExpressionTree currentExpression = tree;

    while (currentExpression.is(Kind.CONCATENATION)) {
      arguments.add(((BinaryExpressionTree) currentExpression).rightOperand());
      currentExpression = ((BinaryExpressionTree) currentExpression).leftOperand();
    }

    if (currentExpression.is(STRING_KINDS)) {
      arguments.add(currentExpression);
    }

    Collections.reverse(arguments);

    return arguments;
  }

}
