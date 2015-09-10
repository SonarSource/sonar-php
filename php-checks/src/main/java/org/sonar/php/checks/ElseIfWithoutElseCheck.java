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

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import com.sonar.sslr.api.AstNode;

@Rule(
  key = "S126",
  name = "\"if ... else if\" constructs shall be terminated with an \"else\" clause",
  priority = Priority.MAJOR,
  tags = {Tags.CERT, Tags.MISRA})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("5min")
public class ElseIfWithoutElseCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.IF_STATEMENT,
      PHPGrammar.ALTERNATIVE_IF_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (isElseIf(astNode) && !hasElse(astNode)) {
      AstNode reportNode = hasElseif(astNode) ? getLastElseif(astNode) : astNode;
      reportIssue(reportNode);
    } else if (hasElseif(astNode) && !hasElse(astNode)) {
      reportIssue(getLastElseif(astNode));
    }
  }

  private void reportIssue(AstNode node) {
    getContext().createLineViolation(this, "Add the missing \"else\" clause.", node);
  }

  private static boolean isElseIf(AstNode ifStmt) {
    AstNode parentPreviousSibling = ifStmt.getParent().getPreviousSibling();
    return parentPreviousSibling != null && parentPreviousSibling.is(PHPKeyword.ELSE);
  }

  private static AstNode getLastElseif(AstNode ifStmt) {
    return ifStmt.getFirstChild(PHPGrammar.ELSEIF_LIST, PHPGrammar.ALTERNATIVE_ELSEIF_LIST).getLastChild();
  }

  private static boolean hasElseif(AstNode ifStmt) {
    return ifStmt.hasDirectChildren(PHPGrammar.ELSEIF_LIST, PHPGrammar.ALTERNATIVE_ELSEIF_LIST);
  }

  private static boolean hasElse(AstNode ifStmt) {
    return ifStmt.hasDirectChildren(PHPGrammar.ELSE_CLAUSE, PHPGrammar.ALTERNATIVE_ELSE_CLAUSE);
  }
}
