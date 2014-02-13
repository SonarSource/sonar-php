/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
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
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.squid.checks.SquidCheck;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.PHPGrammar;

@Rule(
  key = "S1125",
  priority = Priority.MINOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MINOR)
public class BooleanEqualityComparisonCheck extends SquidCheck<Grammar> {

  @Override
  public void init() {
    subscribeTo(PHPGrammar.EQUALITY_EXPR);
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode boolLiteral = getBooleanLiteralFromExpresion(astNode);

    if (boolLiteral != null) {
      getContext().createLineViolation(this, "Remove the literal \"" + boolLiteral.getTokenOriginalValue() + "\" boolean value.", astNode);
    }
  }

  private static AstNode getBooleanLiteralFromExpresion(AstNode equalityExpr) {
    AstNode leftExpr = equalityExpr.getFirstChild(PHPGrammar.EQUALITY_OPERATOR).getPreviousAstNode();
    AstNode rightExpr = equalityExpr.getFirstChild(PHPGrammar.EQUALITY_OPERATOR).getNextAstNode();

    if (isBooleanLiteral(leftExpr)) {
      return leftExpr;
    } else if (isBooleanLiteral(rightExpr)) {
      return rightExpr;
    } else {
      return null;
    }
  }

  private static boolean isBooleanLiteral(AstNode astNode) {
    return astNode.is(PHPGrammar.POSTFIX_EXPR)
      && astNode.getFirstChild().is(PHPGrammar.COMMON_SCALAR)
      && astNode.getFirstChild().getFirstChild().is(PHPGrammar.BOOLEAN_LITERAL);
  }
}
