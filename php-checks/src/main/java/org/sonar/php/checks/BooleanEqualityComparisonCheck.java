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

import com.google.common.collect.Maps;
import com.sonar.sslr.api.AstNode;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;
import java.util.HashMap;

@Rule(
  key = "S1125",
  name = "Literal boolean values should not be used in condition expressions",
  priority = Priority.MINOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
public class BooleanEqualityComparisonCheck extends SquidCheck<LexerlessGrammar> {

  private HashMap<Integer, Integer> alreadyChecked = Maps.newHashMap();

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.UNARY_EXPR,
      PHPGrammar.EQUALITY_OPERATOR,
      PHPGrammar.LOGICAL_AND_OPERATOR,
      PHPGrammar.LOGICAL_OR_OPERATOR);
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode boolLiteral = getBooleanLiteralFromExpression(astNode);

    if (boolLiteral != null && !isAlreadyChecked(boolLiteral)) {
      getContext().createLineViolation(this, "Remove the literal \"" + boolLiteral.getTokenOriginalValue() + "\" boolean value.", astNode);
      alreadyChecked.put(boolLiteral.getTokenLine(), boolLiteral.getToken().getColumn());
    }
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    alreadyChecked.clear();
  }

  private static AstNode getBooleanLiteralFromExpression(AstNode expression) {
    if (expression.is(PHPGrammar.UNARY_EXPR)) {
      return getBooleanLiteralFromUnaryExpression(expression);
    }

    AstNode leftExpr = expression.getPreviousAstNode();
    AstNode rightExpr = expression.getNextAstNode();

    if (isBooleanLiteral(leftExpr)) {
      return leftExpr;
    } else if (isBooleanLiteral(rightExpr)) {
      return rightExpr;
    } else {
      return null;
    }
  }

  private static AstNode getBooleanLiteralFromUnaryExpression(AstNode unaryExpression) {
    AstNode boolLiteral = null;

    if (unaryExpression.getFirstChild().is(PHPPunctuator.BANG)) {
      AstNode expr = unaryExpression.getLastChild();

      if (isBooleanLiteral(expr)) {
        boolLiteral = expr;
      }
    }
    return boolLiteral;
  }

  private static boolean isBooleanLiteral(AstNode astNode) {
    return astNode.is(PHPGrammar.POSTFIX_EXPR)
      && astNode.getFirstChild().is(PHPGrammar.COMMON_SCALAR)
      && astNode.getFirstChild().getFirstChild().is(PHPGrammar.BOOLEAN_LITERAL);
  }

  private boolean isAlreadyChecked(AstNode boolLiteral) {
    Integer column = alreadyChecked.get(boolLiteral.getTokenLine());
    return column != null && column == boolLiteral.getToken().getColumn();
  }

}
