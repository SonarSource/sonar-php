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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.php.parser.PHPTokenType;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import java.util.Map;
import java.util.Set;

@Rule(
  key = "S122",
  name = "Statements should be on separate lines",
  priority = Priority.MINOR,
  tags = {Tags.CONVENTION, Tags.PSR2})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("1min")
public class OneStatementPerLineCheck extends SquidCheck<LexerlessGrammar> {

  private final Map<Integer, StatementCount> statementsPerLine = Maps.newHashMap();
  private final Set<Integer> linesWithHtml = Sets.newHashSet();
  private boolean inFunctionExpression = false;

  private static class StatementCount {
    int nbStatement = 0;
    int nbFunctionExpression = 0;
    int nbNestedStatement = 0;
  }

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.TOP_STATEMENT,
      PHPGrammar.STATEMENT,
      PHPGrammar.FUNCTION_EXPRESSION,
      PHPTokenType.INLINE_HTML);
  }


  @Override
  public void visitFile(AstNode astNode) {
    statementsPerLine.clear();
    linesWithHtml.clear();
    inFunctionExpression = false;
  }

  @Override
  public void visitNode(AstNode node) {
    if (node.is(PHPTokenType.INLINE_HTML)) {
      linesWithHtml.add(node.getTokenLine());
    } else if (node.is(PHPGrammar.FUNCTION_EXPRESSION)) {
      int line = node.getTokenLine();

      if (statementsPerLine.containsKey(line)) {
        statementsPerLine.get(line).nbFunctionExpression++;
        inFunctionExpression = true;
      }
    } else if (!isExcluded(node)) {
      int line = node.getTokenLine();

      if (!statementsPerLine.containsKey(line)) {
        statementsPerLine.put(line, new StatementCount());
      } else if (inFunctionExpression) {
        statementsPerLine.get(line).nbNestedStatement++;
      }

      if (!inFunctionExpression) {
        statementsPerLine.get(line).nbStatement++;
      }
    }
  }

  @Override
  public void leaveFile(AstNode astNode) {
    for (Map.Entry<Integer, StatementCount> statementsAtLine : statementsPerLine.entrySet()) {
      Integer line = statementsAtLine.getKey();
      if (linesWithHtml.contains(line)) {
        continue;
      }

      StatementCount stmtCount = statementsAtLine.getValue();

      if (stmtCount.nbStatement > 1 || stmtCount.nbFunctionExpression > 1 || stmtCount.nbNestedStatement > 1) {
        getContext().createLineViolation(this, "{0} statements were found on this line. Reformat the code to have only one statement per line.", line,
          stmtCount.nbStatement + stmtCount.nbNestedStatement);
      }
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.FUNCTION_EXPRESSION) && statementsPerLine.containsKey(astNode.getTokenLine())) {
      inFunctionExpression = false;
    }
  }

  public boolean isExcluded(AstNode statementNode) {
    AstNode child = statementNode.getFirstChild();
    return child.is(
      PHPGrammar.BLOCK,
      PHPGrammar.LABEL,
      PHPTokenType.INLINE_HTML,
      PHPGrammar.EMPTY_STATEMENT,
      PHPGrammar.STATEMENT);
  }
}
