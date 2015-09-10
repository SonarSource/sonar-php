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
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.List;

@Rule(
  key = "S2041",
  name = "Parentheses should not be used for calls to \"echo\"",
  priority = Priority.MAJOR,
  tags = {Tags.PITFALL})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class EchoWithParenthesisCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(PHPGrammar.ECHO_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    List<AstNode> expressions = astNode.getChildren(PHPGrammar.EXPRESSION);

    if (expressions.size() == 1) {
      AstNode expression = expressions.get(0).getFirstChild().getFirstChild();

      if (expression.is(PHPGrammar.MEMBER_EXPRESSION) && expression.getFirstChild().is(PHPGrammar.PARENTHESIS_EXPRESSION)) {
        getContext().createLineViolation(this, "Remove the parentheses from this \"echo\" call.", expression);
      }
    }
  }
}
