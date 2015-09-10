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

@Rule(
  key = "S1763",
  name = "Jump statements should not be followed by other statements",
  priority = Priority.MAJOR,
  tags = {Tags.MISRA, Tags.CERT, Tags.CWE, Tags.UNUSED})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("5min")
public class CodeFollowingJumpStatementCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.BREAK_STATEMENT,
      PHPGrammar.RETURN_STATEMENT,
      PHPGrammar.CONTINUE_STATEMENT,
      PHPGrammar.THROW_STATEMENT);
  }

  @Override
  public void visitNode(AstNode node) {
    AstNode statementNode = node.getParent();

    AstNode nextStatement = nextStatement(statementNode);
    if (nextStatement != null) {
      getContext().createLineViolation(this, "Remove the code after this \"{0}\".", nextStatement,
        node.getFirstChild().getTokenOriginalValue());
    }
  }

  private AstNode nextStatement(AstNode statementNode) {
    AstNode next = statementNode.getNextSibling();
    while (next != null) {
      if (next.is(PHPGrammar.STATEMENT) && !next.getFirstChild().is(PHPGrammar.EMPTY_STATEMENT)) {
        return next;
      }
      next = next.getNextSibling();
    }
    return null;
  }

}
