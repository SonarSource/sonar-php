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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ElseifClauseTree;
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ConstantConditionCheck.KEY)
public class ConstantConditionCheck extends PHPVisitorCheck {

  public static final String KEY = "S5797";
  private static final String MESSAGE = "Replace this expression; used as a condition it will always be constant.";
  private static final Tree.Kind[] BOOLEAN_CONSTANT_KINDS = {
    Tree.Kind.BOOLEAN_LITERAL,
    Tree.Kind.NUMERIC_LITERAL,
    Tree.Kind.REGULAR_STRING_LITERAL,
    Tree.Kind.NULL_LITERAL,
    Tree.Kind.HEREDOC_LITERAL,
    Tree.Kind.NOWDOC_LITERAL,
    Tree.Kind.MAGIC_CONSTANT,
    Tree.Kind.ARRAY_INITIALIZER_FUNCTION,
    Tree.Kind.ARRAY_INITIALIZER_BRACKET,
    Tree.Kind.NEW_EXPRESSION,
    Tree.Kind.FUNCTION_EXPRESSION,
  };
  private static final Tree.Kind[] CONDITIONAL_KINDS = {
    Tree.Kind.CONDITIONAL_AND,
    Tree.Kind.CONDITIONAL_OR,
    Tree.Kind.ALTERNATIVE_CONDITIONAL_AND,
    Tree.Kind.ALTERNATIVE_CONDITIONAL_OR,
    Tree.Kind.ALTERNATIVE_CONDITIONAL_XOR,
  };

  private static boolean isFirstStatementClassDeclaration(IfStatementTree tree) {
    return tree.statements().stream()
      .findFirst()
      // Class declaration can't be written outside a block
      .filter(s -> s.is(Tree.Kind.BLOCK))
      .map(BlockTree.class::cast)
      .flatMap(block -> block.statements().stream().findFirst())
      .map(firstStatement -> firstStatement.is(Tree.Kind.CLASS_DECLARATION))
      .orElse(false);
  }

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    if (!isFirstStatementClassDeclaration(tree)) {
      ExpressionTree conditionExpression = tree.condition().expression();
      checkConstant(conditionExpression);
    }
    super.visitIfStatement(tree);
  }

  @Override
  public void visitElseifClause(ElseifClauseTree tree) {
    ExpressionTree conditionExpression = tree.condition().expression();
    checkConstant(conditionExpression);
    super.visitElseifClause(tree);
  }

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
    if (tree.is(CONDITIONAL_KINDS)) {
      checkConstant(tree.leftOperand());
      checkConstant(tree.rightOperand());
    }
    super.visitBinaryExpression(tree);
  }

  @Override
  public void visitPrefixExpression(UnaryExpressionTree tree) {
    if (tree.is(Tree.Kind.LOGICAL_COMPLEMENT)) {
      checkConstant(tree.expression());
    }
    super.visitPrefixExpression(tree);
  }

  @Override
  public void visitConditionalExpression(ConditionalExpressionTree tree) {
    ExpressionTree conditionExpression = tree.condition();
    checkConstant(conditionExpression);
    super.visitConditionalExpression(tree);
  }

  private void checkConstant(ExpressionTree conditionExpression) {
    if (conditionExpression.is(BOOLEAN_CONSTANT_KINDS)) {
      newIssue(conditionExpression, MESSAGE);
    }
  }
}
