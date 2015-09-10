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

import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.Iterator;

@Rule(
  key = "S2005",
  name = "String literals should not be concatenated",
  priority = Priority.MINOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("5min")
public class ConcatenatedStringLiteralCheck extends SquidCheck<LexerlessGrammar> {

  private static class Expression {
    AstNode node;
    boolean isStringLiteral = false;

    private Expression(AstNode node) {
      this.node = node;
      AstNode child = this.node.getFirstChild();

      if (child.is(PHPGrammar.COMMON_SCALAR)) {
        AstNode scalar = child.getFirstChild();

        if (scalar.is(PHPGrammar.STRING_LITERAL)) {
          isStringLiteral = true;
        }
      }
    }

    public String getTokenValue() {
      return node.getTokenOriginalValue();
    }

  }

  @Override
  public void init() {
    subscribeTo(PHPGrammar.CONCATENATION_EXPR);
  }

  @Override
  public void visitNode(AstNode astNode) {
    Iterator<AstNode> childIt = astNode.getChildren().iterator();
    Expression previousExpr = new Expression(childIt.next());

    while (childIt.hasNext()) {
      AstNode currentNode = childIt.next();

      if (!currentNode.is(PHPPunctuator.DOT)) {
        Expression currentExpr = new Expression(currentNode);

        if (previousExpr.isStringLiteral && currentExpr.isStringLiteral) {
          getContext().createLineViolation(this, "Combine these strings instead of concatenating them.", previousExpr.node,
            previousExpr.getTokenValue(), currentExpr.getTokenValue());
          // On issue per concatenation expression is reported
          break;
        }
        previousExpr = currentExpr;
      }
    }
  }

}
