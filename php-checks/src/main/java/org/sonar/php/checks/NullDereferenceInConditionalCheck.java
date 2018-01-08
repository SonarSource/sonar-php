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
package org.sonar.php.checks;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.SyntacticEquivalence;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.visitors.CheckContext;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1697")
public class NullDereferenceInConditionalCheck extends PHPVisitorCheck {

  private static final String MESSAGE_FORMAT = "Either reverse the equality operator in the \"%s\" null test, or reverse the logical operator that follows it.";

  private static final List<Kind> AND_KINDS = ImmutableList.of(Kind.CONDITIONAL_AND, Kind.ALTERNATIVE_CONDITIONAL_AND);
  private static final List<Kind> OR_KINDS = ImmutableList.of(Kind.CONDITIONAL_OR, Kind.ALTERNATIVE_CONDITIONAL_OR);

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
    ExpressionTree comparedWithNullExpression = getComparedWithNullExpression(tree.leftOperand(), tree.getKind());
    if (comparedWithNullExpression != null) {
      tree.rightOperand().accept(new NullExpressionUsageVisitor(comparedWithNullExpression, context(), this));
    }

    super.visitBinaryExpression(tree);
  }

  @Nullable
  private static ExpressionTree getComparedWithNullExpression(ExpressionTree expression, Tree.Kind kind) {
    ExpressionTree comparedExpression = getExpressionEqualNullWithAnd(expression, kind);

    if (comparedExpression == null) {
      comparedExpression = getExpressionNotEqualNullWithOr(expression, kind);

      if (comparedExpression == null) {
        comparedExpression = getExpressionComparedWithFunction(expression, kind);
      }
    }

    return comparedExpression;
  }

  @Nullable
  private static ExpressionTree getExpressionEqualNullWithAnd(ExpressionTree tree, Kind kind) {
    if (AND_KINDS.contains(kind)) {
      return getComparedWithNull(tree, Tree.Kind.EQUAL_TO, Tree.Kind.STRICT_EQUAL_TO);
    }

    return null;
  }

  @Nullable
  private static ExpressionTree getExpressionNotEqualNullWithOr(ExpressionTree tree, Kind kind) {
    if (OR_KINDS.contains(kind)) {
      return getComparedWithNull(tree, Tree.Kind.NOT_EQUAL_TO, Tree.Kind.STRICT_NOT_EQUAL_TO);
    }

    return null;
  }

  /**
   *  e.g.      is_null($obj) && ...     or      !is_null($obj) || ...
   */
  @Nullable
  private static ExpressionTree getExpressionComparedWithFunction(ExpressionTree expression, Kind kind) {
    if (OR_KINDS.contains(kind) && expression.is(Kind.LOGICAL_COMPLEMENT) && ((UnaryExpressionTree) expression).expression().is(Kind.FUNCTION_CALL)) {
      FunctionCallTree functionCall = (FunctionCallTree)((UnaryExpressionTree) expression).expression();
      return retrieveArgumentFromIsNullCall(functionCall);
    }

    if (AND_KINDS.contains(kind) && expression.is(Kind.FUNCTION_CALL)) {
      return retrieveArgumentFromIsNullCall((FunctionCallTree)expression);
    }

    return null;
  }

  @Nullable
  private static ExpressionTree retrieveArgumentFromIsNullCall(FunctionCallTree functionCall) {
    if ("is_null".equalsIgnoreCase(functionCall.callee().toString()) && functionCall.arguments().size() == 1) {
      return functionCall.arguments().get(0);
    } else {
      return null;
    }
  }

  @Nullable
  private static ExpressionTree getComparedWithNull(ExpressionTree expression, Tree.Kind kind1, Tree.Kind kind2) {
    ExpressionTree tree = removeParenthesis(expression);
    if (tree.is(kind1, kind2)) {
      BinaryExpressionTree binaryExp = (BinaryExpressionTree) tree;
      if (isNullLiteral(binaryExp.leftOperand())) {
        return removeParenthesis(binaryExp.rightOperand());

      } else if (isNullLiteral(binaryExp.rightOperand())) {
        return removeParenthesis(binaryExp.leftOperand());
      }
    }
    return null;
  }

  private static ExpressionTree removeParenthesis(ExpressionTree expressionTree) {
    if (expressionTree.is(Kind.PARENTHESISED_EXPRESSION)) {
      return removeParenthesis(((ParenthesisedExpressionTree) expressionTree).expression());
    }
    return expressionTree;
  }

  private static boolean isNullLiteral(Tree tree) {
    return tree.is(Tree.Kind.NULL_LITERAL);
  }

  private static class NullExpressionUsageVisitor extends PHPVisitorCheck {

    private final NullDereferenceInConditionalCheck check;
    private ExpressionTree nullExpression;
    private CheckContext context;

    public NullExpressionUsageVisitor(ExpressionTree nullExpression, CheckContext context, NullDereferenceInConditionalCheck check) {
      this.nullExpression = nullExpression;
      this.context = context;
      this.check = check;
    }

    @Override
    public void visitMemberAccess(MemberAccessTree tree) {
      if (SyntacticEquivalence.areSyntacticallyEquivalent(removeParenthesis(tree.object()), nullExpression)) {
        context.newIssue(check, nullExpression, String.format(MESSAGE_FORMAT, nullExpression.toString()));
      }

      super.visitMemberAccess(tree);
    }
  }

}
