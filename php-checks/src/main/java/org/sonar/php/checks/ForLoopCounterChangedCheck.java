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

import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.squid.checks.SquidCheck;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.parser.PHPGrammar;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

@Rule(
  key = "S127",
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckList.SONAR_WAY_PROFILE, priority = Priority.MAJOR)
public class ForLoopCounterChangedCheck extends SquidCheck<Grammar> {

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
    } else if (!counters.isEmpty() && astNode.is(PHPGrammar.ASSIGNMENT_EXPR, PHPPunctuator.INC, PHPPunctuator.DEC)) {
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
    String varName;

    if (astNode.is(PHPGrammar.ASSIGNMENT_EXPR)) {
      varName = getVarName(astNode.getFirstChild());
    } else {
      // Increment or decrement
      varName = isPostUnaryExpr(astNode) ? getVarName(astNode.getNextAstNode()) : getVarName(astNode.getPreviousAstNode());
    }

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
      counterList.add(expr.getFirstChild().getTokenOriginalValue());
    }
    return counterList;
  }

  private boolean isPostUnaryExpr(AstNode unaryOperator) {
    return unaryOperator.getParent().is(PHPGrammar.UNARY_EXPR);
  }

  private String getVarName(AstNode expr) {
    StringBuilder builder = new StringBuilder();
    for (Token token : expr.getTokens()) {
      builder.append(token.getOriginalValue());
    }
    return builder.toString();
  }
}
