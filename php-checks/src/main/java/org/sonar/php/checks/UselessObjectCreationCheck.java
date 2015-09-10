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
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S1848",
  name = "Objects should not be created to be dropped immediately without being used",
  tags = {"bug"},
  priority = Priority.CRITICAL)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.CRITICAL)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.INSTRUCTION_RELIABILITY)
@SqaleConstantRemediation("5min")
public class UselessObjectCreationCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(PHPGrammar.EXPRESSION_STATEMENT);
  }

  @Override
  public void visitNode(AstNode node) {
    AstNode expression = node.getFirstChild(PHPGrammar.EXPRESSION);
    AstNode postfixExpression = expression.getFirstChild(PHPGrammar.POSTFIX_EXPR);
    if (postfixExpression != null) {
      AstNode newExpression = postfixExpression.getFirstChild(PHPGrammar.NEW_EXPR);
      if (newExpression != null) {
        String className = CheckUtils.getExpressionAsString(newExpression.getFirstChild(PHPGrammar.MEMBER_EXPRESSION).getFirstChild());
        getContext().createLineViolation(
          this, "Either remove this useless object instantiation of class \"{0}\" or use it", node, className);
      }
    }
  }

}
