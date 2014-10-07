/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * dev@sonar.codehaus.org
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

import com.sonar.sslr.api.AstNode;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S1488",
  priority = Priority.MINOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
public class ImmediatelyReturnedVariableCheck extends SquidCheck<LexerlessGrammar> {

  private boolean inFunction = false;

  @Override
  public void init() {
    subscribeTo(CheckUtils.functions());
    subscribeTo(PHPGrammar.EXPRESSION_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(CheckUtils.functions())) {
      inFunction = true;
    }
    if (inFunction && astNode.is(PHPGrammar.EXPRESSION_STATEMENT)) {
      checkExpression(astNode);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(CheckUtils.functions())) {
      inFunction = false;
    }
  }

  private void checkExpression(AstNode exprStmt) {
    AstNode nextStmt = exprStmt.getNextAstNode().getFirstChild();
    if (nextStmt == null || nextStmt.isNot(PHPGrammar.RETURN_STATEMENT, PHPGrammar.THROW_STATEMENT) || isInnerIfStatement(exprStmt)) {
      return;
    }

    AstNode expression = exprStmt.getFirstChild(PHPGrammar.EXPRESSION).getFirstChild();

    if (expression.is(PHPGrammar.ASSIGNMENT_EXPR)) {
      checkAssignedVariable(expression, nextStmt);
    } else {
      AstNode exprChild = expression.getFirstChild();
      if (exprChild.is(PHPGrammar.LIST_ASSIGNMENT_EXPR)) {
        checkListVariables(exprChild, nextStmt);
      }
    }
  }

  private boolean isInnerIfStatement(AstNode exprStmt) {
    return exprStmt.getParent().getParent().is(PHPGrammar.IF_STATEMENT);
  }

  private void checkListVariables(AstNode listAssignmentExpr, AstNode nextStmt) {
    AstNode assignmentList = listAssignmentExpr.getFirstChild(PHPGrammar.LIST_EXPR).getFirstChild(PHPGrammar.ASSIGNMENT_LIST);

    for (AstNode element : assignmentList.getChildren(PHPGrammar.ASSIGNMENT_LIST_ELEMENT)) {
      AstNode variable = element.getFirstChild(PHPGrammar.MEMBER_EXPRESSION);

      if (variable != null && variable.getNumberOfChildren() == 1 && isSimplyReturnedOrThrown(variable, nextStmt)) {
        reportIssue(variable, nextStmt);
      }
    }
  }

  private void checkAssignedVariable(AstNode assignmentExpr, AstNode nextStmt) {
    AstNode leftExpr = getLeftHandExpression(assignmentExpr);

    if (leftExpr != null && isSimplyReturnedOrThrown(leftExpr, nextStmt)) {
      reportIssue(leftExpr, nextStmt);
    }
  }

  private void reportIssue(AstNode varNode, AstNode nextStmt) {
    getContext().createLineViolation(this, "Immediately {0} this expression instead of assigning it to the temporary variable \"{1}\".",
      varNode, nextStmt.getFirstChild().getTokenOriginalValue(), varNode.getTokenOriginalValue());
  }

  private boolean isSimplyReturnedOrThrown(AstNode varNode, AstNode nextStmt) {
    AstNode returnedOrThrownExpr = nextStmt.getFirstChild(PHPGrammar.EXPRESSION);
    return returnedOrThrownExpr != null
      && CheckUtils.getExpressionAsString(returnedOrThrownExpr).equals(CheckUtils.getExpressionAsString(varNode));
  }

  /**
   * Returns left hand expression of the assignment only if it a simple variable,
   * returns null otherwise.
   * <p/>
   * Example:<br>
   * $a = 1 returns $a<br>
   * $a->property = 1 returns null
   */
  private AstNode getLeftHandExpression(AstNode assignmentExpr) {
    AstNode leftExpr = assignmentExpr.getFirstChild();
    AstNode variableNode = null;

    if (leftExpr.is(PHPGrammar.MEMBER_EXPRESSION)) {
      variableNode = leftExpr;
    } else if (leftExpr.is(PHPGrammar.POSTFIX_EXPR)) {
      variableNode = leftExpr.getFirstChild();
    }

    if (variableNode != null && variableNode.getNumberOfChildren() == 1) {
      AstNode variableChild = variableNode.getFirstChild();

      if (variableChild.is(PHPGrammar.VARIABLE_WITHOUT_OBJECTS) && variableChild.getFirstChild(PHPGrammar.REFERENCE_VARIABLE).getNumberOfChildren() == 1) {
        return variableChild;
      }
    }
    return null;
  }

}
