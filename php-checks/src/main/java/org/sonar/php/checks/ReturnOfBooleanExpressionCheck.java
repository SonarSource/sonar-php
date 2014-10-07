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
  key = "S1126",
  priority = Priority.MINOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)

public class ReturnOfBooleanExpressionCheck extends SquidCheck<LexerlessGrammar> {
  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.IF_STATEMENT,
      PHPGrammar.ALTERNATIVE_IF_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!hasElseIf(astNode) && hasElse(astNode) && returnsBoolean(getTrueStatement(astNode)) && returnsBoolean(getFalseStatement(astNode))) {
      getContext().createLineViolation(this, "Replace this \"if-then-else\" statement by a single \"return\" statement.", astNode);
    }
  }

  private static AstNode getTrueStatement(AstNode ifStmt) {
    return getInnerStmtOfIfStmt(ifStmt.getFirstChild(PHPGrammar.ELSE_CLAUSE, PHPGrammar.ALTERNATIVE_ELSE_CLAUSE)
      .getFirstChild(PHPGrammar.STATEMENT, PHPGrammar.INNER_STATEMENT_LIST));
  }

  private static AstNode getFalseStatement(AstNode ifStmt) {
    return getInnerStmtOfIfStmt(ifStmt.getFirstChild(PHPGrammar.STATEMENT, PHPGrammar.INNER_STATEMENT_LIST));
  }

  private static AstNode getInnerStmtOfIfStmt(AstNode statement) {
    if (statement != null && statement.is(PHPGrammar.INNER_STATEMENT_LIST)) {
      return statement.getNumberOfChildren() > 1 ? null : statement.getFirstChild();
    }

    return statement;
  }

  private static boolean hasElseIf(AstNode ifStmt) {
    return ifStmt.hasDirectChildren(PHPGrammar.ELSEIF_LIST, PHPGrammar.ALTERNATIVE_ELSEIF_LIST);
  }

  private static boolean hasElse(AstNode ifStmt) {
    return ifStmt.hasDirectChildren(PHPGrammar.ELSE_CLAUSE, PHPGrammar.ALTERNATIVE_ELSE_CLAUSE);
  }

  private static boolean returnsBoolean(AstNode statement) {
    return statement != null && (isBlockReturningBooleanLiteral(statement) || isSimpleReturnBooleanLiteral(statement));
  }

  private static boolean isBlockReturningBooleanLiteral(AstNode statement) {
    AstNode block = statement.getFirstChild(PHPGrammar.BLOCK);
    if (block == null) {
      return false;
    }

    AstNode stmtList = block.getFirstChild(PHPGrammar.INNER_STATEMENT_LIST);
    return stmtList != null
      && stmtList.getNumberOfChildren() == 1
      && isSimpleReturnBooleanLiteral(stmtList.getFirstChild());
  }

  private static boolean isSimpleReturnBooleanLiteral(AstNode statement) {
    AstNode returnStmt = statement.getFirstChild(PHPGrammar.RETURN_STATEMENT);
    if (returnStmt == null) {
      return false;
    }

    AstNode expression = returnStmt.getFirstChild(PHPGrammar.EXPRESSION);
    return expression != null && CheckUtils.isExpressionABooleanLiteral(expression);
  }
}
