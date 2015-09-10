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

import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNode;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.parser.PHPGrammar;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.sslr.parser.LexerlessGrammar;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.Set;

import static org.sonar.php.parser.PHPGrammar.ASSIGNMENT_BY_REFERENCE;
import static org.sonar.php.parser.PHPGrammar.ASSIGNMENT_EXPR;

@Rule(
  key = "S127",
  name = "\"for\" loop stop conditions should be invariant",
  priority = Priority.MAJOR,
  tags = {Tags.PITFALL, Tags.MISRA})
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.LOGIC_RELIABILITY)
@SqaleConstantRemediation("10min")
public class ForLoopCounterChangedCheck extends SquidCheck<LexerlessGrammar> {

  private Set<String> counters = Sets.newHashSet();
  private Set<String> pendingCounters = Sets.newHashSet();

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    counters.clear();
  }

  @Override
  public void init() {
    subscribeTo(
      PHPGrammar.FOR_STATEMENT,
      PHPGrammar.STATEMENT,

      PHPGrammar.ASSIGNMENT_EXPR,
      PHPGrammar.ASSIGNMENT_BY_REFERENCE,
      PHPPunctuator.INC,
      PHPPunctuator.DEC);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.FOR_STATEMENT)) {
      pendingCounters = getLoopsCounters(astNode);
    } else if (astNode.is(PHPGrammar.STATEMENT)) {
      counters.addAll(pendingCounters);
      pendingCounters = Collections.emptySet();
    } else if (!counters.isEmpty() && astNode.is(ASSIGNMENT_EXPR, ASSIGNMENT_BY_REFERENCE, PHPPunctuator.INC, PHPPunctuator.DEC)) {
      check(astNode);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(PHPGrammar.FOR_STATEMENT)) {
      counters.removeAll(getLoopsCounters(astNode));
    }
  }

  private void check(AstNode astNode) {
    AstNode varNode;

    if (astNode.is(ASSIGNMENT_EXPR, ASSIGNMENT_BY_REFERENCE)) {
      varNode = astNode.getFirstChild();
    } else {
      // Increment or decrement
      varNode = isPostUnaryExpr(astNode) ? astNode.getNextAstNode() : astNode.getPreviousAstNode();
    }

    String varName = CheckUtils.getExpressionAsString(varNode);

    if (counters.contains(varName)) {
      reportIssue(astNode, varName);
    }
  }

  private void reportIssue(AstNode astNode, String counter) {
    getContext().createLineViolation(this, "Refactor the code to avoid updating the loop counter \"{0}\" within the loop body.", astNode, counter);
  }

  private Set<String> getLoopsCounters(AstNode astNode) {
    Set<String> counterList = Sets.newHashSet();
    AstNode forExpr = astNode.getFirstChild(PHPPunctuator.SEMICOLON).getPreviousAstNode();

    for (AstNode expr : forExpr.getChildren(PHPGrammar.EXPRESSION)) {
      counterList.add(getCounterName(expr));
    }
    return counterList;
  }

  private String getCounterName(AstNode expression) {
    AstNode exprChild = expression.getFirstChild();
    AstNode variable;

    if (exprChild.is(PHPGrammar.POSTFIX_EXPR)) {
      variable = exprChild.getFirstChild();
    } else if (exprChild.is(PHPGrammar.UNARY_EXPR)) {
      variable = exprChild.getLastChild();
    } else {
      variable = exprChild.getFirstChild().getFirstChild();
    }

    return CheckUtils.getExpressionAsString(variable);
  }

  private boolean isPostUnaryExpr(AstNode unaryOperator) {
    return unaryOperator.getParent().is(PHPGrammar.UNARY_EXPR);
  }

}
