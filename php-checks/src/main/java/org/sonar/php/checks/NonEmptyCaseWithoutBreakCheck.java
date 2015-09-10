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
import com.sonar.sslr.api.Trivia;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

@Rule(
  key = "S128",
  name = "Switch cases should end with an unconditional \"break\" statement",
  priority = Priority.CRITICAL,
  tags = {Tags.CERT, Tags.CWE, Tags.PITFALL, Tags.MISRA})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("10min")
public class NonEmptyCaseWithoutBreakCheck extends SquidCheck<LexerlessGrammar> {

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.CASE_CLAUSE,
      PHPGrammar.DEFAULT_CLAUSE);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!isLastCase(astNode) && !isEmpty(astNode) && (!hasJumpStatement(astNode) && !hasNoBreakComment(astNode))) {
      getContext().createLineViolation(this, "End this switch case with an unconditional break, continue, return or throw statement.", astNode);
    }
  }

  private boolean hasNoBreakComment(AstNode astNode) {
    for (Trivia trivia : astNode.getNextSibling().getToken().getTrivia()) {
      if (trivia.isComment()) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasJumpStatement(AstNode casClause) {

    AstNode lastStmt = casClause.getFirstChild(PHPGrammar.INNER_STATEMENT_LIST).getLastChild();
    if (lastStmt.is(PHPGrammar.STATEMENT) && lastStmt.getFirstChild().is(PHPGrammar.BREAK_STATEMENT)) {
      return true;
    }
    return casClause.hasDescendant(
      PHPGrammar.EXIT_EXPR,
      PHPGrammar.CONTINUE_STATEMENT,
      PHPGrammar.THROW_STATEMENT,
      PHPGrammar.RETURN_STATEMENT,
      PHPGrammar.BREAK_STATEMENT,
      PHPGrammar.GOTO_STATEMENT);
  }

  private static boolean isEmpty(AstNode caseClause) {
    return caseClause.getFirstChild(PHPGrammar.INNER_STATEMENT_LIST) == null;
  }

  private static boolean isLastCase(AstNode caseClause) {
    return caseClause.getNextAstNode().is(PHPPunctuator.RCURLYBRACE) || caseClause.getNextAstNode().is(PHPKeyword.ENDSWITCH);
  }
}
